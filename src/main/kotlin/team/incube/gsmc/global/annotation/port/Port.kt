package team.incube.gsmc.global.annotation.port

import org.springframework.stereotype.Component
import team.incube.gsmc.global.annotation.PortDirection

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class Port(
    val direction: PortDirection,
)
