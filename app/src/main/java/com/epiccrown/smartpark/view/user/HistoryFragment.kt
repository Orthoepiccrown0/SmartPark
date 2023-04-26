package com.epiccrown.smartpark.view.user

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.FragmentHistoryUserBinding
import com.epiccrown.smartpark.databinding.ItemCarParkedHistoryBinding
import com.epiccrown.smartpark.model.internal.PaymentStatus
import com.epiccrown.smartpark.model.response.HistoryResponse
import com.epiccrown.smartpark.repository.UserRepository
import com.epiccrown.smartpark.repository.network.NetworkResult
import com.epiccrown.smartpark.view.base.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryFragment : BaseFragment() {
    private lateinit var binding: FragmentHistoryUserBinding
    private val vm: HistoryViewModel by activityViewModels {
        HistoryViewModel.Factory(
            UserRepository(
                requireContext()
            )
        )
    }

    override fun CoroutineScope.start() {
        launch {
            vm.data.collect() {
                when (it) {
                    is NetworkResult.Success -> {
                        showResult()
                        setResult(it.data)
                    }

                    is NetworkResult.Error -> {
                        showLoading()
                    }

                    is NetworkResult.Loading -> {
                        showLoading()
                    }
                }
            }
        }
    }

    private fun setResult(data: HistoryResponse?) {
        if (data != null) {
            val history = arrayListOf<HistoryItem>()
            history.addAll(data.pendingPayments
                .map { HistoryItem(it, PaymentStatus.PENDING) }
                .sortedBy { it.data.carDateOut })
            history.addAll(data.failedPayments
                .map { HistoryItem(it, PaymentStatus.FAILED) }
                .sortedBy { it.data.carDateOut })
            history.addAll(data.succeedPayments
                .map { HistoryItem(it, PaymentStatus.SUCCEEDED) }
                .sortedBy { it.data.carDateOut })

            val adapter = HistoryAdapter(history) {
                //todo: show details
            }

            if (history.isNotEmpty()) {
                binding.recycler.adapter = adapter
                binding.recycler.layoutManager = LinearLayoutManager(requireContext())
            } else {
                showNoHistory()
            }

        } else {
            showNoHistory()
        }
    }

    private fun showNoHistory() {
        binding.recycler.visibility = View.GONE
        binding.noTransactions.visibility = View.VISIBLE
    }

    private fun showResult() {
        binding.loading.visibility = View.GONE
        binding.recycler.visibility = View.VISIBLE
    }

    private fun showLoading() {
        binding.loading.visibility = View.VISIBLE
        binding.recycler.visibility = View.GONE
    }

    override fun setListeners() {
    }

    override fun prepareStage() {
        vm.getData()
    }

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentHistoryUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    inner class HistoryAdapter(
        val data: ArrayList<HistoryItem>,
        val open: (HistoryResponse.Payment) -> Unit
    ) :
        RecyclerView.Adapter<HistoryAdapter.ItemHolder>() {
        private val red = ContextCompat.getColor(requireContext(), R.color.red)
        private val green = ContextCompat.getColor(requireContext(), R.color.green)
        private val yellow = ContextCompat.getColor(requireContext(), R.color.light_yellow)
        private val black = ContextCompat.getColor(requireContext(), R.color.black)
        private val white = ContextCompat.getColor(requireContext(), R.color.white)

        inner class ItemHolder(val binding: ItemCarParkedHistoryBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun setData(item: HistoryItem) {
                binding.plate.text = item.data.plate
                binding.place.text = item.data.parkName
                binding.date.text = item.data.carDateOut.fromServerDate("dd/MM/yyyy")
                binding.amount.text = "€ ${item.data.import}"
                binding.inTime.text = item.data.carDateIn.fromServerDate("HH:mm")
                binding.outTime.text = item.data.carDateOut.fromServerDate("HH:mm")

                when (item.status) {
                    PaymentStatus.PENDING -> {
                        binding.paymentStatus.backgroundTintList = ColorStateList.valueOf(red)
                        binding.paymentStatus.setTextColor(white)
                        binding.paymentStatus.text = getString(R.string.da_pagare)
                    }

                    PaymentStatus.FAILED -> {
                        binding.paymentStatus.backgroundTintList = ColorStateList.valueOf(yellow)
                        binding.paymentStatus.setTextColor(black)
                        binding.paymentStatus.text = getString(R.string.pagamento_non_confermato)
                    }

                    PaymentStatus.SUCCEEDED -> {
                        binding.paymentStatus.backgroundTintList = ColorStateList.valueOf(green)
                        binding.paymentStatus.setTextColor(white)
                        binding.paymentStatus.text = getString(R.string.confermato)
                    }
                }
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            return ItemHolder(
                ItemCarParkedHistoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val item = data[position]
            holder.setData(item)
        }
    }

    data class HistoryItem(
        val data: HistoryResponse.Payment,
        val status: PaymentStatus
    )

    class HistoryViewModel(private val rep: UserRepository) : ViewModel() {
        private val _data =
            MutableStateFlow<NetworkResult<HistoryResponse>>(NetworkResult.Success(null))
        val data: StateFlow<NetworkResult<HistoryResponse>> = _data.asStateFlow()

        fun getData() = viewModelScope.launch {
            _data.emit(NetworkResult.Loading())
            _data.emit(rep.getHistory())
        }

        class Factory(private val rep: UserRepository) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HistoryViewModel(this.rep) as T
            }
        }
    }
}