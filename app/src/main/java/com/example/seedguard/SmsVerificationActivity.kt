package com.example.seedguard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class SmsVerificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private lateinit var smsCodeEditText: EditText
    private lateinit var verifySmsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_verification)

        auth = Firebase.auth
        smsCodeEditText = findViewById(R.id.smsCodeEditText)
        verifySmsButton = findViewById(R.id.verifySmsButton)

        verificationId = intent.getStringExtra("verificationId") ?: ""

        verifySmsButton.setOnClickListener {
            val smsCode = smsCodeEditText.text.toString().trim()
            if (smsCode.isNotEmpty()) {
                verifySmsCode(smsCode)
            } else {
                Toast.makeText(this, "Please enter the SMS code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifySmsCode(smsCode: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
        auth.currentUser?.linkWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "SMS verification successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainMenu::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "SMS verification failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
