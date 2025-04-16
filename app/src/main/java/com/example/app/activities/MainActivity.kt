package com.example.app.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.app.R

class MainActivity : AppCompatActivity() {

    private lateinit var btnFind: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnFind = findViewById(R.id.btnFind)
        btnFind.setOnClickListener {
            startActivity(Intent(this, BarcodeScannerActivity::class.java))
        }
    }
}