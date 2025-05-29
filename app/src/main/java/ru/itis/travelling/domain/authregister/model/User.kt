package ru.itis.travelling.domain.authregister.model

data class User(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val password: String,
    val confirmPassword: String
)