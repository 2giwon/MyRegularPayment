package com.payment.myregularpayment.data.network.firebase

import androidx.lifecycle.LiveData
import com.payment.myregularpayment.data.network.retrofit.ServerFunctionsImpl

interface ServerFunctions {

    val loading: LiveData<Boolean>

    companion object {
        @Volatile
        private var INSTANCE: ServerFunctions? = null

        fun getInstance(): ServerFunctions =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ServerFunctionsImpl().also { INSTANCE = it }
            }
    }
}
