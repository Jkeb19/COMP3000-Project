package com.example.seedguard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneMultiFactorInfo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainMenu : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var enableSmsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        auth = Firebase.auth
        enableSmsButton = findViewById(R.id.button3)

        val viewSavedButton: Button = findViewById(R.id.button)
        val addNewButton: Button = findViewById(R.id.button2)
        val deleteAccountButton: Button = findViewById(R.id.button4)

        viewSavedButton.setOnClickListener {
            startActivity(Intent(this, ViewWalletsActivity::class.java))
        }

        addNewButton.setOnClickListener {
            startActivity(Intent(this, AddNewSeedPhraseActivity::class.java))
        }

        deleteAccountButton.setOnClickListener {
            startActivity(Intent(this, DeleteAccountActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        checkSmsEnrollmentStatus(auth.currentUser)
    }

    private fun checkSmsEnrollmentStatus(user: FirebaseUser?) {
        user?.let {
            if (it.multiFactor.enrolledFactors.isNotEmpty()) {
                for (factor in it.multiFactor.enrolledFactors) {
                    if (factor is PhoneMultiFactorInfo) {
                        enableSmsButton.visibility = Button.GONE
                    } else {
                        enableSmsButton.visibility = Button.VISIBLE
                        enableSmsButton.setOnClickListener {
                            startActivity(Intent(this, PhoneVerificationActivity::class.java))
                        }
                    }
                }
            } else {
                enableSmsButton.visibility = Button.VISIBLE
                enableSmsButton.setOnClickListener {
                    startActivity(Intent(this, PhoneVerificationActivity::class.java))
                }


            }
        } ?: run {
            enableSmsButton.visibility = Button.VISIBLE
        }
    }
}