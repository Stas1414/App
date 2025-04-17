package com.example.app.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.app.R

class MainActivity : AppCompatActivity() {

    private lateinit var btnMoving: Button
    private lateinit var btnGettingDeal: Button
    private lateinit var btnGiveDeal: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initButton()

        btnMoving.setOnClickListener {
            startMovingActivity()
        }

        btnGiveDeal.setOnClickListener {
            startOrderIssueActivity()
        }

        btnGettingDeal.setOnClickListener {
            startGettingActivity()
        }
    }


    private fun initButton() {
        btnMoving = findViewById(R.id.btnMoving)
        btnGiveDeal = findViewById(R.id.btnGiveDeal)
        btnGettingDeal = findViewById(R.id.btnGettingDeal)
    }


    private fun startMovingActivity() {

    }

    private fun startOrderIssueActivity() {
        val intent = Intent(this, OrderIssueScannerActivity::class.java)
        startActivity(intent)
    }

    private fun startGettingActivity() {

    }
}