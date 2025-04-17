package com.example.app.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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
    private lateinit var btnConductDocument: Button


    private val documentService = DocumentService()

    companion object {
        const val EXTRA_DOC_ID = "EXTRA_DOC_ID"
    }


    private var currentDocId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_document)


        recyclerView = findViewById(R.id.recyclerViewElements)
        progressBar = findViewById(R.id.progressBar)
        textViewStatus = findViewById(R.id.textViewStatus)
        btnConductDocument = findViewById(R.id.buttonConductDocument)

        setupRecyclerView()


        currentDocId = intent.getStringExtra(EXTRA_DOC_ID)

        if (currentDocId.isNullOrBlank()) {
            Log.e("DocElementsActivity", "docId not found in Intent extras or is blank.")
            showError("Ошибка: ID документа не передан.")
            btnConductDocument.isEnabled = false
        } else {

            setupConductButtonListener(currentDocId!!)
            fetchAndDisplayElements(currentDocId!!)
        }
    }

    private fun setupRecyclerView() {
        adapter = DocumentElementAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }


    private fun setupConductButtonListener(docId: String) {
        btnConductDocument.setOnClickListener { conductButtonView ->

            conductButtonView.isEnabled = false
            progressBar.visibility = View.VISIBLE
            textViewStatus.visibility = View.GONE

            Log.d("DocElementsActivity", "Conduct button clicked for docId: $docId")


            lifecycleScope.launch {
                var success = false
                try {

                    success = documentService.conductDocument(docId)


                    if (success) {
                        Log.i("DocElementsActivity", "Document $docId conducted successfully.")
                        showToast("Документ ID $docId успешно проведен!")
                        val intent = Intent(this@ProductDocumentActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.e("DocElementsActivity", "Failed to conduct document $docId.")
                        showError("Не удалось провести документ ID $docId.")

                    }

                } catch (e: Exception) {
                    Log.e("DocElementsActivity", "Exception during conductDocument call for $docId", e)
                    showError("Произошла ошибка при проведении документа.")
                } finally {

                    progressBar.visibility = View.GONE
                    if (!success) {
                        conductButtonView.isEnabled = true
                    }
                    Log.d("DocElementsActivity", "Conduct coroutine finished. Button enabled: ${conductButtonView.isEnabled}")
                }
            }
        }
    }


    private fun fetchAndDisplayElements(docId: String) {
        showLoading(true)

        lifecycleScope.launch {
            val elements: List<DocumentElement>? = try {
                documentService.getEnrichedDocumentElementsForDocumentOptimized(docId)
            } catch (e: Exception) {
                Log.e("DocElementsActivity", "Exception while fetching elements for docId $docId", e)
                null
            }

            showLoading(false)

            if (elements != null) {
                if (elements.isNotEmpty()) {
                    adapter.updateData(elements)
                    showContent()
                } else {
                    showEmpty("Нет элементов в этом документе.")

                    btnConductDocument.visibility = View.GONE
                }
            } else {
                showError("Не удалось загрузить элементы документа.")

                btnConductDocument.visibility = View.GONE
            }
        }
    }


    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            recyclerView.visibility = View.GONE
            textViewStatus.visibility = View.GONE
            btnConductDocument.visibility = View.GONE
        }
    }

    private fun showContent() {
        recyclerView.visibility = View.VISIBLE
        textViewStatus.visibility = View.GONE
        progressBar.visibility = View.GONE

        btnConductDocument.visibility = View.VISIBLE

        if (btnConductDocument.text != "Документ проведен") {
            btnConductDocument.isEnabled = true
        }

    }

    private fun showEmpty(message: String) {
        recyclerView.visibility = View.GONE
        textViewStatus.visibility = View.VISIBLE
        textViewStatus.text = message
        progressBar.visibility = View.GONE

        btnConductDocument.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun showError(errorMessage: String) {
        recyclerView.visibility = View.GONE
        textViewStatus.visibility = View.VISIBLE
        textViewStatus.text = "Ошибка: $errorMessage"
        progressBar.visibility = View.GONE

        btnConductDocument.visibility = View.GONE

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}