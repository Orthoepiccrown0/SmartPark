package com.epiccrown.smartpark.view.landing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.epiccrown.smartpark.databinding.FragmentWelcomeMessageBinding
import com.epiccrown.smartpark.view.base.BaseFragment
import kotlinx.coroutines.CoroutineScope

class WelcomeMessage : BaseFragment() {
    private lateinit var binding: FragmentWelcomeMessageBinding
    override fun CoroutineScope.start() {

    }

    override fun setListeners() {

    }

    override fun prepareStage() {
        val title = arguments?.getInt(TITLE)
        val image = arguments?.getInt(IMAGE)
        val content = arguments?.getInt(CONTENT)
        if (title != null && image != null && content != null) {
            binding.title.text = getString(title)
            binding.image.setImageResource(image)
            binding.content.text = getString(content)
        }
    }

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentWelcomeMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        private const val TITLE = "TITLE"
        private const val IMAGE = "IMAGE"
        private const val CONTENT = "CONTENT"

        fun setArgs(fragment: Fragment, title: Int, image: Int, content: Int) {
            fragment.apply {
                arguments?.putInt(TITLE, title)
                arguments?.putInt(IMAGE, image)
                arguments?.putInt(CONTENT, content)
            }
        }
    }
}