package com.example.seedguard

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class ViewSeedPhrasesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var walletName: String
    private lateinit var seedPhrasesList: MutableList<String>
    private lateinit var seedPhrasesContainer: LinearLayout
    private lateinit var documentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_seed_phrases)

        val walletNameTextView = findViewById<TextView>(R.id.walletNameTextView)
        seedPhrasesContainer = findViewById(R.id.seedPhrasesTextView)
        val backButton: Button = findViewById(R.id.backButton)
        val editButton: Button = findViewById(R.id.editButton)
        val deleteButton: Button = findViewById(R.id.deleteButton)

        db = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        documentId = intent.getStringExtra("DOCUMENT_ID") ?: return
        walletName = intent.getStringExtra("WALLET_NAME") ?: return
        walletNameTextView.text = "Wallet: $walletName"

        fetchSeedPhrases()

        backButton.setOnClickListener { finish() }
        editButton.setOnClickListener { showEditDialog() }
        deleteButton.setOnClickListener { showDeleteConfirmation() }
    }

    private fun fetchSeedPhrases() {
        db.collection("seedPhrases")
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Toast.makeText(this, "Wallet not found.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                seedPhrasesList = document.get("seedPhrases") as? MutableList<String> ?: mutableListOf()
                updateSeedPhrasesDisplay()
            }
            .addOnFailureListener { exception ->
                Log.e("ViewSeedPhrasesActivity", "Error fetching seed phrases: ${exception.message}")
            }
    }
    private fun updateSeedPhrasesDisplay() {
        seedPhrasesContainer.removeAllViews()

        seedPhrasesList.forEachIndexed { index, word ->
            val textView = TextView(this)
            textView.text = "${index + 1}. $word"
            textView.textSize = 16f
            textView.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            seedPhrasesContainer.addView(textView)
        }
    }

    private fun showEditDialog() {
        if (!::seedPhrasesList.isInitialized) {
            Toast.makeText(this, "Error: Seed phrases not loaded yet.", Toast.LENGTH_SHORT).show()
            return
        }

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(20, 20, 20, 20)

        val inputFields = mutableListOf<EditText>()

        seedPhrasesList.forEachIndexed { index, word ->
            val editText = EditText(this)
            editText.setText(word)
            editText.hint = "Word ${index + 1}"
            editText.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
                if (source.matches(Regex("^[a-z]+$"))) source else ""
            })
            editText.background = getDrawable(android.R.drawable.editbox_background)
            layout.addView(editText)
            inputFields.add(editText)
        }

        scrollView.addView(layout)

        AlertDialog.Builder(this)
            .setTitle("Edit Seed Phrases")
            .setView(scrollView)
            .setPositiveButton("Save") { _, _ ->
                val updatedSeedPhrases = inputFields.map { it.text.toString().trim() }
                if (updatedSeedPhrases.any { it.isEmpty() }) {
                    Toast.makeText(this, "Seed phrases cannot be left empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                updateSeedPhrases(updatedSeedPhrases)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateSeedPhrases(updatedSeedPhrases: List<String>) {
        db.collection("seedPhrases")
            .document(documentId)
            .update("seedPhrases", updatedSeedPhrases)
            .addOnSuccessListener {
                Toast.makeText(this, "Seed phrases updated!", Toast.LENGTH_SHORT).show()
                seedPhrasesList = updatedSeedPhrases.toMutableList()
                updateSeedPhrasesDisplay()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Wallet")
            .setMessage("Are you sure you want to delete this wallet? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteWallet() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteWallet() {
        db.collection("seedPhrases")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Wallet deleted!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete wallet.", Toast.LENGTH_SHORT).show()
            }
    }
}

