package com.fiqsky.excryptoapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val coinGeckoApiService = CoinGeckoApiService.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updatePricePeriodically()

        // Schedule the CryptoPriceUpdateWorker
        scheduleCryptoPriceUpdate()
    }

    private fun updatePricePeriodically() {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val simplePrice = fetchSimplePrice("bitcoin,ethereum", "usd")
                updatePrice(simplePrice)
                delay(20000) // Wait for 20 seconds before updating the price again
            }
        }
    }

    private suspend fun fetchSimplePrice(ids: String, vsCurrencies: String): Map<String, Map<String, Double>> {
        return withContext(Dispatchers.IO) {
            coinGeckoApiService.getSimplePrice(ids, vsCurrencies)
        }
    }

    private fun updatePrice(simplePrice: Map<String, Map<String, Double>>) {
        val bitcoin_price_text = findViewById<TextView>(R.id.bitcoin_price_text)
        val ethereum_price_text = findViewById<TextView>(R.id.ethereum_price_text)
        bitcoin_price_text.text = "Bitcoin Price: $${simplePrice["bitcoin"]?.get("usd")?.toString() ?: "N/A"}"
        ethereum_price_text.text = "Ethereum Price: $${simplePrice["ethereum"]?.get("usd")?.toString() ?: "N/A"}"
    }

    fun scheduleCryptoPriceUpdate() {
        val workRequest = PeriodicWorkRequestBuilder<CryptoPriceUpdateWorker>(20, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            "cryptoPriceUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
