package com.payment.myregularpayment

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.BillingClient
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

        billingManager = BillingManager(
            activity = this@MainActivity,
            onBillingConnected = {
                billingManager.getSkuDetails("s001", "sub001", "sub002", billingType = BillingClient.SkuType.SUBS) {
                    viewModel.addSkuDetails(it)
                }

                billingManager.getSkuDetails("a0001", billingType = BillingClient.SkuType.INAPP) {
                    viewModel.addSkuDetails(it)
                }

                billingManager.checkSubscribed { purchase ->
                    viewModel.updateSubscriptionState(purchase ?: return@checkSubscribed)
                }

                billingManager.checkInappPurchase { purchase ->
                    viewModel.updateSubscriptionState(purchase ?: return@checkInappPurchase)
                }

                binding.srlMain.isRefreshing = false
            },
            onSuccess = { purchase ->
                viewModel.updateSubscriptionState(purchase)
            },
            onFailure = { responseCode ->
                Toast.makeText(
                    applicationContext,
                    "구매 도중 오류가 발생하였습니다. (${responseCode})",
                    Toast.LENGTH_SHORT
                ).show()
                binding.srlMain.isRefreshing = false
            }
        )

        billingManager.startConnection()
    }

    private fun setAdapter() = with(binding.rvBill) {
        adapter = ProductAdapter(viewModel = viewModel)
        layoutManager = LinearLayoutManager(
            this@MainActivity,
            LinearLayoutManager.VERTICAL,
            false
        )
        setHasFixedSize(true)
    }

    private fun setBindings() {
        viewModel.wantBuyProduct.observe(this) {
            billingManager.purchaseSku(it)
        }

        viewModel.skuDetails.observe(this) {
            (binding.rvBill.adapter as? ProductAdapter)?.replaceItems(it)
        }

        viewModel.currentSubscription.observe(this) {
            binding.cardviewCurrent.isVisible = true
            binding.current = it
            binding.executePendingBindings()
        }

        binding.srlMain.setOnRefreshListener {
            viewModel.clearSkuDetails()
            billingManager.startConnection()
        }
    }
}
