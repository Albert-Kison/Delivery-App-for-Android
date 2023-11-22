package com.griffith.deliveryapp

import androidx.compose.runtime.mutableStateOf

class Coordinates private constructor() {
    private val latitude = mutableStateOf(0.0)
    private val longitude = mutableStateOf(0.0)

    fun getLatitude(): Double {
        return latitude.value
    }

    fun getLongitude(): Double {
        return longitude.value
    }

    fun setLatitude(newLatitude: Double) {
        latitude.value = newLatitude
    }

    fun setLongitude(newLongitude: Double) {
        longitude.value = newLongitude
    }

    companion object {
        @Volatile
        private var instance: Coordinates? = null

        fun getInstance(): Coordinates {
            return instance ?: synchronized(this) {
                instance ?: Coordinates().also { instance = it }
            }
        }
    }
}