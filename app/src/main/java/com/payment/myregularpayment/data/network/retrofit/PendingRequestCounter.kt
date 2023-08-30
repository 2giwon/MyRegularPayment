package com.payment.myregularpayment.data.network.retrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

class PendingRequestCounter {

    /**
     * Track the number of pending server requests.
     */
    private val pendingRequestCount = AtomicInteger()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    suspend inline fun <T> use(block: () -> T): T {
        increment()
        try {
            return block()
        } finally {
            decrement()
        }
    }

    suspend fun increment() {
        val newCount = pendingRequestCount.incrementAndGet()
        Timber.i("Pending Server Requests: $newCount")
        if (newCount <= 0) {
            Timber.e("Unexpectedly low request count after new request: $newCount")
            _loading.value = false
        } else {
            _loading.value = true
        }
    }

    suspend fun decrement() {
        val newCount = pendingRequestCount.decrementAndGet()
        Timber.i("Pending Server Requests: $newCount")
        if (newCount < 0) {
            Timber.w("Unexpectedly negative request count: $newCount")
            _loading.value = false
        } else {
            _loading.value = false
        }
    }
}
