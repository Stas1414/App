package com.example.app.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.adapter.DocumentElementAdapter
import com.example.app.model.DocumentElement
import com.example.app.service.DocumentService
import kotlinx.coroutines.launch

class ProductDocumentActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DocumentElementAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewStatus: TextView

    private val documentService = DocumentService()


    companion object {
        const val EXTRA_DOC_ID = "idDocument"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_document)


        recyclerView = findViewById(R.id.recyclerViewElements)
        progressBar = findViewById(R.id.progressBar)
        textViewStatus = findViewById(R.id.textViewStatus)

        setupRecyclerView()


        val docId = intent.getStringExtra(EXTRA_DOC_ID)
        if (docId.isNullOrBlank()) {
            Log.e("DocElementsActivity", "docId not found in Intent extras or is blank.")
            showError("Ошибка: ID документа не передан.")
        } else {
            fetchAndDisplayElements(docId)
        }
    }

    private fun setupRecyclerView() {
        adapter = DocumentElementAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchAndDisplayElements(docId: String) {
        showLoading(true)

        lifecycleScope.launch {
            val elements: List<DocumentElement>? = try {
                documentService.getEnrichedDocumentElementsForDocumentOptimized(docId)
            } catch (e: Exception) {
                Log.e("DocElementsActivity", "Exception while fetching enriched elements for docId $docId", e)
                null
            }

            showLoading(false)

            if (elements != null) {
                if (elements.isNotEmpty()) {

                    adapter.updateData(elements)
                    showContent()
                } else {

                    showEmpty("Нет элементов в этом документе.")
                }
            } else {

                showError("Не удалось загрузить элементы документа.")
            }
        }
    }



    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

        if (isLoading) {
            recyclerView.visibility = View.GONE
            textViewStatus.visibility = View.GONE
        }
    }

    private fun showContent() {
        recyclerView.visibility = View.VISIBLE
        textViewStatus.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showEmpty(message: String) {
        recyclerView.visibility = View.GONE
        textViewStatus.visibility = View.VISIBLE
        textViewStatus.text = message
        progressBar.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun showError(errorMessage: String) {
        recyclerView.visibility = View.GONE
        textViewStatus.visibility = View.VISIBLE
        textViewStatus.text = "Ошибка: $errorMessage"
        progressBar.visibility = View.GONE
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
}