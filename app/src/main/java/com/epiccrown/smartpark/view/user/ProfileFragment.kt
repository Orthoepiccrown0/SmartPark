package com.epiccrown.smartpark.view.user

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.FragmentProfileUserBinding
import com.epiccrown.smartpark.databinding.ItemUserCarBinding
import com.epiccrown.smartpark.model.response.UserInfoResponse
import com.epiccrown.smartpark.repository.UserRepository
import com.epiccrown.smartpark.repository.network.NetworkResult
import com.epiccrown.smartpark.utils.preferences.UserPreferences
import com.epiccrown.smartpark.view.MainActivity
import com.epiccrown.smartpark.view.base.BaseFragment
import com.epiccrown.smartpark.view.common.LoadingDialog
import com.epiccrown.smartpark.view.common.TextInputDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ProfileFragment : BaseFragment() {
    private lateinit var pf: UserPreferences
    private lateinit var binding: FragmentProfileUserBinding
    private val vm: ProfileViewModel by activityViewModels {
        ProfileViewModel.Factory(
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

    private fun setResult(data: UserInfoResponse?) {
        if (data != null) {
            binding.userId.text = "UserID: ${data.idUser}"
            binding.userPhone.text = data.phone

            setCars(data.cars)
        }
    }

    private fun setCars(cars: List<UserInfoResponse.Car>) {
        binding.carsContainer.removeAllViews()
        if (cars.isNotEmpty()) {
            cars.forEachIndexed { index, car ->
                val item = ItemUserCarBinding.inflate(layoutInflater, binding.carsContainer, true)
                val isNotRemovable = index == 0 && cars.size == 1
                if (isNotRemovable) {
                    item.icon.visibility = View.GONE
                }

                item.title.text = car.plate
                item.root.setOnClickListener {
                    if (!isNotRemovable) {
                        //todo: remove car
                    }
                }
            }

            val item = ItemUserCarBinding.inflate(layoutInflater, binding.carsContainer, true)
            item.icon.setImageResource(R.drawable.baseline_add_circle_24)
            item.icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green))
            item.title.text = getString(R.string.add_car)
            item.root.setOnClickListener {
                TextInputDialog.showDialog(
                    context = requireContext(),
                    title = R.string.il_tuo_nuovo_veicolo,
                    subtitle = R.string.car_insert_disclaimer,
                    hint = R.string.tua_targa,
                    uppercase = true,
                    maxLenght = 7,
                ) {
                    if (it.length == 7) {
                        val loading = LoadingDialog(requireContext())
                        viewLifecycleOwner.lifecycleScope.launch {
                            vm.addCar(it).collect() {
                                when (it) {
                                    is NetworkResult.Success -> {
                                        loading.dismiss()
                                        Toast.makeText(
                                            requireContext(),
                                            "Veicolo è stato aggiunto con successo!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        vm.getData()
                                    }

                                    is NetworkResult.Error -> {
                                        loading.dismiss()

                                    }

                                    is NetworkResult.Loading -> {
                                        loading.show()
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Targa inserita errata", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        } else {
            //todo: empty car list?
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
        binding.logout.setOnClickListener {
            pf.clear()
            startActivity(Intent(requireContext(), MainActivity::class.java))
            activity?.finish()
        }
    }

    override fun prepareStage() {
        vm.getData()
        pf = UserPreferences(requireContext())
    }

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentProfileUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    class ProfileViewModel(private val rep: UserRepository) : ViewModel() {
        private val _data =
            MutableStateFlow<NetworkResult<UserInfoResponse>>(NetworkResult.Success(null))
        val data: StateFlow<NetworkResult<UserInfoResponse>> = _data.asStateFlow()

        fun getData() = viewModelScope.launch {
            _data.emit(NetworkResult.Loading())
            _data.emit(rep.getUserInfo())
        }

        fun addCar(plate: String) = flow {
            emit(NetworkResult.Loading())
            emit(rep.addCar(plate))
        }

        class Factory(private val rep: UserRepository) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(this.rep) as T
            }
        }
    }
}