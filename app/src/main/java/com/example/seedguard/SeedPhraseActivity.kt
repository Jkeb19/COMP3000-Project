package com.example.seedguard

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SeedPhraseActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: seedphraseadapter
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seedphrase_entry)

        sharedPreferences = getSharedPreferences("SeedGuardPrefs", Context.MODE_PRIVATE)

        val seedPhraseCount = intent.getIntExtra("SEED_PHRASE_COUNT", 12)
        val walletName = intent.getStringExtra("WALLET_NAME") ?: "Unknown Wallet"

        recyclerView = findViewById(R.id.recyclerViewSeedPhrases)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = seedphraseadapter(seedPhraseCount)
        recyclerView.adapter = adapter

        val buttonSubmit: Button = findViewById(R.id.buttonSubmitSeedPhrase)
        buttonSubmit.setOnClickListener {
            val seedPhrases = adapter.getSeedPhrases()

            if (seedPhrases.any { it.isBlank() }) {
                Toast.makeText(this, "Please fill in all seed phrases.", Toast.LENGTH_SHORT).show()
            } else {
                saveToLocalStorage(walletName, seedPhrases)

                Toast.makeText(this, "Seed phrases saved successfully for $walletName!", Toast.LENGTH_LONG).show()

                finish()
            }
        }
    }

    private fun saveToLocalStorage(walletName: String, seedPhrases: List<String>) {
        val editor = sharedPreferences.edit()

        val seedPhrasesString = seedPhrases.joinToString(",")
        editor.putString(walletName, seedPhrasesString)
        editor.apply()
    }
}
