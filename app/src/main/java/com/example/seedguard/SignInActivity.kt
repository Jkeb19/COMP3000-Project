package com.example.seedguard

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
//import androidx.media3.common.util.Log
//import androidx.media3.common.util.UnstableApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.MultiFactorResolver
import com.google.firebase.auth.PhoneMultiFactorInfo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "SignInActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = Firebase.auth

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val signInButton = findViewById<Button>(R.id.signInButton)

        signInButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || !isValidEmail(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signInWithEmailAndPassword(email, password)
        }
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && !user.isEmailVerified) {
                        Log.d(TAG, "Email not verified for user: ${user.uid}")
                        Toast.makeText(this, "Please verify your email first", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, EmailVerificationActivity::class.java).apply {
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        handleMultiFactorAuthentication(user)
                    }
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthMultiFactorException) {
                        val resolver = exception.resolver
                        startMultiFactorAuthentication(resolver)
                    } else {
                        Toast.makeText(this, "Sign-in failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun startMultiFactorAuthentication(resolver: MultiFactorResolver) {
        val multiFactorHint = resolver.hints.find { it is PhoneMultiFactorInfo } as? PhoneMultiFactorInfo
        if (multiFactorHint != null) {
            Log.d(TAG, "Starting SMS Multi-Factor Authentication for: ${multiFactorHint.phoneNumber}")

            val intent = Intent(this, SmsVerificationActivity::class.java).apply {
                putExtra("multiFactorResolver", resolver)
                putExtra("phoneNumber", multiFactorHint.phoneNumber)
            }
            startActivity(intent)
            finish()
        } else {
            Log.e(TAG, "No supported second factor found!")
            Toast.makeText(this, "A supported second factor is required but not found.", Toast.LENGTH_LONG).show()
        }
    }



    private fun handleMultiFactorAuthentication(user: FirebaseUser?) {
        user?.let {
            Log.d(TAG, "User found: ${it.uid}")
            Log.d(TAG, "Enrolled factors: ${it.multiFactor.enrolledFactors}")
            if (it.multiFactor.enrolledFactors.isNotEmpty()) {
                for (factor in it.multiFactor.enrolledFactors) {
                    if (factor is PhoneMultiFactorInfo) {
                        Log.d(TAG, "SMS factor found: ${factor.phoneNumber}")
                        val intent = Intent(this, SmsVerificationActivity::class.java)
                        intent.putExtra("phoneNumber", factor.phoneNumber)
                        startActivity(intent)
                        finish()
                        return
                    } else {
                        Log.d(TAG, "Non-SMS factor found: ${factor.factorId}")
                    }
                }

            } else {
                Log.d(TAG, "No multi-factor authentication enrolled")
                Toast.makeText(this, "Sign-in successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainMenu::class.java)
                startActivity(intent)
                finish()
            }
        } ?: run {
            Log.w(TAG, "User is null")
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        }
    }


    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
