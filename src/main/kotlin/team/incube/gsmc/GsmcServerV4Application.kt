package team.incube.gsmc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GsmcServerV4Application

fun main(args: Array<String>) {
    runApplication<GsmcServerV4Application>(*args)
}
