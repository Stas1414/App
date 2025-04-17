package com.example.app.service

import android.util.Log
import com.example.app.api.ApiBitrix
import com.example.app.model.Document
import com.example.app.model.DocumentElement
import com.example.app.model.Store
import com.example.app.request.DocumentElementRequest
import com.example.app.request.DocumentIdRequest
import com.example.app.request.DocumentRequest
import com.example.app.request.ProductIdRequest
import com.example.app.response.DocumentElementResponse
import com.example.app.response.DocumentIdResponse
import com.example.app.response.DocumentResponse
import com.example.app.response.ProductResponse
import com.example.app.response.StoreResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DocumentService {

    private val baseUrl = "https://bitrix.izocom.by/rest/1/o2deu7wx7zfl3ib4/"
    private val barcodeBaseUrl = "https://bitrix.izocom.by/rest/1/sh1lchx64vrzcor6/"

    private var apiBitrix: ApiBitrix
    private var apiBarcode: ApiBitrix


    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiBitrix = retrofit.create(ApiBitrix::class.java)

        val barcodeRetrofit: Retrofit = Retrofit.Builder()
            .baseUrl(barcodeBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiBarcode = barcodeRetrofit.create(ApiBitrix::class.java)
    }

//    suspend fun getEnrichedDocumentElementsForDocumentOptimized(docId: String): List<DocumentElement>? {
//        if (docId.isBlank()) {
//            Log.e("DocumentService", "Cannot fetch document elements: docId is blank.")
//            return null
//        }
//
//        return withContext(Dispatchers.IO) {
//            try {
//
//                Log.d("DocumentService", "Step 1: Attempting to fetch initial document elements for docId: $docId")
//                val initialRequest = DocumentElementRequest(
//                    filter = mutableMapOf("docId" to docId)
//                )
//                val initialResponse: Response<DocumentElementResponse> = apiBitrix.getDocumentElements(initialRequest)
//
//                if (!initialResponse.isSuccessful) {
//                    Log.e("DocumentService", "Step 1 Failed: Failed to fetch initial elements for docId $docId. Code: ${initialResponse.code()}, Error: ${initialResponse.errorBody()?.string()}")
//                    return@withContext null
//                }
//
//                val initialElements: List<DocumentElement> = initialResponse.body()?.result?.documentElements ?: emptyList()
//
//                if (initialElements.isEmpty()) {
//                    Log.w("DocumentService", "Step 1 Success: No document elements found for docId $docId.")
//                    return@withContext emptyList()
//                }
//                Log.i("DocumentService", "Step 1 Success: Fetched ${initialElements.size} elements for docId $docId.")
//
//
//                val productIdsToFetch: List<Int> = initialElements
//                    .mapNotNull { it.elementId }
//                    .distinct()
//
//
//                if (productIdsToFetch.isEmpty()) {
//                    Log.w("DocumentService", "Step 2: No valid product elementIds found in fetched elements. Returning initial list.")
//                    return@withContext initialElements
//                }
//                Log.d("DocumentService", "Step 2: Found ${productIdsToFetch.size} unique product IDs to fetch: $productIdsToFetch")
//
//
//                Log.d("DocumentService", "Step 3: Fetching product info for ${productIdsToFetch.size} IDs...")
//                val productRequest = ProductIdRequest(
//
//                    filter = mutableMapOf("id" to productIdsToFetch),
//                    select = listOf("id", "iblockId", "name")
//                )
//                val productResponse: Response<ProductResponse> = apiBitrix.getProductsById(productRequest)
//
//                val productNameMap: Map<Int, String>
//                if (productResponse.isSuccessful) {
//                    val productInfoList = productResponse.body()?.result?.products
//                    productNameMap = productInfoList
//                        ?.mapNotNull { product ->
//                            product.id?.let { id ->
//                                product.name?.let { name ->
//                                    id to name
//                                }
//                            }
//                        }
//                        ?.toMap() ?: emptyMap()
//
//                    Log.i("DocumentService", "Step 3 Success: Fetched info for ${productNameMap.size} products.")
//                } else {
//                    Log.e("DocumentService", "Step 3 Failed: Failed to fetch product info. Code: ${productResponse.code()}, Error: ${productResponse.errorBody()?.string()}")
//
//                    return@withContext null
//                }
//
//
//                Log.d("DocumentService", "Step 4: Enriching document elements...")
//                val enrichedElements = initialElements.map { element ->
//
//                    val productName = element.elementId?.let { productNameMap[it] }
//
//                    element.copy(name = productName)
//                }
//
//                Log.i("DocumentService", "Step 4 Success: Enrichment complete. Returning ${enrichedElements.size} elements.")
//                enrichedElements
//
//            } catch (e: Exception) {
//                Log.e("DocumentService", "Error during enrichment process for docId $docId", e)
//                null
//            }
//        }
//    }

    suspend fun getEnrichedDocumentElementsForDocumentOptimized(docId: String): List<DocumentElement>? {
        if (docId.isBlank()) {
            Log.e("DocumentService", "Cannot fetch document elements: docId is blank.")
            return null
        }


        return withContext(Dispatchers.IO) {
            try {
                // --- Шаг 1: Получаем исходные элементы документа ---
                Log.d("DocumentService", "Step 1: Fetching initial document elements for docId: $docId")
                val initialElements = fetchInitialDocumentElements(docId)
                    ?: return@withContext null // Если ошибка, выходим из withContext

                if (initialElements.isEmpty()) {
                    Log.w("DocumentService", "Step 1 Success: No document elements found for docId $docId.")
                    return@withContext emptyList() // Возвращаем пустой список
                }
                Log.i("DocumentService", "Step 1 Success: Fetched ${initialElements.size} elements.")

                // --- Шаг 2: Собираем уникальные ID Продуктов и Складов ---
                val productIdsToFetch = initialElements.mapNotNull { it.elementId }.distinct()
                val storeIdsFrom = initialElements.mapNotNull { it.storeFrom }.distinct()

                Log.d("DocumentService", "Step 2: Unique Product IDs: $productIdsToFetch, Unique Store IDs: $storeIdsFrom")

                // --- Шаг 3: Параллельно получаем данные о Продуктах и Складах ---
                var productNameMap: Map<Int, String> = emptyMap()
                var storeNameMap: Map<Int, String> = emptyMap()

                coroutineScope {

                    val productsDeferred = if (productIdsToFetch.isNotEmpty()) {
                        async { fetchProductNames(productIdsToFetch) }
                    } else {
                        null
                    }


                    val storesDeferred = async { fetchAllStoresMap() }


                    productNameMap = productsDeferred?.await() ?: emptyMap()
                    storeNameMap = storesDeferred.await()


                    if (storesDeferred.isCompleted && storeNameMap.isEmpty()) {
                        Log.w("DocumentService", "Store map is empty after fetch. Enrichment for stores might be incomplete.")
                    }
                    if (productsDeferred != null && productsDeferred.isCompleted && productNameMap.isEmpty() && productIdsToFetch.isNotEmpty()) {
                        Log.w("DocumentService", "Product map is empty after fetch, although product IDs were requested.")
                    }
                }

                Log.i("DocumentService", "Step 3 Success: Fetched ${productNameMap.size} product names and ${storeNameMap.size} store names.")


                Log.d("DocumentService", "Step 4: Enriching document elements...")
                val enrichedElements = initialElements.map { element ->
                    val productName = element.elementId?.let { productNameMap[it] }
                    val storeFromName = element.storeFrom?.let { storeNameMap[it] }

                    element.copy(
                        name =  productName,
                        storeFromName = storeFromName
                    )
                }

                Log.i("DocumentService", "Step 4 Success: Enrichment complete. Returning ${enrichedElements.size} elements.")
                enrichedElements

            } catch (e: Exception) {
                Log.e("DocumentService", "Error during enrichment process for docId $docId", e)
                null
            }
        }
    }


    private suspend fun fetchInitialDocumentElements(docId: String): List<DocumentElement>? {
        val request = DocumentElementRequest(filter = mutableMapOf("docId" to docId))
        return try {
            val response = apiBitrix.getDocumentElements(request)
            if (response.isSuccessful) {
                response.body()?.result?.documentElements ?: emptyList()
            } else {
                Log.e("DocumentService", "[Internal] Failed initial fetch: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("DocumentService", "[Internal] Error initial fetch", e)
            null
        }
    }


    private suspend fun fetchProductNames(productIds: List<Int>): Map<Int, String> {
        if (productIds.isEmpty()) return emptyMap()
        val request = ProductIdRequest(
            filter = mutableMapOf("ID" to productIds),
            select = listOf("id", "name")
        )
        return try {
            val response = apiBitrix.getProductsById(request)
            if (response.isSuccessful) {
                response.body()?.result?.products
                    ?.mapNotNull { p -> p.id?.let { id -> p.name?.let { name -> id to name } } }
                    ?.toMap() ?: emptyMap()
            } else {
                Log.e("DocumentService", "[Internal] Failed product names fetch: ${response.code()}")
                emptyMap()
            }
        } catch (e: Exception) {
            Log.e("DocumentService", "[Internal] Error fetching product names", e)
            emptyMap()
        }
    }


    private suspend fun fetchAllStoresMap(): Map<Int, String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("YourServiceClass", "[Internal] Attempting to fetch all stores...")

                val response: Response<StoreResponse> = apiBitrix.getStoreList()

                if (response.isSuccessful) {

                    val stores: List<Store>? = response.body()?.result?.stores


                    val storeMap = stores
                        ?.mapNotNull { store ->
                            store.id?.let { id ->
                                store.title?.let { title ->
                                    id to title
                                }
                            }
                        }
                        ?.toMap()
                        ?: emptyMap()

                    Log.i("YourServiceClass", "[Internal] Successfully fetched ${storeMap.size} stores and created map.")
                    storeMap

                } else {

                    Log.e("YourServiceClass", "[Internal] Failed to fetch stores. Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
                    emptyMap()
                }
            } catch (e: Exception) {

                Log.e("YourServiceClass", "[Internal] Error fetching stores", e)
                emptyMap()
            }
        }
    }

    suspend fun getDocumentByBarcode(barcode: String): Document? {

        val barcodeFilterKey = "UF_CRM_BARCODE"


        val request = DocumentRequest(
            filter = mutableMapOf(
                barcodeFilterKey to barcode
            )

        )


        return withContext(Dispatchers.IO) {
            try {
                Log.d("DocumentService", "Attempting to fetch document by barcode '$barcode' using filter key '$barcodeFilterKey'")
                val response: Response<DocumentResponse> = apiBitrix.getDocuments(request)


                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val documents: List<Document>? = responseBody?.result?.documents


                    if (documents != null && documents.size == 1) {
                        val document = documents.firstOrNull()
                        Log.i("DocumentService", "Successfully fetched single document for barcode '$barcode': ID ${document?.id}")
                        document
                    } else if (documents != null && documents.isEmpty()) {
                        Log.w("DocumentService", "API call successful, but no document found for barcode '$barcode'")
                        null
                    } else {

                        Log.e("DocumentService", "API call successful, but expected 1 document, found ${documents?.size ?: "null"} for barcode '$barcode'. Response: $responseBody")
                        null
                    }
                } else {

                    Log.e("DocumentService", "Failed to fetch document by barcode '$barcode'. Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {

                Log.e("DocumentService", "Error fetching document by barcode '$barcode'", e)
                null
            }
        }
    }

    suspend fun getDocumentsByProductBarcode(barcode: String): MutableList<Document>? {
        Log.i("DocumentService", "Starting fetch documents flow for product barcode: $barcode")
        return try {

            val productId = getProductIdByBarcodeSuspend(barcode)

            if (productId == null) {
                Log.w("DocumentService", "Step 1 Failed: Product ID not found for barcode '$barcode'. Aborting.")
                return null
            }
            Log.i("DocumentService", "Step 1 Success: Found product ID '$productId' for barcode '$barcode'.")

            val docIds = fetchAllDocumentIds(elementId = productId)

            if (docIds == null) {
                Log.e("DocumentService", "Step 2 Failed: Error fetching document IDs for product ID '$productId'. Aborting.")
                return null
            }
            Log.i("DocumentService", "Step 2 Success: Found ${docIds.size} document IDs for product ID '$productId'. IDs: $docIds")


            if (docIds.isEmpty()) {
                Log.w("DocumentService", "Step 2 Result: No documents associated with product ID '$productId'. Returning empty list.")
                return null
            }


            Log.i("DocumentService", "Step 3: Fetching full document details for ${docIds.size} IDs...")

            val documents = fetchDocuments(docIds = docIds)

            if (documents != null) {
                Log.i("DocumentService", "Step 3 Success: Successfully fetched ${documents.size} documents.")
                documents
            } else {
                Log.e("DocumentService", "Step 3 Failed: Error fetching document details for IDs: $docIds.")
                null
            }

        } catch (e: Exception) {

            Log.e("DocumentService", "Exception during getDocumentsByProductBarcode flow for barcode '$barcode'", e)
            null
        }
    }

    private suspend fun fetchDocuments(
        docIds: List<Int?>
    ): MutableList<Document>? {


        val request = DocumentRequest(
            filter = mutableMapOf(
                "id" to docIds
            )
        )


        return withContext(Dispatchers.IO) {
            try {
                Log.d("DocumentService", "Attempting to fetch documents with request: $request")
                val response: Response<DocumentResponse> = apiBitrix.getDocuments(request)


                if (response.isSuccessful) {
                    val responseBody = response.body()


                    val documents: MutableList<Document> = (responseBody?.result?.documents
                        ?: emptyList()).toMutableList()

                    if (documents.isNotEmpty()) {
                        Log.i("DocumentService", "Successfully fetched ${documents.size} documents.")
                    } else {
                        Log.w("DocumentService", "API call successful, but no documents found or returned. Filter: $docIds, Response: $responseBody")
                    }
                    documents

                } else {

                    Log.e("DocumentService", "Failed to fetch documents. Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {

                Log.e("DocumentService", "Error fetching documents with request $request", e)
                null
            }
        }
    }


    private suspend fun fetchAllDocumentIds(elementId: String): List<Int>? {
        val request = DocumentIdRequest(
            select = mutableListOf(
                "docId"
            ),
            filter = mutableMapOf(
                "elementId" to elementId
            )
        )


        return withContext(Dispatchers.IO) {
            try {
                Log.d("DocumentService", "Attempting to fetch document IDs with filter: $elementId")
                val response: Response<DocumentIdResponse> = apiBitrix.getDocID(request)


                if (response.isSuccessful) {
                    val responseBody = response.body()


                    val docIds: List<Int> = responseBody?.result?.documentElements
                        ?.mapNotNull { element -> element.docId }
                        ?: emptyList()

                    if (docIds.isNotEmpty()) {
                        Log.i("DocumentService", "Successfully fetched ${docIds.size} document IDs.")
                    } else {
                        Log.w("DocumentService", "API call successful, but no document IDs found or extracted for filter $elementId. Response: $responseBody")
                    }
                    docIds

                } else {

                    Log.e("DocumentService", "Failed to fetch document IDs. Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("DocumentService", "Error fetching document IDs with filter $elementId", e)
                null
            }
        }
    }

    private suspend fun getProductIdByBarcodeSuspend(barcodeData: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiBarcode.getProductIdByBarcode(barcodeData)
                if (response.isSuccessful) {
                    val productId = response.body()?.result?.trim()
                    if (!productId.isNullOrEmpty()) {
                        productId
                    } else {
                        Log.w(
                            "ScanActivity",
                            "API returned success but Product ID is null or empty for barcode $barcodeData"
                        )
                        null
                    }
                } else {
                    Log.e(
                        "ScanActivity",
                        "Failed to get product ID: ${response.code()} - ${
                            response.errorBody()?.string()
                        }"
                    )
                    null
                }
            } catch (e: Exception) {

                Log.e("ScanActivity", "Error in getProductIdByBarcodeSuspend", e)
                throw e
            }
        }
    }

//    private suspend fun getProductByIdSuspend(productId: String): Product? {
//
//        return withContext(Dispatchers.IO) {
//            try {
//                val productRequest = ProductIdRequest(id = productId)
//                val response = apiBitrix.getProductById(productRequest)
//
//                if (response.isSuccessful) {
//
//                    val product = mapProductResponse(response.body())
//                    if (product != null) {
//                        Log.d("ScanActivity", "Product details fetched: $product")
//                        product
//                    } else {
//                        Log.e(
//                            "ScanActivity",
//                            "Product details mapping failed or result is null for ID $productId"
//                        )
//                        null
//                    }
//                } else {
//                    Log.e(
//                        "ScanActivity",
//                        "Failed to get product details: ${response.code()} - ${
//                            response.errorBody()?.string()
//                        }"
//                    )
//                    null
//                }
//            } catch (e: Exception) {
//                Log.e("ScanActivity", "Error in getProductByIdSuspend", e)
//                throw e
//            }
//        }
//    }
//
//    private fun mapProductResponse(responseBody: ProductResponse?): Product? {
//        return try {
//            val productDetails = responseBody?.result?.get("product") as? LinkedTreeMap<*, *>
//            if (productDetails != null) {
//                val productMapper = ProductMapper()
//                val product = productMapper.mapToProduct(productDetails)
//                product
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            Log.e("ScanActivity", "Error mapping product response", e)
//            null
//        }
//    }
}