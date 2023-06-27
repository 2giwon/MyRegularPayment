package com.payment.myregularpayment.model

data class RecyclerItem<ITEM>(
    val itemViewType: Int = 0,
    val item: ITEM
)
