package com.example.seedguard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class ViewWalletsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_seedphrases)

        val walletListContainer = findViewById<LinearLayout>(R.id.walletListContainer)

        // Get logged-in user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not signed in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch wallets from the "seedphrase" collection where userid matches
        val db = FirebaseFirestore.getInstance()

        // Fetch wallets associated with the user
        db.collection("seedPhrases")
            .whereEqualTo("userId", userId)
            .get(Source.SERVER)  // Ensures we fetch fresh data, not cached
            .addOnSuccessListener { querySnapshot ->
                Log.d("ViewWalletsActivity", "Documents found: ${querySnapshot.size()}")

                if (querySnapshot.isEmpty) {
                    Toast.makeText(this, "No wallets found.", Toast.LENGTH_SHORT).show()
                    Log.w("ViewWalletsActivity", "No wallets found for user $userId")
                    return@addOnSuccessListener
                }

                for (document in querySnapshot.documents) {
                    val walletName = document.getString("walletName") ?: "Unnamed Wallet"

                    Log.d("ViewWalletsActivity", "Wallet found: $walletName, Data: ${document.data}")

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
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching wallets: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewWalletsActivity", "Query failed: ${exception.message}")
            }
    }
}
