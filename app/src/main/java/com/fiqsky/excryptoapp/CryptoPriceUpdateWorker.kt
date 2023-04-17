package com.fiqsky.excryptoapp

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CryptoPriceUpdateWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val coinGeckoApiService = CoinGeckoApiService.create()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch the data and update the widget
                val simplePrice = coinGeckoApiService.getSimplePrice("bitcoin,ethereum", "usd")
                updateCryptoPriceWidget(simplePrice)

                // If successful, return Result.success()
                Result.success()
            } catch (e: Exception) {
                // If there is an error, return Result.retry()
                Result.retry()
            }
        }
    }

    private fun updateCryptoPriceWidget(simplePrice: Map<String, Map<String, Double>>) {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val thisAppWidget = ComponentName(applicationContext.packageName, CryptoPriceAppWidget::class.java.name)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)

        Log.d("CryptoPriceUpdateWorker", "Updating CryptoPriceAppWidget with simplePrice: $simplePrice")

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(applicationContext.packageName, R.layout.crypto_price_widget)
            views.setTextViewText(R.id.bitcoin_price_widget_text, "Bitcoin Price: $${simplePrice["bitcoin"]?.get("usd")}")
            views.setTextViewText(R.id.ethereum_price_widget_text, "Ethereum Price: $${simplePrice["ethereum"]?.get("usd")}")

            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d("CryptoPriceUpdateWorker", "Updated CryptoPriceAppWidget (appWidgetId: $appWidgetId) with simplePrice: $simplePrice")
        }
    }
}
