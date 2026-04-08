package com.myapp.creatorcollab.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myapp.creatorcollab.data.repository.CollaborationRepository

class CollabViewModelFactory(private val repository: CollaborationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CollabViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CollabViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}