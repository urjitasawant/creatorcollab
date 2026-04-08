package com.myapp.creatorcollab.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.myapp.creatorcollab.data.local.CollabDao
import com.myapp.creatorcollab.data.local.CollabEntity
import com.myapp.creatorcollab.model.CollabType
import com.myapp.creatorcollab.model.PaymentMode

data class BrandValue(val name: String, val value: Double)

data class DashboardStats(
    val paidCount: Int,
    val barterCount: Int,
    val totalCashPayoutReceived: Double,
    val totalReimbursementPending: Double,
    val totalProductValueBarter: Double,
    val barterBrandValues: List<BrandValue>,
    val paidBrandValues: List<BrandValue>
)

class CollaborationRepository(private val collabDao: CollabDao) {

    val allCollaborations: LiveData<List<CollabEntity>> = collabDao.getAllCollaborations()
    
    val recentCollaborations: LiveData<List<CollabEntity>> = collabDao.getRecentCollaborations()

    val dashboardStats: LiveData<DashboardStats> = allCollaborations.map { list ->
        val paidList = list.filter { it.collabType == CollabType.PAID }
        val barterList = list.filter { it.collabType == CollabType.BARTER }

        val cashEarned = paidList.filter { it.isPaymentReceived }.sumOf { it.cashAmount }
        val pendingReimb = list.filter { it.paymentMode == PaymentMode.REIMBURSEMENT && !it.isReimbursementReceived }
            .sumOf { it.reimbursementAmount }
        val barterValTotal = barterList.sumOf { it.productValue }

        val barterBrands = barterList.groupBy { it.brandName }
            .map { (name, collabs) -> BrandValue(name, collabs.sumOf { it.productValue }) }
            .sortedByDescending { it.value }

        val paidBrands = paidList.groupBy { it.brandName }
            .map { (name, collabs) -> BrandValue(name, collabs.sumOf { it.cashAmount }) }
            .sortedByDescending { it.value }

        DashboardStats(
            paidCount = paidList.size,
            barterCount = barterList.size,
            totalCashPayoutReceived = cashEarned,
            totalReimbursementPending = pendingReimb,
            totalProductValueBarter = barterValTotal,
            barterBrandValues = barterBrands,
            paidBrandValues = paidBrands
        )
    }

    fun getDistinctAgencies(): LiveData<List<String>> = collabDao.getDistinctAgencies()
    
    fun getDistinctBrands(): LiveData<List<String>> = collabDao.getDistinctBrands()
    
    fun getPendingReimbursements(): LiveData<List<CollabEntity>> = collabDao.getPendingReimbursements()

    fun getCollabsByBrand(brandName: String): LiveData<List<CollabEntity>> = collabDao.getCollabsByBrand(brandName)

    fun getReceivedPayouts(): LiveData<List<CollabEntity>> = collabDao.getReceivedPayouts()

    suspend fun insert(collab: CollabEntity) = collabDao.insertCollab(collab)
    suspend fun update(collab: CollabEntity) = collabDao.updateCollab(collab)
    suspend fun delete(collab: CollabEntity) = collabDao.deleteCollab(collab)
    fun getCollabsByType(type: CollabType) = collabDao.getCollabsByType(type)
}