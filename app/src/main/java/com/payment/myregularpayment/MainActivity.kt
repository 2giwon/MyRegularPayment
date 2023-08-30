package com.payment.myregularpayment

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.BillingClient
import com.google.android.material.snackbar.Snackbar
import com.payment.myregularpayment.base.BaseActivity
import com.payment.myregularpayment.billing.BillingManager
import com.payment.myregularpayment.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val binding by binding<ActivityMainBinding>(
        layoutResourceId = R.layout.activity_main
    )

    private lateinit var billingManager: BillingManager
    
    private var doubleBackButtonPressed = false

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (doubleBackButtonPressed) {
                finishAffinity()
                return
            }
            
            doubleBackButtonPressed = true
            Toast.makeText(
                this@MainActivity,
                getString(R.string.back_press_exit),
                Toast.LENGTH_SHORT
            ).show()
            CoroutineScope(Dispatchers.Main).launch {
                delay(2000L).run {
                    doubleBackButtonPressed = false
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setAdapter()
        setBindings()
        this.onBackPressedDispatcher.addCallback(this, backPressedCallback)

    }

    private fun setAdapter() = with(binding.rvBill) {
//        adapter = ProductAdapter(viewModel = viewModel)
//        layoutManager = LinearLayoutManager(
//            this@MainActivity,
//            LinearLayoutManager.VERTICAL,
//            false
//        )
//        setHasFixedSize(true)
    }

    private fun setBindings() {
//        viewModel.wantBuyProduct.observe(this) {
//            billingManager.purchaseProduct(it)
//        }
//
//        viewModel.skuDetails.observe(this) {
//            (binding.rvBill.adapter as? ProductAdapter)?.replaceItems(it)
//        }
//
//        viewModel.currentSubscription.observe(this) {
//            binding.cardviewCurrent.isVisible = true
//            binding.current = it
//            binding.executePendingBindings()
//        }
//
//        binding.srlMain.setOnRefreshListener {
//            viewModel.clearSkuDetails()
//            billingManager.startConnection()
//        }
    }
}
