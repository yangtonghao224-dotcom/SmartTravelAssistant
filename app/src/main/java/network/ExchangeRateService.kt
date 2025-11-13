package com.example.smarttravelassistant.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ExchangeRateService {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: ExchangeRateApi by lazy {
        Retrofit.Builder()

            .baseUrl("https://api.frankfurter.app/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ExchangeRateApi::class.java)
    }
}











