package com.payment.myregularpayment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

class MainViewModel : ViewModel() {

    private val _skuDetails = MutableLiveData<List<ProductDetails>>()
    val skuDetails: LiveData<List<ProductDetails>> get() = _skuDetails

    private val _currentSubscription = MutableLiveData<Purchase>()
    val currentSubscription: LiveData<Purchase> get() = _currentSubscription

    private val _wantBuyProduct = MutableLiveData<ProductDetails>()
    val wantBuyProduct: LiveData<ProductDetails> get() = _wantBuyProduct

    fun addInappProducts(list: List<ProductDetails>) {
        val oldList = _skuDetails.value?.toMutableList() ?: mutableListOf()
        list.
        oldList
            .addAll(list)
            .distinctBy { it.productId }


        _skuDetails.value = newList
    }

    fun purchaseProduct(product: ProductDetails) {
        val skuDetails = _skuDetails.value?.toList() ?: return
        skuDetails.find { it.productId == product.productId }?.let { sku ->
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
