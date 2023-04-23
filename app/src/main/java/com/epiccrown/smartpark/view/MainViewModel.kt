package com.epiccrown.smartpark.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.epiccrown.smartpark.model.ProcessDataRequest
import com.epiccrown.smartpark.model.ProcessDataResponse
import com.epiccrown.smartpark.repository.Repository
import com.epiccrown.smartpark.repository.network.NetworkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class MainViewModel(private val rep: Repository) : ViewModel() {
    private val _data = MutableStateFlow<NetworkResult<ProcessDataResponse>>(NetworkResult.Success(null))
    val data: StateFlow<NetworkResult<ProcessDataResponse>> = _data.asStateFlow()

    class Factory(private val rep: Repository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(this.rep) as T
        }
    }

    fun processData(request: ProcessDataRequest) = viewModelScope.launch {
        _data.emit(NetworkResult.Loading())
        _data.emit(rep.processData(request))
    }

    private suspend fun awaitAll(vararg jobs: Job) {
        jobs.asList().joinAll()
    }

}