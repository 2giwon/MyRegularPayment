package com.payment.myregularpayment.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<ITEM>(view: View): RecyclerView.ViewHolder(view) {
    abstract fun bindData(item: ITEM?)
}
