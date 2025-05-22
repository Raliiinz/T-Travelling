package ru.itis.travelling.domain.authregister.model

data class User(
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val confirmPassword: String
)