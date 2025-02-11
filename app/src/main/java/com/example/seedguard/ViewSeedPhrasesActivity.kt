package com.example.seedguard

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class ViewSeedPhrasesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_seed_phrases)

        val walletNameTextView = findViewById<TextView>(R.id.walletNameTextView)
        val seedPhrasesTextView = findViewById<TextView>(R.id.seedPhrasesTextView)
        val backButton: Button = findViewById(R.id.backButton)

        // Set up the back button to return to the previous screen
        backButton.setOnClickListener {
            finish()
        }

        // Get the current user ID from FirebaseAuth
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not signed in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the selected wallet name
        val walletName = intent.getStringExtra("WALLET_NAME")
        if (walletName.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid wallet selection.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("ViewSeedPhrasesActivity", "Fetching seed phrases for Wallet: $walletName, User ID: $userId")

        walletNameTextView.text = "Wallet: $walletName"

        // Fetch seed phrases from Firestore for this specific wallet
        val db = FirebaseFirestore.getInstance()
        db.collection("seedPhrases")
            .whereEqualTo("userId", userId)
            .whereEqualTo("walletName", walletName)  // Ensure we only fetch the selected wallet
            .get(Source.SERVER)  // Fetch fresh data
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Toast.makeText(this, "No seed phrases found for this wallet.", Toast.LENGTH_SHORT).show()
                    Log.w("ViewSeedPhrasesActivity", "No seed phrases found for Wallet: $walletName, User ID: $userId")
                    return@addOnSuccessListener
                }

                val seedPhrasesBuilder = StringBuilder()
                for (document in querySnapshot.documents) {
                    val seedPhrasesList = document.get("seedPhrases") as? List<String>
                    val seedPhrasesText = seedPhrasesList?.mapIndexed { index, word ->
                        "${index + 1}. $word"
                    }?.joinToString("\n") ?: "No seed phrases found"

                    seedPhrasesBuilder.append("$seedPhrasesText\n\n")


                }

                seedPhrasesTextView.text = seedPhrasesBuilder.toString()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching seed phrases: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewSeedPhrasesActivity", "Query failed: ${exception.message}")
            }
    }
}
