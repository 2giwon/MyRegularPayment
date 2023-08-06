package com.payment.myregularpayment

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.BillingClient
import com.payment.myregularpayment.base.BaseActivity
import com.payment.myregularpayment.billing.BillingManager
import com.payment.myregularpayment.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val binding by binding<ActivityMainBinding>(
        layoutResourceId = R.layout.activity_main
    )

    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setAdapter()
        setBindings()

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
    }
}
