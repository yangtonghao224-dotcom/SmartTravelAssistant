package com.example.smarttravelassistant.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException


object ExchangeRateRepository {

    private const val PREF_NAME = "exchange_cache"

    suspend fun getRate(
        context: Context,
        base: String,
        target: String
    ): Result<Pair<Double, Boolean>> = withContext(Dispatchers.IO) {
        try {
            val response = ExchangeRateService.api.getLatest(base, target)
            val rate = response.rates[target]

            if (rate != null) {
                cacheRate(context, base, target, rate)
                Result.success(rate to false)        // fromCache = false
            } else {

                val cached = getCachedRate(context, base, target)
                if (cached != null) {
                    Result.success(cached to true)
                } else {
                    Result.failure(IllegalStateException("Rate not found"))
                }
            }
        } catch (e: IOException) {

            val cached = getCachedRate(context, base, target)
            if (cached != null) {
                Result.success(cached to true)
            } else {
                Result.failure(e)
            }
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun cacheRate(context: Context, base: String, target: String, rate: Double) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val key = "${base}_$target"
        prefs.edit()
            .putString("${key}_value", rate.toString())
            .putLong("${key}_time", System.currentTimeMillis())
            .apply()
    }

    private fun getCachedRate(context: Context, base: String, target: String): Double? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val key = "${base}_$target"
        val value = prefs.getString("${key}_value", null)
        return value?.toDoubleOrNull()
    }
}

