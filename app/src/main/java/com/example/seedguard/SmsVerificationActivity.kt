package com.example.seedguard

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private lateinit var resolver: MultiFactorResolver
    private lateinit var smsCodeEditText: EditText
    private lateinit var verifySmsButton: Button
    private var verificationId: String? = null
    private val TAG = "SmsVerificationActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_verification)

        auth = Firebase.auth
        smsCodeEditText = findViewById(R.id.smsCodeEditText)
        verifySmsButton = findViewById(R.id.verifySmsButton)

        resolver = intent.getParcelableExtra("multiFactorResolver")!!

        verifySmsButton.setOnClickListener {
            val smsCode = smsCodeEditText.text.toString().trim()
            if (smsCode.isNotEmpty()) {
                verifySmsCode(smsCode)
            } else {
                Toast.makeText(this, "Please enter the SMS code", Toast.LENGTH_SHORT).show()
            }
        }

        sendVerificationCode()
    }

    private fun sendVerificationCode() {
        val phoneFactor = resolver.hints.find { it is PhoneMultiFactorInfo } as? PhoneMultiFactorInfo
        if (phoneFactor != null) {
            Log.d(TAG, "Sending verification code to: ${phoneFactor.phoneNumber}")

            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(TAG, "Auto verification completed")
                    verifyWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e(TAG, "Verification failed: ${e.message}", e)
                    Toast.makeText(this@SmsVerificationActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    Log.d(TAG, "Code sent: $id")
                    verificationId = id
                }
            }

            val phoneAuthOptions = PhoneAuthOptions.newBuilder()
                .setActivity(this)
                .setMultiFactorSession(resolver.session)
                .setMultiFactorHint(phoneFactor)
                .setCallbacks(callbacks)
                .setTimeout(60L, TimeUnit.SECONDS)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
        } else {
            Log.e(TAG, "No phone factor found!")
            Toast.makeText(this, "No phone factor found!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifySmsCode(smsCode: String) {
        verificationId?.let { id ->
            val credential = PhoneAuthProvider.getCredential(id, smsCode)
            verifyWithCredential(credential)
        } ?: run {
            Log.e(TAG, "Verification ID is missing!")
            Toast.makeText(this, "Verification ID is missing!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyWithCredential(credential: PhoneAuthCredential) {
        val assertion = PhoneMultiFactorGenerator.getAssertion(credential)

        resolver.resolveSignIn(assertion)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "MFA Sign-in successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainMenu::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e(TAG, "MFA Sign-in failed: ${task.exception?.message}")
                    Toast.makeText(this, "MFA failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}