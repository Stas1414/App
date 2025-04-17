package com.example.app.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app.R
import com.example.app.model.Deal
import com.example.app.service.DealService
import kotlinx.coroutines.launch

class OrderIssueActivity : AppCompatActivity() {


    private val dealExtraKey = "DEAL_EXTRA"

    private val targetStageName = "Успешно завершен"


    private lateinit var tvDealTitle: TextView
    private lateinit var tvDealId: TextView
    private lateinit var tvDealStage: TextView
    private lateinit var tvDealOpportunity: TextView
    private lateinit var tvDealComments: TextView
    private lateinit var btnIssueOrder: Button
    private lateinit var progressBar: ProgressBar


    private lateinit var service: DealService

    private var currentDeal: Deal? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deal)


        setupViews()




        service = DealService()


        currentDeal = getDealFromIntent()


        if (currentDeal != null) {
            populateUi(currentDeal!!)
            setupButtonClickListener(currentDeal!!)
        } else {
            showErrorAndFinish("Ошибка: Данные сделки не найдены.")
        }
    }


    private fun setupViews() {
        tvDealTitle = findViewById(R.id.tvDealTitle)
        tvDealId = findViewById(R.id.tvDealId)
        tvDealStage = findViewById(R.id.tvDealStage)
        tvDealOpportunity = findViewById(R.id.tvDealOpportunity)
        tvDealComments = findViewById(R.id.tvDealComments)
        btnIssueOrder = findViewById(R.id.btnIssueOrder)


    }


    @SuppressLint("SetTextI18n")
    private fun populateUi(deal: Deal) {
        tvDealTitle.text = deal.title
        tvDealId.text = deal.id
        tvDealStage.text = deal.stageId
        tvDealOpportunity.text = "${deal.opportunity} ${deal.currencyId}"
        tvDealComments.text = deal.comments ?: "Нет комментариев"
        title = "Сделка #${deal.id}"
    }


    private fun setupButtonClickListener(deal: Deal) {
        btnIssueOrder.setOnClickListener { buttonView ->


            val dealIdDouble = deal.id.toDoubleOrNull()
            if (dealIdDouble == null) {
                showToast("Ошибка: Некорректный формат ID сделки (${deal.id})")
                return@setOnClickListener
            }


            buttonView.isEnabled = false



            lifecycleScope.launch {
                try {
                    Log.d(
                        "OrderIssueActivity",
                        "Coroutine started: Updating deal $dealIdDouble to stage '$targetStageName'"
                    )

                    val success = service.updateDealToStageByName(dealIdDouble)

                    Log.d("OrderIssueActivity", "Coroutine finished. Update success: $success")

                    if (success) {
                        showToast("Сделка #${deal.id} успешно обновлена до стадии '$targetStageName'")
                    } else {
                        showToast("Не удалось обновить сделку #${deal.id}. Проверьте логи сервиса.")
                    }

                } catch (e: Exception) {
                    Log.e("OrderIssueActivity", "Error during deal update coroutine", e)
                    showToast("Произошла ошибка при обновлении сделки.")
                } finally {
                    buttonView.isEnabled = true
                    Log.d("OrderIssueActivity", "Coroutine finalized. Button enabled.")
                }
            }
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


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


    private fun showErrorAndFinish(message: String) {
        showToast(message)
        finish()
    }
}