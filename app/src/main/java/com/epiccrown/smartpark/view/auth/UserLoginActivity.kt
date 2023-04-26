package com.epiccrown.smartpark.view.auth

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.ActivityUserLoginBinding
import com.epiccrown.smartpark.model.request.LoginRequest
import com.epiccrown.smartpark.repository.AuthRepository
import com.epiccrown.smartpark.repository.network.NetworkResult
import com.epiccrown.smartpark.utils.preferences.UserPreferences
import com.epiccrown.smartpark.view.MainActivity
import com.epiccrown.smartpark.view.base.BaseActivity
import com.epiccrown.smartpark.view.common.LoadingDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class UserLoginActivity : BaseActivity() {
    private lateinit var binding: ActivityUserLoginBinding
    private val vm: UserLoginViewModel by viewModels { UserLoginViewModel.Factory(AuthRepository()) }
    private val pf = UserPreferences(this)
    private lateinit var  intentMain :Intent
    override fun CoroutineScope.observeStuff() {

    }

    override fun prepareStage() {
        intentMain = Intent(this, MainActivity::class.java)
    }

    private fun isDataOk(): Boolean {
        var valid = true

//        if (binding.phone.text.length != 10) {
//            valid = false
//        }

        if (binding.plate.text.length < 7) {
            valid = false
        }

        return valid
    }

    override fun setListeners() {
        binding.confirm.setOnClickListener {
            if (isDataOk()) {
                login()
                binding.error.visibility = View.GONE
            } else {
                binding.error.visibility = View.VISIBLE
            }
        }
    }

    private fun login() {
        val request = LoginRequest(
            phone = "+39${binding.phone.text}",
            plate = binding.plate.text.toString()
        )
        val loading = LoadingDialog(this)
        lifecycleScope.launch {
            vm.login(request).collect() {
                when (it) {
                    is NetworkResult.Loading -> {
                        loading.show()
                    }

                    is NetworkResult.Success -> {
                        loading.dismiss()
                        if (it.data != null) {
                            pf.setUser(it.data)
                            startActivity(intentMain)
                            finish()
                        } else
                            showError()
                    }

                    is NetworkResult.Error -> {
                        loading.dismiss()
                        showError()
                    }
                }
            }
        }
    }

    private fun showError() {
        Toast.makeText(
            this@UserLoginActivity,
            getString(R.string.login_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun getActivityView(): View {
        binding = ActivityUserLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    class UserLoginViewModel(private val rep: AuthRepository) : ViewModel() {
        class Factory(private val rep: AuthRepository) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UserLoginViewModel(this.rep) as T
            }
        }

        fun login(request: LoginRequest) = flow {
            emit(NetworkResult.Loading())
            emit(rep.login(request))
        }
    }
}