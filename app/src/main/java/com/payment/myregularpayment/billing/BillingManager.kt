package com.payment.myregularpayment.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class BillingManager(
    val onBillingConnected: () -> Unit,
    val onSuccess: (Purchase) -> Unit,
    val onFailure: (Int) -> Unit,
    private val activity: Activity
) {

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        val responseCode = billingResult.responseCode
        if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {

            }
        } else {
            onFailure(billingResult.responseCode)
        }
    }

    private val billingClient = BillingClient.newBuilder(activity)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
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

        billingClient.querySkuDetailsAsync(params.build()) { _, list ->
            CoroutineScope(Dispatchers.Main).launch {
                result(list ?: emptyList())
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

    fun checkSubscribed(sku: String, result: (Purchase?) -> Unit) {
        billingClient.queryPurchasesAsync(sku) { _, purchases ->
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
}
