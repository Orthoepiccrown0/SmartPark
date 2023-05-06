package com.epiccrown.smartpark.view.admin.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.epiccrown.smartpark.model.request.CarRevealedRequest
import com.epiccrown.smartpark.model.request.ProcessDataRequest
import com.epiccrown.smartpark.model.response.ProcessDataResponse
import com.epiccrown.smartpark.repository.AdminRepository
import com.epiccrown.smartpark.repository.network.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class AdminViewModel(private val rep: AdminRepository) : ViewModel() {
    private val _data =
        MutableStateFlow<NetworkResult<ProcessDataResponse>>(NetworkResult.Success(null))
    val data: StateFlow<NetworkResult<ProcessDataResponse>> = _data.asStateFlow()

    class Factory(private val rep: AdminRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminViewModel(this.rep) as T
        }
    }

    fun processData(request: ProcessDataRequest) = viewModelScope.launch {
        _data.emit(NetworkResult.Loading())
        _data.emit(rep.processData(request))
    }

    fun carRevealed(request: CarRevealedRequest) = flow {
        emit(NetworkResult.Loading())
        emit(rep.carRevealed(request))
    }

}