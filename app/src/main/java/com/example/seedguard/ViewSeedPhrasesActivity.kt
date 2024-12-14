package com.example.seedguard

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ViewSeedPhrasesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_seed_phrases)

        val walletName = intent.getStringExtra("WALLET_NAME") ?: return
        val sharedPreferences = getSharedPreferences("SeedGuardPrefs", Context.MODE_PRIVATE)
        val seedPhrases = sharedPreferences.getString(walletName, null)

        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }


        if (seedPhrases == null) {
            Toast.makeText(this, "No seed phrases found for $walletName.", Toast.LENGTH_SHORT).show()
            return
        }

        val walletNameTextView = findViewById<TextView>(R.id.walletNameTextView)
        val seedPhrasesTextView = findViewById<TextView>(R.id.seedPhrasesTextView)

        walletNameTextView.text = walletName
        seedPhrasesTextView.text = seedPhrases.replace(",", "\n")
    }
}
