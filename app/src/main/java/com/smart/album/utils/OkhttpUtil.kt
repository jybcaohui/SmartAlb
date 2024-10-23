package com.smart.album.utils

import android.app.Activity
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object OkhttpUtil {

    fun makeSimpleGetRequest(context: Activity, url: String, onResponse: (String?) -> Unit) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                context.runOnUiThread {
                    onResponse(null)
                    println("Error making network request. ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                context.runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        onResponse(responseBody)
                    } else {
                        onResponse(null)
                        println("Network request failed with code ${response.code}")
                    }
                }
            }
        })
    }
}