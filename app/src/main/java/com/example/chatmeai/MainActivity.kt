package com.example.chatmeai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.chatmeai.databinding.ActivityMainBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)


        binding.send.setOnClickListener {
            val question = binding.questionText.text.toString()
            binding.lottie.visibility = View.VISIBLE
            binding.lottie.playAnimation()
            getResponse(question){response ->
                runOnUiThread{
                    binding.lottie.visibility = View.GONE
                    binding.lottie.pauseAnimation()// Hide the Lottie animation view
                    binding.responseText.text = response
                }
            }
        }

        setContentView(binding.root)
    }


    fun getResponse(question: String, callback: (String) -> Unit){
        val api_key = "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"
        val url = "https://api.openai.com/v1/completions"

        val requestBody = """
            {
            "model": "text-davinci-003",
            "prompt": "$question",
            "max_tokens": 500,
            "temperature": 0
            }
        """.trimIndent()


        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $api_key")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()



        client.newCall(request).enqueue(object  : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Fail", " Api Failed")
                runOnUiThread {
                    binding.lottie.visibility = View.GONE
                    binding.lottie.pauseAnimation()
                    Toast.makeText(this@MainActivity,"I didnt understand , can you ask again ?", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null) {
                    Log.v("data", body)
                    try {
                        val jsonObject = JSONObject(body)
                        if (jsonObject.has("choices")) {
                            val jsonArray: JSONArray = jsonObject.getJSONArray("choices")
                            if (jsonArray.length() > 0) {
                                val textResult = jsonArray.getJSONObject(0).getString("text")
                                callback(textResult)
                                return
                            }
                        }
                    } catch (e: JSONException) {
                        Log.e("JSONException", e.message, e)
                    }
                }
                runOnUiThread {
                    binding.lottie.visibility = View.GONE
                    binding.lottie.pauseAnimation()
                    Toast.makeText(this@MainActivity, "I didn't understand, can you ask again? (dont use symbols)", Toast.LENGTH_LONG).show()
                }
            }


        })
    }
}