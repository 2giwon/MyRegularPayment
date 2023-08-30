package com.payment.myregularpayment.billing

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class BillingManager(
    val onBillingConnected: () -> Unit,
    val onSuccess: (Purchase) -> Unit,
    val onConnectedFailure: (String) -> Unit,
    val onFailure: (Int) -> Unit,
    private val activity: ComponentActivity
) {

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        val responseCode = billingResult.responseCode
        if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                confirmPurchase(purchase)
            }
        } else {
            onFailure(billingResult.responseCode)
        }
    }

    private val billingClient = BillingClient.newBuilder(activity)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Timber.d(
                    "BiilingManager",
                    "== BillingClient onBillingServiceDisconnected() called =="
                )
                startConnection()
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    onBillingConnected()
                } else {
                    onConnectedFailure(billingResult.debugMessage)
                }
            }
        })
    }

    fun getInAppProducts(
        vararg sku: String,
        result: (List<ProductDetails>) -> Unit = {}
    ) {
        val productList = sku.asList().map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)

        activity.lifecycleScope.launch(Dispatchers.IO) {
            val productDetailsResult: ProductDetailsResult =
                billingClient.queryProductDetails(params.build())

            result(productDetailsResult.productDetailsList ?: emptyList())
        }
    }

    fun purchaseProduct(productDetails: ProductDetails) {
        val offerToken: String =
            productDetails.subscriptionOfferDetails?.get(0)?.offerToken ?: return

        val productDetailParamList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailParamList)
            .build()

        val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            onFailure(responseCode)
        }
    }

    fun checkSubscribed(result: (Purchase?) -> Unit) {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { _, purchaseList ->
            CoroutineScope(Dispatchers.Main).launch {
                for (purchase in purchaseList) {
                    if (purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        return@launch result(purchase)
                    }
                }
                return@launch result(null)
            }
        }
    }

    fun checkInappPurchase(result: (Purchase?) -> Unit) {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams
                .newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { _, purchases ->
            CoroutineScope(Dispatchers.Main).launch {
                for (purchase in purchases) {
                    if (purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        return@launch result(purchase)
                    }
                }
                return@launch result(null)
            }
        }
    }

    fun confirmPurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val ackPurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)

            CoroutineScope(Dispatchers.IO).launch {
                billingClient.acknowledgePurchase(ackPurchaseParams.build()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                            onSuccess(purchase)
                        } else {
                            onFailure(it.responseCode)
                        }
                    }
                }
            }
        }
    }

    fun disconnectedBillingClient() {
        billingClient.endConnection()
    }
}
