package com.example.seedguard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_sign_in)

        auth = FirebaseAuth.getInstance()

        val nameField = findViewById<EditText>(R.id.editTextName)
        val emailField = findViewById<EditText>(R.id.editTextEmailAddress)
        val passwordField = findViewById<EditText>(R.id.editTextPassword)
        val confirmPasswordField = findViewById<EditText>(R.id.editTextConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val btnForgotPassword = findViewById<Button>(R.id.btnForgotPW)

        btnSignUp.setOnClickListener {
            val name = nameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createAccount(email, password, name)
        }

        btnSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        btnForgotPassword.setOnClickListener {
            val intent = Intent(this, PasswordResetActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createAccount(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user != null) {
                        sendEmailVerification(user)
                        saveUserDetails(user.uid, name, email)
                    }
                } else {
                    Toast.makeText(this, "Sign-up failed: An account with this email address already exists, please use another.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun sendEmailVerification(user: com.google.firebase.auth.FirebaseUser) {
        user.sendEmailVerification()
            .addOnSuccessListener {
                Toast.makeText(this, "Verification email sent. Please verify before logging in.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send verification email: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserDetails(userId: String, name: String, email: String) {
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "userId" to userId
        )

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "User details added successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding user details", e)
            }
    }


    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
