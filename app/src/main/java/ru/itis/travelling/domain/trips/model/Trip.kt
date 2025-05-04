package ru.itis.travelling.domain.trips.model

data class Trip(
    val id: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val price: String,
    val admin: Participant,
    val participants: MutableList<Participant>,
//    val transactions: List<Transaction>
)


//data class Transaction(
//    val id: String,
//    val title: String,
//    val amount: String,
//    val date: String
//)
