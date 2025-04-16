package com.example.app.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app.R
import com.example.app.model.Deal

class DealActivity : AppCompatActivity() {


    private val dealExtraKey = "DEAL_EXTRA"


    private lateinit var tvDealTitle: TextView
    private lateinit var tvDealId: TextView
    private lateinit var tvDealStage: TextView
    private lateinit var tvDealOpportunity: TextView
    private lateinit var tvDealComments: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deal)


        tvDealTitle = findViewById(R.id.tvDealTitle)
        tvDealId = findViewById(R.id.tvDealId)
        tvDealStage = findViewById(R.id.tvDealStage)
        tvDealOpportunity = findViewById(R.id.tvDealOpportunity)
        tvDealComments = findViewById(R.id.tvDealComments)



        val deal = getDealFromIntent()

        if (deal != null) {
            tvDealTitle.text = deal.title
            tvDealId.text = deal.id
            tvDealStage.text = deal.stageId
            tvDealOpportunity.text = "${deal.opportunity} ${deal.currencyId}"
            tvDealComments.text = deal.comments ?: "Нет комментариев"




            title = "Сделка #${deal.id}"

        } else {

            Toast.makeText(this, "Ошибка: Данные сделки не найдены.", Toast.LENGTH_LONG).show()
            finish()
        }
    }


    @Suppress("DEPRECATION")
    private fun getDealFromIntent(): Deal? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(dealExtraKey, Deal::class.java)
        } else {
            intent.getSerializableExtra(dealExtraKey) as? Deal
        }
    }
}