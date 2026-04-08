package com.myapp.creatorcollab.ui.viewmodels

import androidx.lifecycle.*
import com.myapp.creatorcollab.data.local.CollabEntity
import com.myapp.creatorcollab.data.repository.CollaborationRepository
import com.myapp.creatorcollab.data.repository.DashboardStats
import com.myapp.creatorcollab.model.CollabType
import kotlinx.coroutines.launch

class CollabViewModel(private val repository: CollaborationRepository) : ViewModel() {

    val allCollaborations: LiveData<List<CollabEntity>> = repository.allCollaborations
    
    val recentCollaborations: LiveData<List<CollabEntity>> = repository.recentCollaborations
    
    val dashboardStats: LiveData<DashboardStats> = repository.dashboardStats
    
    val distinctAgencies: LiveData<List<String>> = repository.getDistinctAgencies()
    
    val distinctBrands: LiveData<List<String>> = repository.getDistinctBrands()
    
    val pendingReimbursements: LiveData<List<CollabEntity>> = repository.getPendingReimbursements()

    fun insert(collab: CollabEntity) = viewModelScope.launch {
        repository.insert(collab)
    }

    fun update(collab: CollabEntity) = viewModelScope.launch {
        repository.update(collab)
    }

    fun delete(collab: CollabEntity) = viewModelScope.launch {
        repository.delete(collab)
    }

    fun getFilteredCollabs(type: CollabType): LiveData<List<CollabEntity>> {
        return repository.getCollabsByType(type)
    }

    fun getPendingCampaigns(): LiveData<List<CollabEntity>> {
        return allCollaborations.map { list ->
            list.filter { !it.isPosted }
        }
    }

    fun getCompletedCampaigns(): LiveData<List<CollabEntity>> {
        return allCollaborations.map { list ->
            list.filter { 
                it.isPosted && (it.collabType == CollabType.BARTER || it.isPaymentReceived)
            }
        }
    }

    fun getCollabsByBrand(brandName: String): LiveData<List<CollabEntity>> {
        return repository.getCollabsByBrand(brandName)
    }

    fun getReceivedPayouts(): LiveData<List<CollabEntity>> {
        return repository.getReceivedPayouts()
    }
}