package traversium.tripservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TripServiceApplication

fun main(args: Array<String>) {
    runApplication<TripServiceApplication>(*args)
}
