package com.example.seedguard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        auth = Firebase.auth

        val resendButton = findViewById<Button>(R.id.resendButton)
        val checkVerifiedButton = findViewById<Button>(R.id.checkVerifiedButton)

        resendButton.setOnClickListener {
            resendVerificationEmail()
        }

        checkVerifiedButton.setOnClickListener {
            checkEmailVerificationStatus()
        }

        checkEmailVerificationStatus()
    }

    private fun resendVerificationEmail() {
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this, "Failed to resend: ${task.exception?.message}", Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    private fun checkEmailVerificationStatus() {
        auth.currentUser?.reload()?.addOnCompleteListener { reloadTask ->
            if (reloadTask.isSuccessful) {
                val user = auth.currentUser
                if (user?.isEmailVerified == true) {
                    Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainMenu::class.java).apply {
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Email not verified yet. Please check your inbox.", Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "Error checking verification status", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
