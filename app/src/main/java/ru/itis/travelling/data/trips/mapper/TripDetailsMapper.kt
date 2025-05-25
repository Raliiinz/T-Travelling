package ru.itis.travelling.data.trips.mapper

import ru.itis.travelling.data.trips.remote.model.TripDetailsRequest
import ru.itis.travelling.data.trips.remote.model.TripDetailsResponse
import ru.itis.travelling.data.trips.remote.model.UserDto
import ru.itis.travelling.domain.trips.model.Participant
import ru.itis.travelling.domain.trips.model.TripDetails
import javax.inject.Inject

class TripDetailsMapper @Inject constructor() {

    fun mapToRequest(trip: TripDetails): TripDetailsRequest {
        return TripDetailsRequest(
            name = trip.destination,
            totalBudget = trip.price.toDoubleOrNull() ?: 0.0,
            dateOfBegin = trip.startDate,
            dateOfEnd = trip.endDate,
            participantPhones = trip.participants.map { it.phone }
        )
    }

    fun mapFromResponse(response: TripDetailsResponse): TripDetails {
        return TripDetails(
            id = response.id.toString(),
            destination = response.name,
            startDate = response.dateOfBegin,
            endDate = response.dateOfEnd,
            price = response.totalBudget.toString(),
            admin = mapUserDtoToParticipant(response.creator),
            participants = response.participants.map { mapUserDtoToParticipant(it) }.toMutableList()
        )
    }

    private fun mapUserDtoToParticipant(userDto: UserDto): Participant {
        return Participant(
            phone = userDto.phoneNumber,
            firstName = userDto.firstName,
            lastName = userDto.lastName
        )
    }
}
