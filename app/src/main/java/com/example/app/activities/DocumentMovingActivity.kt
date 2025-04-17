package com.example.app.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.adapter.DocumentAdapter
import com.example.app.listener.OnItemClickListener
import com.example.app.model.Document

class DocumentMovingActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var recyclerViewDocuments: RecyclerView
    private lateinit var documentAdapter: DocumentAdapter


    private val documentListExtraKey = "PRODUCT_EXTRA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_moving)

        setupRecyclerView()
        loadDocumentsFromIntent()
    }


    private fun setupRecyclerView() {
        recyclerViewDocuments = findViewById(R.id.recyclerViewDocuments)

        documentAdapter = DocumentAdapter(mutableListOf())
        recyclerViewDocuments.adapter = documentAdapter
        recyclerViewDocuments.layoutManager = LinearLayoutManager(this)
        recyclerViewDocuments.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    }


    private fun loadDocumentsFromIntent() {
        val documents = getDocumentsFromIntent()

        if (!documents.isNullOrEmpty()) {
            Log.d("DocumentMovingActivity", "Received ${documents.size} documents.")

            documentAdapter.updateData(documents)
        } else {

            Log.w("DocumentMovingActivity", "No documents received from intent or list is empty.")
            Toast.makeText(this, "Нет документов для отображения.", Toast.LENGTH_LONG).show()
        }
    }


    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    private fun getDocumentsFromIntent(): MutableList<Document>? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                intent.getSerializableExtra(documentListExtraKey, ArrayList::class.java) as? MutableList<Document>

            } else {

                intent.getSerializableExtra(documentListExtraKey) as? MutableList<Document>
            }
        } catch (e: ClassCastException) {
            Log.e("DocumentMovingActivity", "Error casting intent extra to MutableList<Document>", e)
            null
        } catch (e: Exception) {
            Log.e("DocumentMovingActivity", "Error getting documents from intent", e)
            null
        }
    }

    override fun onItemClick(title: String, idDocument: String) {
        Log.d("DocumentMovingActivity", "Item clicked: Title='$title', ID='$idDocument'")
        val intent = Intent(this, ProductDocumentActivity::class.java).apply {
            putExtra("title", title)
            putExtra("idDocument", idDocument)
        }
        startActivity(intent)
    }
}