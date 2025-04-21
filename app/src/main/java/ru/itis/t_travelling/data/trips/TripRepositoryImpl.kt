package ru.itis.t_travelling.data.trips

import ru.itis.t_travelling.domain.trips.model.Trip
import ru.itis.t_travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
//    private val tripApi: TripApi,
//    private val tripDao: TripDao
) : TripRepository {

    override suspend fun getTripsByUserId(userId: String): List<Trip> {
        // Сначала проверяем локальную БД
//        val localTrips = tripDao.getTripsByUserId(userId)
//        return if (localTrips.isNotEmpty()) {
//            localTrips
//        } else {
//            // Если локально нет данных, запрашиваем с API
//            val remoteTrips = tripApi.getTripsByUserId(userId)
//            // Сохраняем в БД для кэширования
//            tripDao.insertAll(remoteTrips)
//            remoteTrips
//        }
        return emptyList()
    }
}