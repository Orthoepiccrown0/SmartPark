package com.epiccrown.smartpark.view.auth

import android.view.View
import com.epiccrown.smartpark.databinding.ActivityUserLoginBinding
import com.epiccrown.smartpark.view.base.BaseActivity
import kotlinx.coroutines.CoroutineScope

class UserLoginActivity : BaseActivity() {
    private lateinit var binding: ActivityUserLoginBinding

    override fun CoroutineScope.observeStuff() {

    }

    override fun prepareStage() {
    }

    private fun isDataOk(): Boolean {
        var valid = true

        if (binding.phone.text.length != 10) {
            valid = false
        }

        if (binding.plate.text.length != 6) {
            valid = false
        }

        return valid
    }

    override fun setListeners() {
        binding.confirm.setOnClickListener {
            if (isDataOk()) {

            } else {
                //todo: show error
            }
        }
    }

    override fun getActivityView(): View {
        binding = ActivityUserLoginBinding.inflate(layoutInflater)
        return binding.root
    }
}