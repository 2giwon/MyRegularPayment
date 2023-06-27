package com.payment.myregularpayment

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.payment.myregularpayment.base.BaseViewHolder
import com.payment.myregularpayment.model.RecyclerItem
import com.payment.myregularpayment.model.ViewType
import com.payment.myregularpayment.viewholder.BillingViewHolder
import com.payment.myregularpayment.viewholder.UsedProductViewHolder

class ProductAdapter : RecyclerView.Adapter<BaseViewHolder<Any>>() {

    private val list = mutableListOf<RecyclerItem<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Any> {
        return when (viewType) {
            ViewType.BILLING_LIST.ordinal -> BillingViewHolder(
                layoutResId = R.layout.item_billing_product, parent
            )

            ViewType.USED_PRODUCT_LIST.ordinal -> UsedProductViewHolder(
                layoutResId = R.layout.item_used_product, parent
            )

            else -> object : BaseViewHolder<Any>(View(parent.context)) {
                override fun bindData(item: Any?) {}
            }
        } as BaseViewHolder<Any>
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: BaseViewHolder<Any>, position: Int) {
        holder.bindData(list[position].item)
    }

    @Suppress("NotifyDataSetChanged")
    fun replaceItems(items: List<RecyclerItem<*>>) {
        list.clear()
        list.addAll(items)
        notifyDataSetChanged()
    }
}
