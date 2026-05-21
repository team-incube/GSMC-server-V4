package team.incube.gsmc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import team.incube.gsmc.global.security.jwt.JwtProperties

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class)
class GsmcServerV4Application

fun main(args: Array<String>) {
    runApplication<GsmcServerV4Application>(*args)
}
