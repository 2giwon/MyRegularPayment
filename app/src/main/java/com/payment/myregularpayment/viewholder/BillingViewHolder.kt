package com.payment.myregularpayment.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.SkuDetails
import com.payment.myregularpayment.MainViewModel
import com.payment.myregularpayment.base.BaseViewHolder
import com.payment.myregularpayment.databinding.ItemBillingProductBinding

class BillingViewHolder(
    @LayoutRes layoutResId: Int,
    parent: ViewGroup,
    private val viewModel: MainViewModel
): BaseViewHolder<ProductDetails>(
    LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
) {
    private val binding: ItemBillingProductBinding by lazy(LazyThreadSafetyMode.NONE) {
        ItemBillingProductBinding.bind(itemView)
    }

    override fun bindData(item: ProductDetails?) {
        binding.run {
            viewModel = this@BillingViewHolder.viewModel
            product = item ?: return@run
            tvProductName.text = item.title
            tvPrice.text = item.name
            tvProductType.text = item.productType
        }
    }
}
