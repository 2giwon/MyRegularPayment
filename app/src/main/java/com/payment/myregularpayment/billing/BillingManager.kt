package com.payment.myregularpayment.billing

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class BillingManager(
    val onBillingConnected: () -> Unit,
    val onSuccess: (Purchase) -> Unit,
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
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    onBillingConnected()
                } else {
                    onFailure(billingResult.responseCode)
                }
            }
        })
    }

    fun getSkuDetails(
        vararg sku: String,
        billingType: String,
        result: (List<SkuDetails>) -> Unit = {}
    ) {
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(sku.asList())
            .setType(billingType)

        activity.lifecycleScope.launch(Dispatchers.IO) {
            val skuDetailsResult = billingClient.querySkuDetails(params.build())
            withContext(Dispatchers.Main) {
                result(skuDetailsResult.skuDetailsList ?: emptyList())
            }
        }
    }

    fun purchaseSku(skuDetails: SkuDetails) {
        val flowParams = BillingFlowParams.newBuilder().apply {
            setSkuDetails(skuDetails)
        }.build()

        val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            onFailure(responseCode)
        }
    }

    fun checkSubscribed(result: (Purchase?) -> Unit) {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS) { _, purchases ->
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

    fun checkInappPurchase(result: (Purchase?) -> Unit) {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP) { _, purchases ->
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
