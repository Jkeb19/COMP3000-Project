package com.example.seedguard

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class PhoneVerificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private lateinit var phoneNumberInput: EditText
    private lateinit var codeInput: EditText
    private lateinit var sendCodeButton: Button
    private lateinit var verifyCodeButton: Button
    private var multiFactorSession: MultiFactorSession? = null
    private val TAG = "PhoneVerificationActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_verification)

        auth = FirebaseAuth.getInstance()

        phoneNumberInput = findViewById(R.id.phoneNumberInput)
        codeInput = findViewById(R.id.codeInput)
        sendCodeButton = findViewById(R.id.sendCodeButton)
        verifyCodeButton = findViewById(R.id.verifyCodeButton)

        sendCodeButton.setOnClickListener {
            val phoneNumber = phoneNumberInput.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                requestMultiFactorEnrollment(phoneNumber)
            } else {
                Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show()
            }
        }

        verifyCodeButton.setOnClickListener {
            val code = codeInput.text.toString().trim()
            if (code.isNotEmpty()) {
                verifyCode(code)
            } else {
                Toast.makeText(this, "Enter the verification code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestMultiFactorEnrollment(phoneNumber: String) {
        val user = auth.currentUser
        if (user != null) {
            user.multiFactor.session
                .addOnCompleteListener { sessionTask ->
                    if (sessionTask.isSuccessful) {
                        multiFactorSession = sessionTask.result
                        sendVerificationCode(phoneNumber)
                    } else {
                        Log.e(TAG, "Failed to get MultiFactorSession: ${sessionTask.exception?.message}")
                        Toast.makeText(this, "Error starting MFA enrollment", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "User not signed in!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val session = multiFactorSession
        if (session == null) {
            Log.e(TAG, "MultiFactorSession is null")
            Toast.makeText(this, "Verification session not available", Toast.LENGTH_SHORT).show()
            return
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setMultiFactorSession(session)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(TAG, "Verification completed automatically")
                    enrollPhoneNumberAsMFA(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e(TAG, "Verification failed: ${e.message}", e)
                    Toast.makeText(this@PhoneVerificationActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    Log.d(TAG, "Verification code sent: $id")
                    verificationId = id
                    verifyCodeButton.isEnabled = true
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            enrollPhoneNumberAsMFA(credential)
        } catch (e: Exception) {
            Log.e(TAG, "Verification failed: ${e.message}")
            Toast.makeText(this, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun enrollPhoneNumberAsMFA(credential: PhoneAuthCredential) {
        val user = auth.currentUser
        if (user != null) {
            val phoneAssertion = PhoneMultiFactorGenerator.getAssertion(credential)

            user.multiFactor.enroll(phoneAssertion, "Phone MFA")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Phone number enrolled for MFA!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Log.e(TAG, "MFA enrollment failed: ${task.exception?.message}")
                        Toast.makeText(this, "MFA enrollment failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(this, "User not signed in!", Toast.LENGTH_SHORT).show()
        }
    }

}