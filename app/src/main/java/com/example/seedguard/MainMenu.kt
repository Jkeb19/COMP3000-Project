package com.example.seedguard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainMenu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val viewSavedButton: Button = findViewById(R.id.button)
        val addNewButton: Button = findViewById(R.id.button2)

        viewSavedButton.setOnClickListener {
            val intent = Intent(this, ViewWalletsActivity::class.java)
            startActivity(intent)
        }

        addNewButton.setOnClickListener {
            val intent = Intent(this, AddNewSeedPhraseActivity::class.java)
            startActivity(intent)
        }
        val enableSmsButton: Button = findViewById(R.id.button3)

        enableSmsButton.setOnClickListener {
            val intent = Intent(this, PhoneVerificationActivity::class.java)
            startActivity(intent)
        }

    }
}
