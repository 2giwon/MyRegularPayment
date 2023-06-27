package com.payment.myregularpayment.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.android.billingclient.api.SkuDetails
import com.payment.myregularpayment.base.BaseViewHolder
import com.payment.myregularpayment.databinding.ItemBillingProductBinding

class BillingViewHolder(
    @LayoutRes layoutResId: Int,
    parent: ViewGroup
): BaseViewHolder<SkuDetails>(
    LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
) {
    private val binding: ItemBillingProductBinding by lazy(LazyThreadSafetyMode.NONE) {
        ItemBillingProductBinding.bind(itemView)
    }

    override fun bindData(item: SkuDetails?) {
        item?.run {
            binding.tvProductName.text = item.title
        }
    }
}
