package com.payment.myregularpayment

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class BillingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BillingRepository = (application as MyRegularPaymentApp).repo
}
