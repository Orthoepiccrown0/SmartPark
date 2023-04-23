package com.epiccrown.smartpark.view.landing

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.ActivityLandingHostBinding
import com.epiccrown.smartpark.view.MainActivity

class HostActivity : AppCompatActivity() {
    private lateinit var array: java.util.ArrayList<WelcomeScreenContainer>
    private lateinit var binding: ActivityLandingHostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingHostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prepareStage()
        setListeners()
    }

    private fun setListeners() {
        binding.next.setOnClickListener {
            if (binding.pager.currentItem == array.size - 1) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                binding.pager.setCurrentItem(binding.pager.currentItem + 1, true)
            }
        }
    }

    private fun prepareStage() {
        array = arrayListOf(
            WelcomeScreenContainer(
                R.string.app_name,
                R.drawable.welcome_1,
                R.string.app_name
            ),
            WelcomeScreenContainer(
                R.string.app_name,
                R.drawable.welcome_1,
                R.string.app_name
            ),
            WelcomeScreenContainer(
                R.string.app_name,
                R.drawable.welcome_1,
                R.string.app_name
            )
        )

        val adapter = ScreenSlidePagerAdapter(array, this)
        binding.pager.adapter = adapter
        binding.dots.attachTo(binding.pager)
    }

    data class WelcomeScreenContainer(
        val title: Int,
        val image: Int,
        val content: Int,
    )

    class ScreenSlidePagerAdapter(
        val data: ArrayList<WelcomeScreenContainer>,
        fa: FragmentActivity
    ) :
        FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = data.size

        override fun createFragment(position: Int): Fragment {
            val fragment = WelcomeMessage()
            val item = data[position]
            WelcomeMessage.setArgs(fragment, item.title, item.image, item.content)
            return fragment
        }
    }
}