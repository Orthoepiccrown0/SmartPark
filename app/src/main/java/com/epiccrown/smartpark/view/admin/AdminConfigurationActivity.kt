package com.epiccrown.smartpark.view.admin

import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.ActivityAdminConfigurationBinding
import com.epiccrown.smartpark.model.internal.AdminConfiguration
import com.epiccrown.smartpark.model.request.AddParkRequest
import com.epiccrown.smartpark.model.request.AddZoneRequest
import com.epiccrown.smartpark.model.response.AllZonesResponse
import com.epiccrown.smartpark.repository.AdminRepository
import com.epiccrown.smartpark.repository.network.NetworkResult
import com.epiccrown.smartpark.utils.preferences.UserPreferences
import com.epiccrown.smartpark.view.base.BaseActivity
import com.epiccrown.smartpark.view.common.LoadingDialog
import com.epiccrown.smartpark.view.common.SingleSelectionPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class AdminConfigurationActivity : BaseActivity() {
    private lateinit var pf: UserPreferences
    private lateinit var binding: ActivityAdminConfigurationBinding
    private var selectedPark: AllZonesResponse.Zone.Park? = null
    private var selectedZone: AllZonesResponse.Zone? = null

    private val vm: AdminConfigViewModel by viewModels {
        AdminConfigViewModel.Factory(
            AdminRepository()
        )
    }


    private lateinit var loading: LoadingDialog

    override fun CoroutineScope.observeStuff() {
        launch {
            vm.data.collect() {
                when (it) {
                    is NetworkResult.Loading -> {
                        loading.show()
                    }

                    is NetworkResult.Success -> {
                        loading.dismiss()
                        setData(it.data)
                    }

                    is NetworkResult.Error -> {
                        loading.dismiss()
                        //todo: show error
                    }
                }
            }
        }
    }

    private fun openZoneSelector(zones: List<AllZonesResponse.Zone>) {
        if (zones.isNotEmpty()) {
            SingleSelectionPicker(
                data = zones.toTypedArray(),
                title = R.string.select_zone,
                subtitle = R.string.select_zone_subheader,
                selection = {
                    selectedZone = it
                    selectedPark = null
                    binding.selectedZone.text = it.zoneName
                    binding.selectedPark.setText(R.string.seleziona)

                    checkData()
                }).show(supportFragmentManager, null)
        } else {
            Toast.makeText(this, getString(R.string.no_zone), Toast.LENGTH_SHORT).show()
        }

    }

    private fun openParkSelector(parks: List<AllZonesResponse.Zone.Park>?) {
        if (parks != null) {
            SingleSelectionPicker(
                data = parks.toTypedArray(),
                title = R.string.select_park,
                subtitle = R.string.select_park_subheader,
                selection = {
                    selectedPark = it
                    binding.selectedPark.text = it.name

                    checkData()
                }).show(supportFragmentManager, null)
        } else {
            Toast.makeText(this, getString(R.string.no_park), Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkData() {
        var isValid = true

        if (selectedPark == null)
            isValid = false

        if (selectedZone == null)
            isValid = false

        binding.confirm.isEnabled = isValid
    }

    private fun setData(data: AllZonesResponse?) {
        if (data != null) {
            val selectedConfig = pf.getAdminConfiguration()

            if (!vm.isUpdating && selectedConfig != null) {
                binding.selectedPark.text = selectedConfig.selectedPark.name
                binding.selectedZone.text = selectedConfig.selectedZone.zoneName
                selectedPark = selectedConfig.selectedPark
                selectedZone = selectedConfig.selectedZone
            }


            binding.selectedPark.setOnClickListener {
                if (selectedZone != null) {
                    binding.selectedZone.setBackgroundResource(R.drawable.drawable_combobox)
                    openParkSelector(data.zones.find { it.idZone == selectedZone?.idZone }?.parks)
                } else {
                    binding.selectedZone.setBackgroundResource(R.drawable.drawable_combobox_error)
                }
            }

            binding.selectedZone.setOnClickListener {
                openZoneSelector(data.zones)
            }

        }
    }


    override fun prepareStage() {
        loading = LoadingDialog(this)
        pf = UserPreferences(this)
        vm.getData()
        if (pf.getAdminConfiguration() != null) {
            binding.title.text = getString(R.string.configurazione)
        }
    }

    override fun setListeners() {
        binding.back.setOnClickListener {
            finish()
        }

        binding.confirm.setOnClickListener {
            pf.setAdminConfig(AdminConfiguration(selectedZone!!, selectedPark!!))
            setResult(RESULT_OK)
            finish()
        }

        binding.addPark.setOnClickListener {
            if (selectedZone == null) {
                binding.selectedZone.setBackgroundResource(R.drawable.drawable_combobox_error)
            } else {
                binding.selectedZone.setBackgroundResource(R.drawable.drawable_combobox)
                NewParkDialog(selectedZone!!.idZone) {
                    addPark(it)
                }.show(supportFragmentManager, null)
            }
        }

        binding.addZone.setOnClickListener {
            NewZoneDialog() {
                addZone(it)
            }.show(supportFragmentManager, null)
        }
    }

    private fun addPark(it: AddParkRequest) {
        launch {
            vm.addPark(it).collect() {
                when (it) {
                    is NetworkResult.Loading -> {
                        loading.show()
                    }

                    is NetworkResult.Success -> {
                        vm.updateData()
                    }

                    is NetworkResult.Error -> {
                        //todo: show error
                    }
                }
            }
        }
    }

    private fun addZone(it: AddZoneRequest) {
        launch {
            vm.addZone(it).collect() {
                when (it) {
                    is NetworkResult.Loading -> {
                        loading.show()
                    }

                    is NetworkResult.Success -> {
                        vm.updateData()
                    }

                    is NetworkResult.Error -> {
                        //todo: show error
                    }
                }
            }
        }
    }

    override fun getActivityView(): View {
        binding = ActivityAdminConfigurationBinding.inflate(layoutInflater)
        return binding.root
    }

    class AdminConfigViewModel(private val rep: AdminRepository) : ViewModel() {
        private val _data =
            MutableStateFlow<NetworkResult<AllZonesResponse>>(NetworkResult.Success(null))
        val data: StateFlow<NetworkResult<AllZonesResponse>> = _data.asStateFlow()
        var isUpdating = false
        fun getData() = viewModelScope.launch {
            _data.emit(NetworkResult.Loading())
            _data.emit(rep.getAllZones())
        }

        fun addPark(request: AddParkRequest) = flow {
            emit(NetworkResult.Loading())
            emit(rep.addPark(request))
        }

        fun addZone(request: AddZoneRequest) = flow {
            emit(NetworkResult.Loading())
            emit(rep.addZone(request))
        }

        fun updateData() {
            isUpdating = true
            getData()
        }

        class Factory(private val rep: AdminRepository) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminConfigViewModel(this.rep) as T
            }
        }
    }
}