package com.example.seedguard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddNewSeedPhraseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_seedphrases)

        val walletNameEditText: EditText = findViewById(R.id.editTextText)
        val button12: Button = findViewById(R.id.button4)
        val button18: Button = findViewById(R.id.button5)
        val button24: Button = findViewById(R.id.button6)

        button12.setOnClickListener {
            navigateToSeedPhraseActivity(walletNameEditText.text.toString(), 12)
        }

        button18.setOnClickListener {
            navigateToSeedPhraseActivity(walletNameEditText.text.toString(), 18)
        }

        button24.setOnClickListener {
            navigateToSeedPhraseActivity(walletNameEditText.text.toString(), 24)
        }
    }

    /**
     * Navigates to SeedPhraseActivity with the provided wallet name and seed phrase count.
     */
    private fun navigateToSeedPhraseActivity(walletName: String, seedPhraseCount: Int) {
        val intent = Intent(this, SeedPhraseActivity::class.java)

        intent.putExtra("WALLET_NAME", walletName)
        intent.putExtra("SEED_PHRASE_COUNT", seedPhraseCount)
        startActivity(intent)
    }
}
