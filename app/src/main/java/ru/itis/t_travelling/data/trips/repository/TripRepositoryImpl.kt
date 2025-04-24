package ru.itis.t_travelling.data.trips.repository

import ru.itis.t_travelling.domain.trips.model.Participant
import ru.itis.t_travelling.domain.trips.model.Trip
import ru.itis.t_travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
//    TODO("Api")
//    private val tripApi: TripApi,
) : TripRepository {


    override suspend fun getTripsByPhone(phoneNumber: String): List<Trip> {
//        TODO("Not yet implemented")
//        val remoteTrips = tripApi.getTripsByUserId(userId)
//        return remoteTrips
        return listOf(
                Trip(
            id = "1",
            destination = "Сочи",
            startDate = "12.12.2025",
            endDate = "24.12.2025",
            price = 150000,
            admin = Participant("1", "+79276654566"),
            participants =
                listOf(
                    Participant("2", "+79274486464"),
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
                price = 130000,
                admin = Participant("1", "+79376654566"),
                participants =
                    listOf(
                        Participant("2", "+79274486499"),
                        Participant("3", "+79274389964"),
                        Participant("4", "+79374996464"),
                        Participant("5", "+79299433464"),
                        Participant("6", "+79274399464"),
                    )
            ),
            Trip(
                id = "2",
                destination = "Москва",
                startDate = "12.12.2026",
                endDate = "24.12.2026",
                price = 130000,
                admin = Participant("1", "+79376654566"),
                participants =
                    listOf(
                        Participant("2", "+79274486499"),
                        Participant("3", "+79274389964"),
                        Participant("4", "+79374996464"),
                        Participant("5", "+79299433464"),
                        Participant("6", "+79274399464"),
                    )
            ),
            Trip(
                id = "2",
                destination = "Москва",
                startDate = "12.12.2026",
                endDate = "24.12.2026",
                price = 130000,
                admin = Participant("1", "+79376654566"),
                participants =
                    listOf(
                        Participant("2", "+79274486499"),
                        Participant("3", "+79274389964"),
                        Participant("4", "+79374996464"),
                        Participant("5", "+79299433464"),
                        Participant("6", "+79274399464"),
                    )
            ),
            Trip(
                id = "2",
                destination = "Москва",
                startDate = "12.12.2026",
                endDate = "24.12.2026",
                price = 130000,
                admin = Participant("1", "+79376654566"),
                participants =
                    listOf(
                        Participant("2", "+79274486499"),
                        Participant("3", "+79274389964"),
                        Participant("4", "+79374996464"),
                        Participant("5", "+79299433464"),
                        Participant("6", "+79274399464"),
                    )
            ),
            Trip(
                id = "2",
                destination = "Москва",
                startDate = "12.12.2026",
                endDate = "24.12.2026",
                price = 130000,
                admin = Participant("1", "+79376654566"),
                participants =
                    listOf(
                        Participant("2", "+79274486499"),
                        Participant("3", "+79274389964"),
                        Participant("4", "+79374996464"),
                        Participant("5", "+79299433464"),
                        Participant("6", "+79274399464"),
                    )
            ),
            Trip(
                id = "2",
                destination = "Москва",
                startDate = "12.12.2026",
                endDate = "24.12.2026",
                price = 130000,
                admin = Participant("1", "+79376654566"),
                participants =
                    listOf(
                        Participant("2", "+79274486499"),
                        Participant("3", "+79274389964"),
                        Participant("4", "+79374996464"),
                        Participant("5", "+79299433464"),
                        Participant("6", "+79274399464"),
                    )
            ),
            Trip(
                id = "2",
                destination = "Москва",
                startDate = "12.12.2026",
                endDate = "24.12.2026",
                price = 130000,
                admin = Participant("1", "+79376654566"),
                participants =
                    listOf(
                        Participant("2", "+79274486499"),
                        Participant("3", "+79274389964"),
                        Participant("4", "+79374996464"),
                        Participant("5", "+79299433464"),
                        Participant("6", "+79274399464"),
                    )
            ),
            Trip(
                id = "2",
                destination = "Москва",
                startDate = "12.12.2026",
                endDate = "24.12.2026",
                price = 130000,
                admin = Participant("1", "+79376654566"),
                participants =
                    listOf(
                        Participant("2", "+79274486499"),
                        Participant("3", "+79274389964"),
                        Participant("4", "+79374996464"),
                        Participant("5", "+79299433464"),
                        Participant("6", "+79274399464"),
                    )
            ),
            Trip(
                id = "2",
                destination = "Москва",
                startDate = "12.12.2026",
                endDate = "24.12.2026",
                price = 130000,
                admin = Participant("1", "+79376654566"),
                participants =
                    listOf(
                        Participant("2", "+79274486499"),
                        Participant("3", "+79274389964"),
                        Participant("4", "+79374996464"),
                        Participant("5", "+79299433464"),
                        Participant("6", "+79274399464"),
                    )
            ),
        )
    }

    override suspend fun getTripDetails(tripId: String): Trip? {
//        TODO("Not yet implemented")
        return Trip(
            id = "1",
            destination = "Сочи",
            startDate = "12.12.2025",
            endDate = "24.12.2025",
            price = 150000,
            admin = Participant("1", "+79276654566"),
            participants =
                listOf(
                    Participant("2", "+79274486464"),
                    Participant("3", "+79274386464"),
                    Participant("4", "+79374486464"),
                    Participant("5", "+79274433464"),
                    Participant("6", "+79274336464"),
                )
        )
    }
}