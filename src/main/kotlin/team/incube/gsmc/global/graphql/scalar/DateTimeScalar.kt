package team.incube.gsmc.global.graphql.scalar

import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeScalar {
    val DateTime: GraphQLScalarType =
        GraphQLScalarType
            .newScalar()
            .name("DateTime")
            .description("ISO-8601 날짜/시간 스칼라 (예: 2024-01-01T12:00:00+09:00)")
            .coercing(
                object : Coercing<OffsetDateTime, String> {
                    override fun serialize(
                        dataFetcherResult: Any,
                        graphQLContext: GraphQLContext,
                        locale: Locale,
                    ): String =
                        when (dataFetcherResult) {
                            is OffsetDateTime -> dataFetcherResult.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                            is ZonedDateTime ->
                                dataFetcherResult.toOffsetDateTime().format(
                                    DateTimeFormatter.ISO_OFFSET_DATE_TIME,
                                )
                            is Instant ->
                                dataFetcherResult
                                    .atOffset(
                                        ZoneOffset.UTC,
                                    ).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                            is LocalDateTime ->
                                dataFetcherResult
                                    .atZone(ZoneId.of("Asia/Seoul"))
                                    .toOffsetDateTime()
                                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                            is String -> dataFetcherResult
                            else -> throw CoercingSerializeException(
                                "DateTime으로 직렬화할 수 없는 타입: ${dataFetcherResult::class}",
                            )
                        }

                    override fun parseValue(
                        input: Any,
                        graphQLContext: GraphQLContext,
                        locale: Locale,
                    ): OffsetDateTime =
                        when (input) {
                            is String ->
                                runCatching { OffsetDateTime.parse(input, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
                                    .getOrElse { throw CoercingParseValueException("유효하지 않은 DateTime 값: $input") }
                            else -> throw CoercingParseValueException("DateTime으로 파싱할 수 없는 타입: ${input::class}")
                        }

                    override fun parseLiteral(
                        input: Value<*>,
                        variables: CoercedVariables,
                        graphQLContext: GraphQLContext,
                        locale: Locale,
                    ): OffsetDateTime =
                        when (input) {
                            is StringValue ->
                                runCatching {
                                    OffsetDateTime.parse(input.value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                }.getOrElse {
                                    throw CoercingParseLiteralException(
                                        "유효하지 않은 DateTime 리터럴: ${input.value}",
                                    )
                                }
                            else -> throw CoercingParseLiteralException("DateTime으로 파싱할 수 없는 리터럴 타입: ${input::class}")
                        }
                },
            ).build()
}
