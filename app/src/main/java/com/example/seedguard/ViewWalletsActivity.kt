package com.example.seedguard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ViewWalletsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_seedphrases)

        val sharedPreferences = getSharedPreferences("SeedGuardPrefs", Context.MODE_PRIVATE)
        val allWallets = sharedPreferences.all
        val walletListContainer = findViewById<LinearLayout>(R.id.walletListContainer)

        if (allWallets.isEmpty()) {
            Toast.makeText(this, "No wallets found.", Toast.LENGTH_SHORT).show()
            return
        }

        for ((walletName, _) in allWallets) {
            val walletButton = Button(this).apply {
                text = walletName
                setOnClickListener {
                    val intent = Intent(this@ViewWalletsActivity, ViewSeedPhrasesActivity::class.java)
                    intent.putExtra("WALLET_NAME", walletName)
                    startActivity(intent)
                }
            }
            walletListContainer.addView(walletButton)
        }
    }
}
