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
        verifyCodeButton.isEnabled = false


        sendCodeButton.setOnClickListener {
            val phoneNumber = phoneNumberInput.text.toString().trim()
            when {
                phoneNumber.isEmpty() -> {
                    Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
                }
                !phoneNumber.startsWith("+") -> {
                    Toast.makeText(this, "Please include country code (e.g., +44)", Toast.LENGTH_LONG).show()
                }
                phoneNumber.length < 10 -> {
                    Toast.makeText(this, "Phone number too short", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    requestMultiFactorEnrollment(phoneNumber)
                }
            }
        }

        verifyCodeButton.setOnClickListener {
            val code = codeInput.text.toString().trim()
            if (code.length == 6) {
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
            Toast.makeText(this, "User not signed in, please login again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val session = multiFactorSession
        if (session == null) {
            Log.e(TAG, "MultiFactorSession is null")
            Toast.makeText(this, "Verification Failed", Toast.LENGTH_SHORT).show()
            return
        }
        sendCodeButton.isEnabled = false

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setMultiFactorSession(session)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(TAG, "Verification completed")
                    enrollPhoneNumberAsMFA(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e(TAG, "Verification failed: ${e.message}", e)
                    sendCodeButton.isEnabled = true
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
            verifyCodeButton.isEnabled = false
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            enrollPhoneNumberAsMFA(credential)
        } catch (e: Exception) {
            Log.e(TAG, "Verification failed: ${e.message}")
            verifyCodeButton.isEnabled = true
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
                        Toast.makeText(this, "Phone number enrolled for MFA", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        verifyCodeButton.isEnabled = true
                        Log.e(TAG, "MFA enrollment failed: ${task.exception?.message}")
                        Toast.makeText(this, "MFA enrollment failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(this, "User not signed in, please login again", Toast.LENGTH_LONG).show()
        }
    }

}