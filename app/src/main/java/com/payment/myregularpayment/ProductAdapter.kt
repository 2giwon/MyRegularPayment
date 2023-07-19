package com.payment.myregularpayment

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.payment.myregularpayment.base.BaseViewHolder
import com.payment.myregularpayment.viewholder.BillingViewHolder

class ProductAdapter(
    private val viewModel: MainViewModel,
) : RecyclerView.Adapter<BaseViewHolder<SkuDetails>>() {

    private val list = mutableListOf<SkuDetails>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<SkuDetails> {
        return BillingViewHolder(
            layoutResId = R.layout.item_billing_product,
            viewModel = viewModel,
            parent = parent
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<SkuDetails>, position: Int) {
        holder.bindData(list[position])
    }

    override fun getItemCount(): Int = list.size

    @Suppress("NotifyDataSetChanged")
    fun replaceItems(items: List<SkuDetails>) {
        list.clear()
        list.addAll(items)
        notifyDataSetChanged()
    }
}
