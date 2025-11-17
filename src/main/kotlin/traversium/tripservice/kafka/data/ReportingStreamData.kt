package traversium.tripservice.kafka.data


import java.time.YearMonth


data class ReportingStreamData(
    val timestamp: YearMonth,
    val action: DomainEvent
)