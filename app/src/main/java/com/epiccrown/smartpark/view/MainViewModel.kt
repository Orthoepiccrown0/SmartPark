package com.epiccrown.smartpark.view

import androidx.lifecycle.ViewModel
import com.epiccrown.smartpark.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll

class MainViewModel(private val rep: UserRepository) : ViewModel() {


    private suspend fun awaitAll(vararg jobs: Job) {
        jobs.asList().joinAll()
    }

}