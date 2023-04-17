package com.fiqsky.excryptoapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApiService {

    @GET("api/v3/simple/price")
    suspend fun getSimplePrice(
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrencies: String
    ): Map<String, Map<String, Double>>

    companion object {
        private const val BASE_URL = "https://api.coingecko.com/"

        fun create(): CoinGeckoApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CoinGeckoApiService::class.java)
        }
    }
}
