package com.epiccrown.smartpark.view.admin

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.ActivityAdminConfigurationBinding
import com.epiccrown.smartpark.model.response.HomeResponse
import com.epiccrown.smartpark.repository.UserRepository
import com.epiccrown.smartpark.repository.network.NetworkResult
import com.epiccrown.smartpark.utils.preferences.UserPreferences
import com.epiccrown.smartpark.view.base.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminConfigurationActivity : BaseActivity() {
    private lateinit var pf: UserPreferences
    private lateinit var binding: ActivityAdminConfigurationBinding

    override fun CoroutineScope.observeStuff() {

    }

    override fun prepareStage() {
        pf = UserPreferences(this)
        if (pf.getAdminConfiguration() != null) {
            binding.title.text = getString(R.string.configurazione)
        }
    }

    override fun setListeners() {
        binding.back.setOnClickListener {
            finish()
        }

        binding.confirm.setOnClickListener {

        }

        binding.addPark.setOnClickListener {
            NewParkDialog().show(supportFragmentManager, null)
        }
    }

    override fun getActivityView(): View {
        binding = ActivityAdminConfigurationBinding.inflate(layoutInflater)
        return binding.root
    }

    class AdminConfigViewModel(private val rep: UserRepository) : ViewModel() {
        private val _data =
            MutableStateFlow<NetworkResult<HomeResponse>>(NetworkResult.Success(null))
        val data: StateFlow<NetworkResult<HomeResponse>> = _data.asStateFlow()

        fun getData() = viewModelScope.launch {
            _data.emit(NetworkResult.Loading())
            _data.emit(rep.getHomePage())
        }

        class Factory(private val rep: UserRepository) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminConfigViewModel(this.rep) as T
            }
        }
    }
}