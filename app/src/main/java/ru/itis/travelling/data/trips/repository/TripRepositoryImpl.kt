package ru.itis.travelling.data.trips.repository

import ru.itis.travelling.domain.trips.model.Participant
import ru.itis.travelling.domain.trips.model.Trip
import ru.itis.travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
//    TODO("Api")
//    private val tripApi: TripApi,
) : TripRepository {

    // Временное хранилище для демонстрации работы
    private val mockTrips = mutableListOf(
        Trip(
            id = "1",
            destination = "Сочи",
            startDate = "12.12.2025",
            endDate = "24.12.2025",
            price = "150000",
            admin = Participant("1", "89274486464"),
            participants = mutableListOf(
                Participant("2", "+79274488464"),
                Participant("3", "+79274386464"),
                Participant("4", "+79374486464"),
                Participant("5", "+79274433464"),
                Participant("6", "+79274336464"),
            )
        ),
        Trip(
            id = "2",
            destination = "Москва",
            startDate = "12.12.2026",
            endDate = "24.12.2026",
            price = "130000",
            admin = Participant("1", "+79376654566"),
            participants = mutableListOf(
                Participant("2", "89274486464"),
                Participant("3", "+79274389964"),
                Participant("4", "+79374996464"),
                Participant("5", "+79299433464"),
                Participant("6", "+79274399464"),
            )
        )
    )

    override suspend fun getTripsByPhone(phoneNumber: String): List<Trip> {
        return mockTrips.filter { trip ->
            trip.admin.phone == phoneNumber || trip.participants.any { it.phone == phoneNumber }
        }
    }

    override suspend fun getTripDetails(tripId: String): Trip? {
        return mockTrips.find { it.id == tripId }
    }

    override suspend fun leaveTrip(tripId: String, userPhone: String): Boolean {
        val trip = mockTrips.find { it.id == tripId } ?: return false

        if (trip.admin.phone == userPhone) {
            return false
        }

        trip.participants.removeAll { it.phone == userPhone }
        return true
    }

    override suspend fun deleteTrip(tripId: String): Boolean {
        return mockTrips.removeAll { it.id == tripId }
    }
}