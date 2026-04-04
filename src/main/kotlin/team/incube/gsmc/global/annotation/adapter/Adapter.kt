package team.incube.gsmc.global.annotation.adapter

import org.springframework.stereotype.Component
import team.incube.gsmc.global.annotation.PortDirection

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class Adapter(
    val direction: PortDirection,
)
