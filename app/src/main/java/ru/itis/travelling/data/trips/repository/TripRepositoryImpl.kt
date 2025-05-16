package ru.itis.travelling.data.trips.repository

import ru.itis.travelling.domain.trips.model.Participant
import ru.itis.travelling.domain.trips.model.Trip
import ru.itis.travelling.domain.trips.repository.TripRepository
import ru.itis.travelling.presentation.trips.util.FormatUtils
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
            admin = Participant("89274486464"),
            participants = mutableListOf(
                Participant("89274488464"),
                Participant("89274386464"),
                Participant("89374486464")
            )
        ),
        Trip(
            id = "2",
            destination = "Москва",
            startDate = "12.12.2026",
            endDate = "24.12.2026",
            price = "130000",
            admin = Participant("89376654566"),
            participants = mutableListOf(
                Participant("89274486464"),
                Participant("89274389964"),
                Participant("89374996464"),
                Participant("89299433464"),
                Participant("89274399464"),
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

    override suspend fun createTrip(trip: Trip) {
        //TODO
//        api.createTrip(trip)
        val newId = (mockTrips.maxOfOrNull { it.id.toInt() }?.plus(1) ?: 1).toString()

        val newTrip = trip.copy(id = newId)

        mockTrips.add(newTrip)
    }

    override suspend fun updateTrip(trip: Trip) {
        //TODO
        val index = mockTrips.indexOfFirst { it.id == trip.id }

        if (index != -1) {
            mockTrips[index] = trip
        } else {
            println("Trip with id ${trip.id} not found for update")
            throw NoSuchElementException("Trip with id ${trip.id} not found")
        }
    }
}