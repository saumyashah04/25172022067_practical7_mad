package com.example.a25172022067_practical7_mad.network

import android.content.Context
import android.util.Log
import com.example.a25172022067_practical7_mad.model.Person
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object ApiService {

    private const val TAG = "ApiService"

    // Primary and mirror API endpoints for person data
    private const val PRIMARY_API_URL = "https://raw.githubusercontent.com/devrsh-data/api/main/persons.json"
    private const val FALLBACK_API_URL = "https://mocki.io/v1/d4867d8b-b5d5-4a48-a4ab-79131b5809b8"

    data class FetchResult(
        val persons: List<Person>,
        val isFromNetwork: Boolean,
        val message: String
    )

    /**
     * Retrieves person data in JSON format from an internet API endpoint.
     * Falls back to local bundled JSON asset if network is unreachable.
     */
    fun fetchPersons(context: Context): FetchResult {
        // Attempt network fetch from primary API
        var jsonString: String? = downloadJson(PRIMARY_API_URL)

        // If primary fails, attempt mirror API
        if (jsonString == null) {
            jsonString = downloadJson(FALLBACK_API_URL)
        }

        if (jsonString != null) {
            val list = parseJson(jsonString)
            if (list.isNotEmpty()) {
                Log.d(TAG, "Successfully fetched ${list.size} persons from Internet API")
                return FetchResult(list, isFromNetwork = true, "Retrieved ${list.size} records from Internet API")
            }
        }

        // Fallback to local asset if internet is not accessible
        Log.w(TAG, "Internet API unreachable or offline. Falling back to bundled assets.")
        val fallbackJson = loadJsonFromAsset(context, "persons.json")
        val fallbackList = if (fallbackJson != null) parseJson(fallbackJson) else emptyList()
        return FetchResult(
            fallbackList,
            isFromNetwork = false,
            "Loaded ${fallbackList.size} records (Offline fallback)"
        )
    }

    private fun downloadJson(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.setRequestProperty("Accept", "application/json")
            connection.doInput = true

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                readStream(connection.inputStream)
            } else {
                Log.e(TAG, "HTTP error $responseCode from $urlString")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch from API $urlString: ${e.message}")
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun loadJsonFromAsset(context: Context, filename: String): String? {
        return try {
            val inputStream = context.assets.open(filename)
            readStream(inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading asset $filename: ${e.message}")
            null
        }
    }

    private fun readStream(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line).append('\n')
        }
        reader.close()
        return sb.toString()
    }

    fun parseJson(jsonString: String): List<Person> {
        val list = mutableListOf<Person>()
        try {
            val jsonArray = JSONArray(jsonString)
            val limit = minOf(jsonArray.length(), 4)
            for (i in 0 until limit) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.optString("name", "")
                val phone = obj.optString("phone", "")
                val email = obj.optString("email", "")
                val address = obj.optString("address", "")
                list.add(Person(name = name, phone = phone, email = email, address = address))
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON parsing error: ${e.message}")
        }
        return list
    }
}
