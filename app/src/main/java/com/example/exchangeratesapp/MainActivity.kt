package com.example.exchangeratesapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.util.Locale

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val amountText = findViewById<EditText>(R.id.amountText)
        val currencySpinner = findViewById<Spinner>(R.id.currencySpinner)
        val convertButton = findViewById<Button>(R.id.convertButton)
        val resultView = findViewById<TextView>(R.id.resultView)

        CoroutineScope(Dispatchers.IO).launch {
            val symbols = getCurrencySymbols()
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@MainActivity, R.layout.spinner_item, symbols)
                currencySpinner.adapter = adapter
            }
        }

        convertButton.setOnClickListener {
            val amountInput = amountText.text.toString()
            if (amountInput.isNotEmpty()) {
                val amount = amountInput.toDouble()
                val currency = currencySpinner.selectedItem.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    val rate = getCurrencyRate(currency)
                    val result = amount * rate
                    withContext(Dispatchers.Main) {
                        resultView.text = String.format(Locale.US, "%.2f", result)
                    }
                }
            }
        }
    }

    private fun getCurrencySymbols(): List<String> {
        val url = URL("http://data.fixer.io/api/symbols?access_key=120e92d45bf1a5c3ee314b0c8581a973")
        val json = JSONObject(url.readText())
        return json.getJSONObject("symbols").keys().asSequence().toList()
    }

    private fun getCurrencyRate(currency: String): Double {
        val url = URL("http://data.fixer.io/api/latest?access_key=120e92d45bf1a5c3ee314b0c8581a973&symbols=$currency")
        val json = JSONObject(url.readText())
        return json.getJSONObject("rates").let { it.getDouble(it.keys().next()) }
    }
}
