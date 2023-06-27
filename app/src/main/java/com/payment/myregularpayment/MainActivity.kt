package com.payment.myregularpayment

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.payment.myregularpayment.base.BaseActivity
import com.payment.myregularpayment.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val binding by binding<ActivityMainBinding>(
        layoutResourceId = R.layout.activity_main
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAdapter()
    }

    private fun setAdapter() = with(binding.rvBill) {
        adapter = ProductAdapter()
        layoutManager = LinearLayoutManager(
            this@MainActivity,
            LinearLayoutManager.VERTICAL,
            false
        )
        setHasFixedSize(true)
    }
}
