package com.epiccrown.smartpark.view.user

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.FragmentHomeUserBinding
import com.epiccrown.smartpark.databinding.ItemCarParkedBinding
import com.epiccrown.smartpark.databinding.ItemCarParkedOnlineBinding
import com.epiccrown.smartpark.model.response.HomeResponse
import com.epiccrown.smartpark.repository.UserRepository
import com.epiccrown.smartpark.repository.network.NetworkResult
import com.epiccrown.smartpark.view.base.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.Date

class HomeFragment : BaseFragment() {
    private lateinit var binding: FragmentHomeUserBinding
    private val vm: HomeViewModel by activityViewModels {
        HomeViewModel.Factory(
            UserRepository(
                requireContext()
            )
        )
    }

    override fun CoroutineScope.start() {
        this.launch {
            vm.data.collect() {
                when (it) {
                    is NetworkResult.Success -> {
                        showResult()
                        setResult(it.data)
                    }

                    is NetworkResult.Loading -> {
                        showLoading()
                    }

                    is NetworkResult.Error -> {}
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setResult(data: HomeResponse?) {
        if (data != null) {
            val decimalFormatter = DecimalFormat("#.##")
            binding.currentDebit.text =
                "€ ${decimalFormatter.parse(data.otherInfo.debit.toString())}"
            if (data.otherInfo.debit != 0.0) {
                binding.currentDebitStatus.text = getString(R.string.pagamenti_mancanti)
            } else {
                binding.currentDebitStatus.text = getString(R.string.pagamenti_regolari)
            }

            binding.userCars.text = data.otherInfo.numCars
            binding.userParks.text = data.otherInfo.numParkedTimes

            if (data.availablePayments.isNotEmpty()) {
                binding.pendingPaymentsLayout.visibility = View.VISIBLE
                setPendingPayments(data.availablePayments)
            } else {
                binding.pendingPaymentsLayout.visibility = View.GONE
            }

            if (data.activeCars.isNotEmpty()) {
                binding.carsOnlineLayout.visibility = View.VISIBLE
                setActiveCars(data.activeCars)
            } else {
                binding.carsOnlineLayout.visibility = View.GONE
            }

            if (data.lastTransactions.isNotEmpty()) {
                binding.carsOnlineLayout.visibility = View.VISIBLE
                setTransactions(data.lastTransactions)
            } else {
                binding.carsOnlineLayout.visibility = View.GONE
            }

            binding.totalSpent.text = "€ ${decimalFormatter.parse(data.totalSpent)}"
            binding.monthlySpent.text = "€ ${decimalFormatter.parse(data.spentThisMonth)}"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTransactions(lastTransactions: List<HomeResponse.LastTransaction>) {
        val inflater = LayoutInflater.from(requireContext())
        val decimalFormatter = DecimalFormat("#.00")

        lastTransactions.forEach {
            val item =
                ItemCarParkedBinding.inflate(inflater, binding.lastTransactionContainer, true)
            item.amount.text = "€ ${decimalFormatter.parse(it.import)?.toString() ?: "0,00"}"

            item.date.text = it.carDateOut.fromServerDate("dd/MM/yyyy")
            item.inTime.text = it.carDateIn.fromServerDate("HH:mm")
            item.outTime.text = it.carDateOut.fromServerDate("HH:mm")

            item.plate.text = it.plate
            item.place.text = it.parkName

            item.root.setOnClickListener {
                //todo: open details
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setActiveCars(activeCars: List<HomeResponse.ActiveCar>) {
        val inflater = LayoutInflater.from(requireContext())
        val decimalFormatter = DecimalFormat("#.00")

        activeCars.forEach {
            val item =
                ItemCarParkedOnlineBinding.inflate(inflater, binding.carsOnlineContainer, true)

            item.date.text = it.enterTimestamp.fromServerDate("dd/MM/yyyy")


            item.plate.text = it.plate
            item.place.text = it.parkName
            val enterTime = it.enterTimestamp.fromServerDate()
            val ratePerSecond = (it.parkPrice.toDouble() / 60) / 60
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    while (true) {
                        val now = Date()
                        val diff: Long = now.time - enterTime.time
                        val seconds = diff / 1000
                        val minutes = seconds / 60
                        val hours = minutes / 60
                        val days = hours / 24

                        item.elapsedTime.text = "Tempo passato: $minutes min"
                        item.amount.text = "€ ${decimalFormatter.format(seconds * ratePerSecond)}"

                        delay(1000)
                    }
                }
            }

            item.root.setOnClickListener {
                //todo: open details
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setPendingPayments(availablePayments: List<HomeResponse.AvailablePayment>) {
        val inflater = LayoutInflater.from(requireContext())
        val decimalFormatter = DecimalFormat("#.00")

        availablePayments.forEach {
            val item =
                ItemCarParkedBinding.inflate(inflater, binding.pendingPaymentsContainer, true)
            item.amount.text = "€ ${decimalFormatter.parse(it.import)?.toString() ?: "0,00"}"

            item.date.text = it.carDateOut.fromServerDate("dd/MM/yyyy")
            item.inTime.text = it.carDateIn.fromServerDate("HH:mm")
            item.outTime.text = it.carDateOut.fromServerDate("HH:mm")

            item.plate.text = it.plate
            item.place.text = it.parkName

            item.root.setOnClickListener {
                //todo: open details
            }
        }
    }

    private fun showResult() {
        binding.loading.visibility = View.GONE
        binding.content.visibility = View.VISIBLE
    }

    private fun showLoading() {
        binding.loading.visibility = View.VISIBLE
        binding.content.visibility = View.GONE
    }

    override fun setListeners() {

    }

    override fun prepareStage() {
        vm.getData()
    }

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentHomeUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    class HomeViewModel(private val rep: UserRepository) : ViewModel() {
        private val _data =
            MutableStateFlow<NetworkResult<HomeResponse>>(NetworkResult.Success(null))
        val data: StateFlow<NetworkResult<HomeResponse>> = _data.asStateFlow()

        fun getData() = viewModelScope.launch {
            _data.emit(NetworkResult.Loading())
            _data.emit(rep.getHomePage())
        }

        class Factory(private val rep: UserRepository) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(this.rep) as T
            }
        }
    }
}