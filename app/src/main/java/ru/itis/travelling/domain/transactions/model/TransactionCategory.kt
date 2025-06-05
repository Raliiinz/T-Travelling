package ru.itis.travelling.domain.transactions.model

import android.content.Context
import ru.itis.travelling.R

enum class TransactionCategory {
    TRANSPORT,
    ACCOMMODATION,
    FOOD,
    ENTERTAINMENT,
    SHOPPING,
    HEALTH,
    COMMUNICATION,
    VISA_DOCUMENTS,
    OTHER
}

fun TransactionCategory.getDisplayName(context: Context): String = when (this) {
    TransactionCategory.TRANSPORT -> context.getString(R.string.category_transport)
    TransactionCategory.ACCOMMODATION -> context.getString(R.string.category_accommodation)
    TransactionCategory.FOOD -> context.getString(R.string.category_food)
    TransactionCategory.ENTERTAINMENT -> context.getString(R.string.category_entertainment)
    TransactionCategory.SHOPPING -> context.getString(R.string.category_shopping)
    TransactionCategory.HEALTH -> context.getString(R.string.category_health)
    TransactionCategory.COMMUNICATION -> context.getString(R.string.category_communication)
    TransactionCategory.VISA_DOCUMENTS -> context.getString(R.string.category_visa_documents)
    TransactionCategory.OTHER -> context.getString(R.string.category_other)
}

//fun TransactionCategory.getDisplayName(): String = when (this) {
//    TransactionCategory.FOOD -> "Питание"
//    TransactionCategory.TRANSPORT -> "Транспорт"
//    TransactionCategory.ACCOMMODATION -> "Проживание"
//    TransactionCategory.ENTERTAINMENT -> "Развлечения"
//    TransactionCategory.SHOPPING -> "Покупки"
//    TransactionCategory.HEALTH -> "Здоровье"
//    TransactionCategory.COMMUNICATION -> "Связь и интернет"
//    TransactionCategory.VISA_DOCUMENTS -> "Виза и документы"
//    TransactionCategory.OTHER -> "Прочее"
//}

fun TransactionCategory.getIconResId(): Int = when (this) {
    TransactionCategory.TRANSPORT -> R.drawable.ic_transport
    TransactionCategory.ACCOMMODATION -> R.drawable.ic_accommodation
    TransactionCategory.FOOD -> R.drawable.ic_food
    TransactionCategory.ENTERTAINMENT -> R.drawable.ic_entertainment
    TransactionCategory.SHOPPING -> R.drawable.ic_shopping
    TransactionCategory.HEALTH -> R.drawable.ic_health
    TransactionCategory.COMMUNICATION -> R.drawable.ic_communication
    TransactionCategory.VISA_DOCUMENTS -> R.drawable.ic_visa_documents
    TransactionCategory.OTHER -> R.drawable.ic_other
}

//fun mapCategoryToApiValue(displayName: String): String {
//    return when (displayName) {
//        "Transport"-> "TRANSPORT"
//        "Accommodation" -> "ACCOMMODATION"
//        "Food" -> "FOOD"
//        "Entertainment" -> "ENTERTAINMENT"
//        "Shopping" -> "SHOPPING"
//        "Health" -> "HEALTH"
//        "Communication" -> "COMMUNICATION"
//        "Visa Documents" -> "VISA_DOCUMENTS"
//        else -> "OTHER"
//    }
//}
fun mapCategoryToApiValue(displayName: String, context: Context): String {
    return when (displayName) {
        context.getString(R.string.category_transport) -> "TRANSPORT"
        context.getString(R.string.category_accommodation) -> "ACCOMMODATION"
        context.getString(R.string.category_food) -> "FOOD"
        context.getString(R.string.category_entertainment) -> "ENTERTAINMENT"
        context.getString(R.string.category_shopping) -> "SHOPPING"
        context.getString(R.string.category_health) -> "HEALTH"
        context.getString(R.string.category_communication) -> "COMMUNICATION"
        context.getString(R.string.category_visa_documents) -> "VISA_DOCUMENTS"
        else -> "OTHER"
    }
}