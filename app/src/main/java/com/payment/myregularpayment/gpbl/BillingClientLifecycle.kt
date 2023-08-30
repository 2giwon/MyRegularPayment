package com.payment.myregularpayment.gpbl

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.AlternativeChoiceDetails.Product
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.payment.myregularpayment.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class BillingClientLifecycle private constructor(
    private val applicationContext: Context,
    private val externalScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : DefaultLifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener,
    ProductDetailsResponseListener, PurchasesResponseListener {

    private val _subscriptionPurchases = MutableStateFlow<List<Purchase>>(emptyList())
    private val _oneTimeProductPurchases = MutableStateFlow<List<Purchase>>(emptyList())

    val subscriptionPurchases = _subscriptionPurchases.asStateFlow()
    val oneTimeProductPurchases = _oneTimeProductPurchases.asStateFlow()

    val productDetails = MutableLiveData<ProductDetails?>()
    val oneTimeProductDetails = MutableLiveData<ProductDetails?>()

    private var cachedPurchasesList: List<Purchase>? = null

    private lateinit var billingClient: BillingClient

    override fun onCreate(owner: LifecycleOwner) {
        Timber.d("ON_CREATE")

        billingClient = BillingClient.newBuilder(applicationContext)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        if (!billingClient.isReady) {
            Timber.d("BillingClient : Start connection ...")
            billingClient.startConnection(this)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Timber.d("ON_DESTROY")
        if (billingClient.isReady) {
            Timber.d("BillingClient can only be used once -- closing connection")

            billingClient.endConnection()
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Timber.d("onPurchasesUpdated: $responseCode $debugMessage")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases == null) {
                    Timber.d("onPurchasesUpdated: null purchase list")
                    processPurchases(null)
                } else {
                    processPurchases(purchases)
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.i("onPurchasesUpdated: User canceled the purchase")
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Timber.i("onPurchasesUpdated: The user already owns this item")
            }

            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Timber.e(
                    "onPurchasesUpdated: Developer error means that Google Play does " +
                            "not recognize the configuration. If you are just getting started, " +
                            "make sure you have configured the application correctly in the " +
                            "Google Play Console. The product ID must match and the APK you " +
                            "are using must be signed with release keys."
                )
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        Timber.d("onBillingServiceDisconnected")
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage

        Timber.d("onBillingSetupFinished: $responseCode $debugMessage")
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            querySubscriptionProductDetails()
            queryOneTimeProductDetails()
            querySubscriptionPurchases()
            queryOneTimeProductPurchases()
        }
    }

    override fun onProductDetailsResponse(
        billingResult: BillingResult,
        productDetailsList: MutableList<ProductDetails>
    ) {
        val response = BillingResponse(billingResult.responseCode)
        val debugMessage = billingResult.debugMessage
        when {
            response.isOk -> {
                processProductDetails(productDetailsList)
            }

            response.isTerribleFailure -> {
                Timber.w("onProductDetailsResponse - Unexpected error: ${response.code} $debugMessage")
            }

            else -> {
                Timber.e("onProductDetailsResponse: ${response.code} $debugMessage")
            }
        }
    }

    override fun onQueryPurchasesResponse(
        billingResult: BillingResult,
        purchasesList: List<Purchase>
    ) {
        processPurchases(purchasesList)
    }

    private fun processPurchases(purchasesList: List<Purchase>?) {
        Timber.d("processPurchases: ${purchasesList?.size} purchase(s)")
        purchasesList?.let { list ->
            if (isUnChangedPurchaseList(purchasesList)) {
                Timber.d("processPurchases: Purchase list has not changed")
                return
            }
            externalScope.launch {
                val subscriptionPurchaseList = list.filter { purchase ->
                    purchase.products.any { product ->
                        product in listOf(Constants.SUB_PRODUCT01, Constants.SUB_PRODUCT02)
                    }
                }

                val oneTimeProductPurchasesList = list.filter { purchase ->
                    purchase.products.contains(Constants.ONE_TIME_PRODUCT01)
                }

                _oneTimeProductPurchases.emit(oneTimeProductPurchasesList)
                _subscriptionPurchases.emit(subscriptionPurchaseList)
            }
            logAcknowledgementStatus(list)
        }
    }

    private fun isUnChangedPurchaseList(purchasesList: List<Purchase>): Boolean {
        val isUnchanged = purchasesList == cachedPurchasesList
        if (!isUnchanged) {
            cachedPurchasesList = purchasesList
        }
        return isUnchanged
    }

    private fun logAcknowledgementStatus(purchasesList: List<Purchase>) {
        var acknowledgedCounter = 0
        var unacknowledgedCounter = 0
        for (purchase in purchasesList) {
            if (purchase.isAcknowledged) {
                acknowledgedCounter++
            } else {
                unacknowledgedCounter++
            }
        }
        Timber.d(
            "logAcknowledgementStatus: acknowledged=$acknowledgedCounter " +
                    "unacknowledged=$unacknowledgedCounter"
        )
    }

    private fun querySubscriptionProductDetails() {
        Timber.d("querySubscriptionProductDetails")
        val params = QueryProductDetailsParams.newBuilder()

        val productList: MutableList<QueryProductDetailsParams.Product> = mutableListOf()
        for (product in LIST_OF_SUBSCRIPTION_PRODUCTS) {
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        }

        params.setProductList(productList).let { productDetailsParams ->
            billingClient.queryProductDetailsAsync(productDetailsParams.build(), this)
        }
    }

    private fun queryOneTimeProductDetails() {
        Timber.d("queryOneTimeProductDetails")
        val params = QueryProductDetailsParams.newBuilder()

        val productList = LIST_OF_ONE_TIME_PRODUCTS.map { product ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(product)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        params.apply {
            setProductList(productList)
        }.let { productDetailsParams ->
            billingClient.queryProductDetailsAsync(productDetailsParams.build(), this)
        }
    }

    fun querySubscriptionPurchases() {
        if (!billingClient.isReady) {
            Timber.d("querySubscriptionPurchases: BillingClient is not ready")
            billingClient.startConnection(this)
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build(), this
        )
    }

    fun queryOneTimeProductPurchases() {
        if (!billingClient.isReady) {
            Timber.d("queryOneTimeProductPurchases: BillingClient is not ready")
            billingClient.startConnection(this)
        }
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(), this
        )
    }

    private fun processProductDetails(productDetailsList: MutableList<ProductDetails>) {
        val expectedProductDetailsCount = LIST_OF_SUBSCRIPTION_PRODUCTS.size
        if (productDetailsList.isEmpty()) {
            Timber.e(
                "processProductDetails: " +
                        "Expected ${expectedProductDetailsCount}, " +
                        "Found null ProductDetails. " +
                        "Check to see if the products you requested are correctly published " +
                        "in the Google Play Console."
            )
            postProductDetails(emptyList())
        } else {
            postProductDetails(productDetailsList)
        }
    }

    private fun postProductDetails(productDetailsList: List<ProductDetails>) {
        productDetailsList.forEach { productDetails ->
            when (productDetails.productType) {
                BillingClient.ProductType.SUBS -> {
                    this.productDetails.postValue(productDetails)
                }

                BillingClient.ProductType.INAPP -> {
                    this.oneTimeProductDetails.postValue(productDetails)
                }
            }
        }
    }

    fun launchBillingFlow(activity: Activity, params: BillingFlowParams): Int {
        if (!billingClient.isReady) {
            Timber.e("launchBillingFlow: BillingClient is not ready")
        }
        val billingResult = billingClient.launchBillingFlow(activity, params)
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Timber.d("launchBillingFlow: BillingResponse $responseCode $debugMessage")
        return responseCode
    }

//    suspend fun acknowledgePurchase(purchaseToken: String): BillingResult {
//        Timber.d("acknowledgePurchase")
//        val params = BillingClient.BillingFlowParams.newBuilder()
//            .setObfuscatedAccountId(Constants.ACCOUNT_ID)
//            .setObfuscatedProfileId(Constants.PROFILE_ID)
//            .setPurchaseToken(purchaseToken)
//            .build()
//        return billingClient.acknowledgePurchase(params)
//    }

    companion object {
        private const val MAX_RETRY_ATTEMPT = 3

        private val LIST_OF_SUBSCRIPTION_PRODUCTS = listOf(
            Constants.SUB_PRODUCT01,
            Constants.SUB_PRODUCT02
        )

        private val LIST_OF_ONE_TIME_PRODUCTS = listOf(
            Constants.ONE_TIME_PRODUCT01
        )

        @Volatile
        private var INSTANCE: BillingClientLifecycle? = null

        fun getInstance(applicationContext: Context): BillingClientLifecycle =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingClientLifecycle(applicationContext).also { INSTANCE = it }
            }
    }
}

@JvmInline
private value class BillingResponse(val code: Int) {
    val isOk: Boolean
        get() = code == BillingClient.BillingResponseCode.OK
    val canFailGracefully: Boolean
        get() = code == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
    val isRecoverableError: Boolean
        get() = code in setOf(
            BillingClient.BillingResponseCode.ERROR,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
        )
    val isNonrecoverableError: Boolean
        get() = code in setOf(
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
        )
    val isTerribleFailure: Boolean
        get() = code in setOf(
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED,
            BillingClient.BillingResponseCode.USER_CANCELED,
        )
}
