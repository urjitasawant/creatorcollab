package com.myapp.creatorcollab.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.myapp.creatorcollab.model.CollabType

@Dao
interface CollabDao {
    @Query("SELECT * FROM collaborations ORDER BY deadline ASC")
    fun getAllCollaborations(): LiveData<List<CollabEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollab(collab: CollabEntity)

    @Update
    suspend fun updateCollab(collab: CollabEntity)

    @Delete
    suspend fun deleteCollab(collab: CollabEntity)

    @Query("SELECT * FROM collaborations WHERE collabType = :type")
    fun getCollabsByType(type: CollabType): LiveData<List<CollabEntity>>

    @Query("SELECT * FROM collaborations ORDER BY id DESC LIMIT 5")
    fun getRecentCollaborations(): LiveData<List<CollabEntity>>

    @Query("SELECT DISTINCT agency FROM collaborations WHERE agency IS NOT NULL AND agency != ''")
    fun getDistinctAgencies(): LiveData<List<String>>

    @Query("SELECT DISTINCT brandName FROM collaborations WHERE brandName IS NOT NULL AND brandName != ''")
    fun getDistinctBrands(): LiveData<List<String>>

    @Query("SELECT * FROM collaborations WHERE paymentMode = 'REIMBURSEMENT' AND isReimbursementReceived = 0")
    fun getPendingReimbursements(): LiveData<List<CollabEntity>>

    @Query("SELECT * FROM collaborations WHERE isPosted = 0")
    fun getPendingCampaigns(): LiveData<List<CollabEntity>>

    @Query("SELECT * FROM collaborations WHERE isPosted = 1 AND ( (collabType = 'PAID' AND isPaymentReceived = 1) OR (collabType = 'BARTER') )")
    fun getCompletedCampaigns(): LiveData<List<CollabEntity>>

    @Query("SELECT * FROM collaborations WHERE brandName = :brandName")
    fun getCollabsByBrand(brandName: String): LiveData<List<CollabEntity>>

    @Query("SELECT * FROM collaborations WHERE collabType = 'PAID' AND isPaymentReceived = 1")
    fun getReceivedPayouts(): LiveData<List<CollabEntity>>
}