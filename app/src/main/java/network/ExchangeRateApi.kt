package com.example.smarttravelassistant.network

import retrofit2.http.GET
import retrofit2.http.Query


data class ExchangeRateResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

interface ExchangeRateApi {

    @GET("latest")
    suspend fun getLatest(

        @Query("from") base: String,
        @Query("to") symbols: String
    ): ExchangeRateResponse
}






