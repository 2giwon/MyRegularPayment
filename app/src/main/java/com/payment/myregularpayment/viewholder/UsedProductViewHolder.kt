package com.payment.myregularpayment.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.android.billingclient.api.SkuDetails
import com.payment.myregularpayment.base.BaseViewHolder
import com.payment.myregularpayment.databinding.ItemUsedProductBinding

class UsedProductViewHolder(
    @LayoutRes layoutResId: Int,
    parent: ViewGroup
) : BaseViewHolder<SkuDetails>(
    LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
) {
    private val binding: ItemUsedProductBinding by lazy(LazyThreadSafetyMode.NONE) {
        ItemUsedProductBinding.bind(itemView)
    }

    override fun bindData(item: SkuDetails?) {
        item?.run {
            binding.tvTitle.text = item.title
        }
    }
}
