package com.epiccrown.smartpark.view.auth

import android.content.Intent
import android.view.View
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.ActivityUserSelectorBinding
import com.epiccrown.smartpark.utils.preferences.UserPreferences
import com.epiccrown.smartpark.view.MainActivity
import com.epiccrown.smartpark.view.base.BaseActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.Tab
import kotlinx.coroutines.CoroutineScope

class UserSelectorActivity : BaseActivity() {
    private lateinit var binding: ActivityUserSelectorBinding
    private var selectedUser = UserType.ADMIN
    private val pf = UserPreferences(this)
    override fun CoroutineScope.observeStuff() {

    }

    override fun prepareStage() {

        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(R.string.gestore).setTag(UserType.ADMIN)
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(R.string.utente).setTag(UserType.USER)
        )
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) {
                if (tab?.tag == UserType.ADMIN) {
                    binding.userLayout.visibility = View.GONE
                    binding.adminLayout.visibility = View.VISIBLE
                } else {
                    binding.userLayout.visibility = View.VISIBLE
                    binding.adminLayout.visibility = View.GONE
                }
                if (tab != null) {
                    selectedUser = tab.tag as UserType
                }
            }

            override fun onTabUnselected(tab: Tab?) {

            }

            override fun onTabReselected(tab: Tab?) {

            }

        })
    }

    override fun setListeners() {
        binding.confirm.setOnClickListener {
            pf.setAdmin(selectedUser == UserType.ADMIN)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun getActivityView(): View {
        binding = ActivityUserSelectorBinding.inflate(layoutInflater)
        return binding.root
    }

    enum class UserType {
        ADMIN,
        USER
    }
}