package com.payment.myregularpayment.data.network.retrofit

import androidx.lifecycle.LiveData
import com.payment.myregularpayment.data.network.firebase.ServerFunctions
import retrofit2.Response

fun <T> Response<T>.errorLog(): String {
    return "Failed to call API (Error code: ${code()}) - ${errorBody()?.string()}"
}

class ServerFunctionsImpl: ServerFunctions {

    private val pendingRequestCounter = PendingRequestCounter()

    override val loading: LiveData<Boolean>
        get() = pendingRequestCounter.loading


}
