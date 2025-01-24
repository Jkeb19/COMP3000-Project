package com.example.seedguard

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_sign_in)

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val db = FirebaseFirestore.getInstance()


        val nameField = findViewById<EditText>(R.id.editTextName)
        val emailField = findViewById<EditText>(R.id.editTextEmailAddress)
        val passwordField = findViewById<EditText>(R.id.editTextPassword)
        val confirmPasswordField = findViewById<EditText>(R.id.editTextConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnSignIn =findViewById<Button>(R.id.btnSignIn)
        val btnForgotPass = findViewById<Button>(R.id.btnForgotPW)

        btnSignUp.setOnClickListener {
            val name = nameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else if (!isValidEmail(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            } else {
                // Create a user with Firebase Authentication
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Get the generated userId from Firebase Authentication
                            val userId = FirebaseAuth.getInstance().currentUser?.uid

                            if (userId != null) {
                                saveUserDetails(userId, name, email, password)
                                Toast.makeText(this, "User signed up successfully!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainMenu::class.java)
                                startActivity(intent)
                                finish() // Close the sign-up screen
                            } else {
                                Toast.makeText(this, "Failed to retrieve user ID", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        btnSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        btnForgotPass.setOnClickListener {  }

        //autoFillCredentials()
    }
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }


    fun saveUserDetails(userId: String, name: String, email: String, password: String) {
        val db = FirebaseFirestore.getInstance()

        val hashedPassword = hashPassword(password)

        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "password" to hashedPassword,
            "userId" to userId // Include the userId in the Firestore document
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

//    private fun autoFillCredentials() {
//        val savedName = sharedPreferences.getString("NAME", null)
//        val savedEmail = sharedPreferences.getString("EMAIL", null)
//        val savedPassword = sharedPreferences.getString("PASSWORD", null)
//
//        if (!savedName.isNullOrEmpty() && !savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
//            findViewById<EditText>(R.id.editTextName).setText(savedName)
//            findViewById<EditText>(R.id.editTextEmailAddress).setText(savedEmail)
//            findViewById<EditText>(R.id.editTextPassword).setText(savedPassword)
//            findViewById<EditText>(R.id.editTextConfirmPassword).setText(savedPassword)
//        }
//    }
}
