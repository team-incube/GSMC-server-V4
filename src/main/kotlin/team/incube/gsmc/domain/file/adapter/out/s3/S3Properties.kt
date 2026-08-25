package team.incube.gsmc.domain.file.adapter.out.s3

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "aws.s3")
data class S3Properties(
    val bucket: String,
    val region: String,
    val accessKey: String,
    val secretKey: String,
)
