package com.payment.myregularpayment.billing

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener

class BillingManager(
    val onBillingConnected: () -> Unit,
    val onSuccess: (Purchase) -> Unit,
    val onFailure: (Int) -> Unit,
    val context: Context
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

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()


}
