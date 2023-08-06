package com.payment.myregularpayment

import android.content.Context
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("bind_setText_sku")
internal fun setSkus(textView: TextView, skuList: MutableList<String>?) {
    val context: Context = textView.context
    val purchaseId: String = skuList?.joinToString() ?: return

    if (purchaseId.contains("a0001")) {
        textView.text = context.getText(R.string.inapp_product_a0001)
    } else if (purchaseId.contains("sub002")) {
        textView.text = context.getText(R.string.subscribe_product_sub002)
    }
}
