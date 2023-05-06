package com.epiccrown.smartpark.view.user

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.ActivityPaymentDetailsBinding
import com.epiccrown.smartpark.model.internal.PaymentStatus
import com.epiccrown.smartpark.model.request.SuccessPaymentRequest
import com.epiccrown.smartpark.model.response.HomeResponse
import com.epiccrown.smartpark.repository.UserRepository
import com.epiccrown.smartpark.repository.network.NetworkResult
import com.epiccrown.smartpark.view.base.BaseActivity
import com.epiccrown.smartpark.view.common.LoadingDialog
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class PaymentDetailsActivity : BaseActivity() {
    private lateinit var loading: LoadingDialog
    private lateinit var binding: ActivityPaymentDetailsBinding
    private val vm: PaymentViewModel by viewModels { PaymentViewModel.Factory(UserRepository(this)) }

    override fun CoroutineScope.observeStuff() {}

    override fun prepareStage() {
        loading = LoadingDialog(this)
        val dataRaw = intent.getStringExtra(DETAILS)
        if (dataRaw != null) {
            val data = Gson().fromJson(dataRaw, PaymentDetails::class.java)
            setStatus(data)
            binding.plate.text = data.plate
            binding.place.text = data.parkName
            binding.date.text = data.carDateOut.fromServerDate("dd/MM/yyyy")
            binding.amount.text = "-${data.import}€"
            binding.inTime.text = data.carDateIn.fromServerDate("HH:mm")
            binding.outTime.text = data.carDateOut.fromServerDate("HH:mm")
            binding.action.text = getString(R.string.paga, "${data.import}€")


        }
    }

    private fun setStatus(data: PaymentDetails) {
        val red = ContextCompat.getColor(this, R.color.red)
        val green = ContextCompat.getColor(this, R.color.green)
        val yellow = ContextCompat.getColor(this, R.color.light_yellow)
        val black = ContextCompat.getColor(this, R.color.black)
        val white = ContextCompat.getColor(this, R.color.white)

        when (data.status) {
            PaymentStatus.PENDING -> {
                binding.paymentStatus.backgroundTintList = ColorStateList.valueOf(red)
                binding.paymentStatus.setTextColor(white)
                binding.paymentStatus.text = getString(R.string.da_pagare)
                binding.action.visibility = View.VISIBLE

                binding.action.setOnClickListener {
                    launch {
                        if (data.idPendingPayment != null)
                            vm.pay(SuccessPaymentRequest(data.idPendingPayment)).collect() {
                                when (it) {
                                    is NetworkResult.Loading -> {
                                        loading.show()
                                    }

                                    is NetworkResult.Success -> {
                                        loading.dismiss()
                                        setResult(RESULT_OK)
                                        finish()
                                    }

                                    is NetworkResult.Error -> {
                                        loading.dismiss()

                                    }
                                }
                            }
                    }
                }
            }

            PaymentStatus.FAILED -> {
                binding.paymentStatus.backgroundTintList = ColorStateList.valueOf(yellow)
                binding.paymentStatus.setTextColor(black)
                binding.paymentStatus.text = getString(R.string.pagamento_non_confermato)
                binding.action.visibility = View.VISIBLE

                binding.action.setOnClickListener {
                    launch {
                        if (data.idPendingPayment != null)
                            vm.pay(SuccessPaymentRequest(data.idPendingPayment)).collect() {
                                when (it) {
                                    is NetworkResult.Loading -> {
                                        loading.show()
                                    }

                                    is NetworkResult.Success -> {
                                        loading.dismiss()
                                        setResult(RESULT_OK)
                                        finish()
                                    }

                                    is NetworkResult.Error -> {
                                        loading.dismiss()

                                    }
                                }
                            }
                    }
                }
            }

            PaymentStatus.SUCCEEDED -> {
                binding.paymentStatus.backgroundTintList = ColorStateList.valueOf(green)
                binding.paymentStatus.setTextColor(white)
                binding.paymentStatus.text = getString(R.string.confermato)
                binding.action.visibility = View.GONE
            }
        }
    }

    override fun setListeners() {
        binding.back.setOnClickListener { finish() }


    }

    override fun getActivityView(): View {
        binding = ActivityPaymentDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    class PaymentViewModel(private val rep: UserRepository) : ViewModel() {

        fun pay(request: SuccessPaymentRequest) = flow {
            emit(NetworkResult.Loading())
            emit(rep.successPayment(request))
        }

        class Factory(private val rep: UserRepository) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PaymentViewModel(this.rep) as T
            }
        }
    }

    data class PaymentDetails(
        val idPendingPayment: String? = null,
        val status: PaymentStatus,
        val carDateIn: String = "",
        val carDateOut: String = "",
        val `import`: String = "",
        val parkName: String = "",
        val plate: String = ""
    )

    companion object {
        private const val DETAILS = "DETAILS"
        fun newIntent(context: Context, details: PaymentDetails): Intent {
            return Intent(context, PaymentDetailsActivity::class.java).apply {
                putExtra(DETAILS, Gson().toJson(details))
            }
        }

        fun getPaymentDetails(data: HistoryFragment.HistoryItem): PaymentDetails {
            return PaymentDetails(
                data.data.idPendingPayment,
                data.status,
                data.data.carDateIn,
                data.data.carDateOut,
                data.data.import,
                data.data.parkName,
                data.data.plate,
            )
        }
    }

}