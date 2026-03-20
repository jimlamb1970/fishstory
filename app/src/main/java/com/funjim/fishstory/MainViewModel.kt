package com.funjim.fishstory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.funjim.fishstory.database.TripDao

//class MainViewModel(val tripDao: TripDao) : ViewModel() {
//    val allTrips = tripDao.getAllTrips()
//}

//class MainViewModelFactory(private val tripDao: TripDao) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return MainViewModel(tripDao) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
