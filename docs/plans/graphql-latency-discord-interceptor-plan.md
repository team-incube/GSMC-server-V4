# GraphQL 응답속도 Discord 알림 인터셉터 구현 계획

- 작성일: 2026-07-17
- 관련 문서: 없음

## 1. 개요

모든 GraphQL 요청의 응답속도를 Discord 채널로 전송한다. GraphQL은 REST와 달리 모든 요청이 `POST /graphql` 하나로 들어오므로, URL만으로는 어떤 쿼리/뮤테이션이었는지 구분할 수 없다 — 이를 위해 Spring for GraphQL의 `WebGraphQlInterceptor`를 이 프로젝트 최초로 도입한다.

현재 이 코드베이스에는:
- `WebGraphQlInterceptor`/`WebGraphQlConfigurer` 구현체가 전혀 없다.
- `alert` 도메인은 GraphQL 스키마에 입력 타입(`PatchAlertIsReadInput`) 하나만 있고 domain/port/service/adapter 전부 미구현이다.
- Discord 웹훅 연동 코드, `WebClient`/`RestTemplate` 빈 등록이 전혀 없다 — 완전히 새로 만드는 작업이다.
- 인증은 `JwtAuthenticationFilter`(서블릿 `Filter`)가 처리하고, `sdk.logging`은 `application.yaml`의 `not-logging-urls`에 `/graphql/**`가 등록돼 있어 GraphQL 요청을 실제로 로깅하지 않고 있다 — 이번 작업과 무관하지만 참고용으로 기록.

## 2. 인터뷰로 확정한 의사결정 사항

| 항목 | 결정 |
| --- | --- |
| 요청 식별 정보 | `operationName`이 아니라 **쿼리 원문(`request.document`)** 사용 — operationName 없는 익명 쿼리도 식별 가능하고 필드 단위까지 보여줌 |
| 전송 범위 | 임계치 필터링 없이 **모든 요청** 전송. 대신 가독성을 최대한 높인 포맷으로 보완 |
| 메시지 포맷 | Discord **embed** + 응답속도별 색상 구분 |
| 색상 임계값 | 🟢 <300ms / 🟡 300~700ms / 🟠 700~1000ms / 🔴 ≥1000ms |
| embed 추가 필드 | 요청자 정보(`userId`/`role`, `SecurityContextHolder`에서 조회), 요청 변수(`variables`), GraphQL 에러 여부(에러 시 강제 빨강 + 에러 메시지) |
| 변수 마스킹 | 키 이름에 `password`/`token`/`secret`(대소문자 무관)이 포함되면 값을 `***`로 치환. 현재 스키마엔 해당 필드가 없지만 외부로 나가는 메시지라 되돌리기 어려우므로 최소 안전장치로 추가 |
| Discord 클라이언트 위치 | `global/discord/`로 분리해 재사용 가능한 컴포넌트로 구현 (향후 "점수 승인/반려 알림" 등 다른 Discord 알림에도 재사용 가능) |
| 인터셉터/설정 위치 | `global/graphql/interceptor/`, `global/graphql/config/` — 기존 `global/graphql/config`(스칼라 설정), `global/security/filter`(서블릿 필터) 배치 관례를 따름 |
| HTTP 클라이언트 | `RestClient`(Spring 6.1+, `spring-web`에 이미 포함 — 신규 의존성 불필요). `WebClient`(reactive, `spring-boot-starter-webflux` 신규 의존성 필요)는 기각 |
| 비동기 처리 | `Mono.fromRunnable { ... }.subscribeOn(Schedulers.boundedElastic()).subscribe()`로 응답 파이프라인과 완전히 분리한 fire-and-forget. Discord 전송 실패/지연이 실제 GraphQL 응답에 전혀 영향을 주지 않음 |
| Webhook URL 설정 | 기존 시크릿 관리 관례(`application.yaml`의 `${JWT_SECRET}` 등)와 동일하게 `${DISCORD_GRAPHQL_LATENCY_WEBHOOK_URL}` 환경변수 플레이스홀더 사용 — 코드/설정 파일에 실제 URL 하드코딩 금지 |

## 3. 패키지/파일 구조

```
global/
├── config/
│   └── RestClientConfig.kt              신규 — RestClient 빈 등록
├── discord/
│   ├── DiscordEmbed.kt                  신규 — embed 페이로드 모델(title, description, color, fields)
│   └── DiscordWebhookClient.kt          신규 — RestClient로 webhookUrl에 embed 전송, fire-and-forget, 실패 시 로그만 남기고 삼킴
└── graphql/
    └── interceptor/
        └── GraphQlLatencyDiscordInterceptor.kt   신규 — @Component + WebGraphQlInterceptor 구현. Boot가 ObjectProvider<WebGraphQlInterceptor>로 자동 수집·등록하므로 별도 설정 클래스 불필요
```

## 4. `global/discord/` 설계

### `DiscordEmbed.kt`

```kotlin
package team.incube.gsmc.global.discord

data class DiscordEmbed(
    val title: String,
    val description: String? = null,
    val color: Int,
    val fields: List<Field> = emptyList(),
) {
    data class Field(
        val name: String,
        val value: String,
        val inline: Boolean = false,
    )

    companion object {
        const val COLOR_GREEN = 0x2ECC71
        const val COLOR_YELLOW = 0xF1C40F
        const val COLOR_ORANGE = 0xE67E22
        const val COLOR_RED = 0xE74C3C
    }
}
```

### `DiscordWebhookClient.kt`

```kotlin
package team.incube.gsmc.global.discord

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class DiscordWebhookClient(
    private val restClient: RestClient,
) {
    private val logger = LoggerFactory.getLogger(DiscordWebhookClient::class.java)

    fun sendAsync(
        webhookUrl: String,
        embed: DiscordEmbed,
    ) {
        Mono.fromRunnable<Unit> { send(webhookUrl, embed) }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    private fun send(
        webhookUrl: String,
        embed: DiscordEmbed,
    ) {
        runCatching {
            restClient
                .post()
                .uri(webhookUrl)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(mapOf("embeds" to listOf(embed.toPayload())))
                .retrieve()
                .toBodilessEntity()
        }.onFailure { logger.warn("Discord webhook 전송 실패: {}", it.message) }
    }

    private fun DiscordEmbed.toPayload(): Map<String, Any?> =
        mapOf(
            "title" to title,
            "description" to description,
            "color" to color,
            "fields" to
                fields.map {
                    mapOf("name" to it.name, "value" to it.value, "inline" to it.inline)
                },
        )
}
```

`RestClient`는 `RestClient.Bean` 형태로 `global/config/`에 빈 등록(신규, `RestClient.create()`). 실패는 로그만 남기고 삼켜서 Discord 장애가 애플리케이션에 전혀 영향을 주지 않는다.

## 5. `global/graphql/` 설계

### `GraphQlLatencyDiscordInterceptor.kt`

```kotlin
package team.incube.gsmc.global.graphql.interceptor

import org.springframework.beans.factory.annotation.Value
import org.springframework.graphql.server.WebGraphQlInterceptor
import org.springframework.graphql.server.WebGraphQlRequest
import org.springframework.graphql.server.WebGraphQlResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import team.incube.gsmc.global.discord.DiscordEmbed
import team.incube.gsmc.global.discord.DiscordWebhookClient

@Component
class GraphQlLatencyDiscordInterceptor(
    private val discordWebhookClient: DiscordWebhookClient,
    @Value("\${discord.webhook.graphql-latency-url}") private val webhookUrl: String,
) : WebGraphQlInterceptor {
    override fun intercept(
        request: WebGraphQlRequest,
        chain: WebGraphQlInterceptor.Chain,
    ): Mono<WebGraphQlResponse> {
        val start = System.currentTimeMillis()
        return chain
            .next(request)
            .doOnNext { response -> report(request, response, System.currentTimeMillis() - start) }
    }

    private fun report(
        request: WebGraphQlRequest,
        response: WebGraphQlResponse,
        elapsedMs: Long,
    ) {
        val hasErrors = response.errors.isNotEmpty()
        val color =
            when {
                hasErrors -> DiscordEmbed.COLOR_RED
                elapsedMs < 300 -> DiscordEmbed.COLOR_GREEN
                elapsedMs < 700 -> DiscordEmbed.COLOR_YELLOW
                elapsedMs < 1000 -> DiscordEmbed.COLOR_ORANGE
                else -> DiscordEmbed.COLOR_RED
            }

        val fields =
            buildList {
                add(DiscordEmbed.Field("응답시간", "${elapsedMs}ms", inline = true))
                add(DiscordEmbed.Field("요청자", requesterInfo(), inline = true))
                add(DiscordEmbed.Field("쿼리", truncate(request.document), inline = false))
                if (request.variables.isNotEmpty()) {
                    add(DiscordEmbed.Field("변수", truncate(maskSensitive(request.variables).toString()), inline = false))
                }
                if (hasErrors) {
                    add(DiscordEmbed.Field("에러", truncate(response.errors.joinToString { it.message }), inline = false))
                }
            }

        discordWebhookClient.sendAsync(
            webhookUrl,
            DiscordEmbed(title = "GraphQL 요청", color = color, fields = fields),
        )
    }

    private fun requesterInfo(): String {
        val auth = SecurityContextHolder.getContext().authentication ?: return "익명"
        return "${auth.name} (${auth.authorities.joinToString()})"
    }

    private fun maskSensitive(variables: Map<String, Any?>): Map<String, Any?> =
        variables.mapValues { (key, value) ->
            if (SENSITIVE_KEY_PATTERN.containsMatchIn(key)) "***" else value
        }

    private fun truncate(text: String): String = if (text.length > 900) text.take(900) + "..." else text

    companion object {
        private val SENSITIVE_KEY_PATTERN = Regex("password|token|secret", RegexOption.IGNORE_CASE)
    }
}
```

### 인터셉터 등록 방식 (설정 클래스 불필요)

실제 클래스패스(spring-boot-graphql 4.0.5, spring-graphql 2.0.2) 확인 결과 `WebGraphQlConfigurer`라는 인터페이스는 이 버전에 아예 존재하지 않는다. `GraphQlWebMvcAutoConfiguration.webGraphQlHandler(ExecutionGraphQlService, ObjectProvider<WebGraphQlInterceptor>)`가 컨텍스트의 모든 `WebGraphQlInterceptor` 빈을 `ObjectProvider`로 자동 수집해 등록한다. 따라서 `GraphQlLatencyDiscordInterceptor`를 `@Component`로만 등록하면 되고, 별도 `WebGraphQlConfigurer`/`GraphQlInterceptorConfig` 클래스는 불필요하다.

`application.yaml` 추가:
```yaml
discord:
  webhook:
    graphql-latency-url: ${DISCORD_GRAPHQL_LATENCY_WEBHOOK_URL}
```

## 6. 열린 이슈 / 검증 필요 사항

- **`SecurityContextHolder` 접근 가능 여부**: `MemberUtil`과 동일한 패턴(`SecurityContextHolder.getContext().authentication?.principal as? CustomUserDetails`)으로 구현했다. 서블릿(MVC) 스택이라 요청 스레드가 그대로 유지될 것으로 보이지만, **실제 로그인 후 GraphQL 요청을 날려 요청자 필드가 채워지는지는 아직 실제 환경에서 검증하지 못함** — 로컬 구동 후 확인 필요
- **Discord Rate Limit**: 웹훅 기본 한도(2초당 5회)를 넘는 트래픽이 발생하면 429가 뜰 수 있음 — 현재는 모든 요청을 다 보내기로 했으므로, 트래픽이 늘면 재검토 필요(현재는 학교 인증관리 시스템 특성상 저트래픽으로 가정)

## 7. 작업 순서 (체크리스트)

1. [x] `global/config/RestClientConfig.kt` — `RestClient` 빈 등록
2. [x] `global/discord/DiscordEmbed.kt` 작성
3. [x] `global/discord/DiscordWebhookClient.kt` 작성
4. [x] `global/graphql/interceptor/GraphQlLatencyDiscordInterceptor.kt` 작성 (`@Component`만으로 자동 등록 확인 완료, §6의 SecurityContext 부분은 코드는 완성했으나 실환경 검증 아직)
5. [x] `application.yaml`에 `discord.webhook.graphql-latency-url` 추가
6. [ ] 로컬에서 실제 Discord 웹훅(테스트용 채널)으로 GraphQL 쿼리/뮤테이션 실행해 embed가 의도대로 오는지 수동 검증 (색상 구간별, 에러 케이스, 변수 마스킹 포함) — 실제 webhook URL 필요, 사람이 직접 확인
7. [x] KtLint 포맷 적용 (`ktlintFormat`/`ktlintCheck` 통과)
8. [x] 단위 테스트 작성: `DiscordWebhookClientTest`(2건), `GraphQlLatencyDiscordInterceptorTest`(4건) — 전체 테스트 스위트 101건 실패 없이 통과
