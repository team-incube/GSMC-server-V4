package team.incube.gsmc.domain.project.adapter.out.openapi

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "datagsm.openapi")
data class DataGsmOpenApiProperties(
    val baseUrl: String,
    val apiKey: String,
    val connectTimeout: Duration = Duration.ofSeconds(3),
    val readTimeout: Duration = Duration.ofSeconds(5),
)
