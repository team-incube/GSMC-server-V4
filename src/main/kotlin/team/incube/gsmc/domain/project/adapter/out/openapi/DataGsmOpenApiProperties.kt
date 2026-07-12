package team.incube.gsmc.domain.project.adapter.out.openapi

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "datagsm.openapi")
data class DataGsmOpenApiProperties(
    val baseUrl: String,
    val apiKey: String,
)
