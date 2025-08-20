package org.example.predictionapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class MainActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var btnCheck: Button
    private lateinit var txtResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tabLayout)
        btnCheck = findViewById(R.id.btnCheck)
        txtResult = findViewById(R.id.txtResult)

        tabLayout.addTab(tabLayout.newTab().setText("MZPLAY"))
        tabLayout.addTab(tabLayout.newTab().setText("MYSGAMES"))

        btnCheck.setOnClickListener {
            when (tabLayout.selectedTabPosition) {
                0 -> fetchMzplay()
                1 -> fetchMysgames()
                else -> Toast.makeText(this, "Unknown tab", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ----- API layer -----

    interface GameService {
        @POST("api/webapi/GetGameIssue")
        fun getGameIssue(@Body body: Map<String, Any?> = mapOf("type" to 1)): Call<JsonObject>

        @POST("api/webapi/GetNoaverageEmerdList")
        fun getHistory(@Body body: Map<String, Any?> = mapOf("page" to 1, "pageSize" to 1, "type" to 1)): Call<JsonObject>
    }

    private fun retrofit(baseUrl: String): Retrofit {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    private fun fetchMzplay() {
        val service = retrofit("https://mzplayapi.com/").create(GameService::class.java)
        service.getHistory().enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val number = parseLastDigit(response.body())
                if (number == null) {
                    txtResult.text = "Parse error"
                    return
                }
                val result = if (number < 5) "Win $number" else "Lose $number"
                txtResult.text = "MZPLAY → $result"
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                txtResult.text = "Error: ${t.message}"
            }
        })
    }

    private fun fetchMysgames() {
        val service = retrofit("https://newapi.9lottery.cc/").create(GameService::class.java)
        service.getHistory().enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val number = parseLastDigit(response.body())
                if (number == null) {
                    txtResult.text = "Parse error"
                    return
                }
                val result = if (number >= 5) "Win $number" else "Lose $number"
                txtResult.text = "MYSGAMES → $result"
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                txtResult.text = "Error: ${t.message}"
            }
        })
    }

    // Attempts to extract a last digit from common fields found in the decompiled APK
    private fun parseLastDigit(json: JsonObject?): Int? {
        if (json == null) return null
        // Potential shapes: {"data":{"list":[{...}]}} or {"data":[{...}]} etc.
        fun findFirstItem(obj: JsonObject): JsonObject? {
            // look for arrays under common keys
            val keys = listOf("data", "result", "records", "list", "items")
            for (k in keys) {
                if (obj.has(k)) {
                    val el = obj.get(k)
                    if (el.isJsonArray && el.asJsonArray.size() > 0) {
                        val first = el.asJsonArray[0]
                        if (first.isJsonObject) return first.asJsonObject
                    } else if (el.isJsonObject) {
                        // recurse one level
                        val nested = findFirstItem(el.asJsonObject)
                        if (nested != null) return nested
                    }
                }
            }
            return null
        }

        val item = findFirstItem(json) ?: return null
        val fields = listOf("number", "num", "result", "openCode", "opencode", "open_code")
        for (f in fields) {
            if (item.has(f)) {
                val v: JsonElement = item.get(f)
                if (v.isJsonPrimitive) {
                    val s = v.asString
                    val digits = s.filter { it.isDigit() }
                    if (digits.isNotEmpty()) return digits.last().digitToInt()
                }
            }
        }
        return null
    }
}
