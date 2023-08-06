package com.payment.myregularpayment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails

class MainViewModel : ViewModel() {

    private val _skuDetails = MutableLiveData<List<SkuDetails>>()
    val skuDetails: LiveData<List<SkuDetails>> get() = _skuDetails

    private val _currentSubscription = MutableLiveData<Purchase>()
    val currentSubscription: LiveData<Purchase> get() = _currentSubscription

    private val _wantBuyProduct = MutableLiveData<SkuDetails>()
    val wantBuyProduct: LiveData<SkuDetails> get() = _wantBuyProduct

    fun addSkuDetails(list: List<SkuDetails>) {
        val newList = _skuDetails.value?.toMutableList() ?: mutableListOf()
        newList.addAll(list)

        _skuDetails.value = newList
    }

    fun purchaseProduct(product: SkuDetails) {
        val skuDetails = _skuDetails.value?.toList() ?: return
        skuDetails.find { it.sku == product.sku }?.let { sku ->
            _wantBuyProduct.value = sku
        }
    }

    fun updateSubscriptionState(purchase: Purchase) {
        _currentSubscription.value = purchase
    }

    fun clearSkuDetails() {
        _skuDetails.value = listOf()
    }
}
