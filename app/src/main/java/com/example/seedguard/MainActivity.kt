package com.example.seedguard

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

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
                saveCredentials(name, email, password)
                val intent = Intent(this, MainMenu::class.java)
                startActivity(intent)
            }
        }
        btnSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        btnForgotPass.setOnClickListener {  }

        autoFillCredentials()
    }

    private fun saveCredentials(name: String, email: String, password: String) {
        val editor = sharedPreferences.edit()
        editor.putString("NAME", name)
        editor.putString("EMAIL", email)
        editor.putString("PASSWORD", password)
        editor.apply()

        Toast.makeText(this, "User details saved successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun autoFillCredentials() {
        val savedName = sharedPreferences.getString("NAME", null)
        val savedEmail = sharedPreferences.getString("EMAIL", null)
        val savedPassword = sharedPreferences.getString("PASSWORD", null)

        if (!savedName.isNullOrEmpty() && !savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            findViewById<EditText>(R.id.editTextName).setText(savedName)
            findViewById<EditText>(R.id.editTextEmailAddress).setText(savedEmail)
            findViewById<EditText>(R.id.editTextPassword).setText(savedPassword)
            findViewById<EditText>(R.id.editTextConfirmPassword).setText(savedPassword)
        }
    }
}
