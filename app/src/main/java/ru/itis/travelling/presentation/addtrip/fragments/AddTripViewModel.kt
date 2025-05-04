package ru.itis.travelling.presentation.addtrip.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.itis.travelling.presentation.base.navigation.Navigator
import javax.inject.Inject

@HiltViewModel
class AddTripViewModel @Inject constructor(
    private val navigator: Navigator
) : ViewModel() {


//    fun showAddTripBottomSheet(phone: String) {
//        phoneNumber = phone
//        viewModelScope.launch {
//            navigator.showAddTripBottomSheet(phone)
//        }
//    }

    fun navigateToTrips(phoneNumber: String) {
        viewModelScope.launch {
            navigator.navigateToTripsFragment(phoneNumber)
        }
    }
}