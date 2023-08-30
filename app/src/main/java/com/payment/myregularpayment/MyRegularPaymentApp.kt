package com.payment.myregularpayment

import android.app.Application
import com.payment.myregularpayment.data.network.firebase.ServerFunctions
import com.payment.myregularpayment.gpbl.BillingClientLifecycle

class MyRegularPaymentApp : Application() {

    private val serverFunctions: ServerFunctions
        get() {
            return ServerFunctions.getInstance()
        }


    val billingClientLifecycle: BillingClientLifecycle
        get() = BillingClientLifecycle.getInstance(this)

}
