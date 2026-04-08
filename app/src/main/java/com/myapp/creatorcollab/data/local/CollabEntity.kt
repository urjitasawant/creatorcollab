package com.myapp.creatorcollab.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.myapp.creatorcollab.model.CollabType
import com.myapp.creatorcollab.model.PaymentMode
import java.util.Date

@Entity(tableName = "collaborations")
data class CollabEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val brandName: String,
    val platform: String, 
    val deadline: Date,
    val arrivalDate: Date? = null,
    val mandateDate: Date? = null,
    
    // Deliverables (Platform Options)
    val hasInstaReel: Boolean = false,
    val hasInstaCarousel: Boolean = false,
    val hasInstaStories: Boolean = false,
    val hasYouTubeShorts: Boolean = false,
    
    // Financials
    val collabType: CollabType, 
    val paymentMode: PaymentMode?,
    val cashAmount: Double = 0.0,
    val productValue: Double = 0.0,
    val reimbursementAmount: Double = 0.0,
    
    // Meta Data
    val agency: String? = null,
    val notes: String? = null,
    
    // Workflow Tracking
    val isProductReceived: Boolean = false,
    val isMandateReceived: Boolean = false,
    val isVideoShot: Boolean = false,
    val isVideoEdited: Boolean = false,
    val isSentForApproval: Boolean = false,
    val isApproved: Boolean = false,
    val isPosted: Boolean = false,
    val isPaymentReceived: Boolean = false,
    val isReimbursementReceived: Boolean = false,
    
    // Deliverable completion
    val isInstaReelDone: Boolean = false,
    val isInstaCarouselDone: Boolean = false,
    val isInstaStoriesDone: Boolean = false,
    val isYouTubeShortsDone: Boolean = false
) {
    fun getProgressPercent(): Int {
        val stages = mutableListOf<Boolean>()
        stages.add(isProductReceived)
        stages.add(isMandateReceived)
        stages.add(isVideoShot)
        stages.add(isVideoEdited)
        stages.add(isSentForApproval)
        stages.add(isApproved)
        stages.add(isPosted)
        
        if (hasInstaReel) stages.add(isInstaReelDone)
        if (hasInstaCarousel) stages.add(isInstaCarouselDone)
        if (hasInstaStories) stages.add(isInstaStoriesDone)
        if (hasYouTubeShorts) stages.add(isYouTubeShortsDone)
        
        if (collabType == CollabType.PAID) {
            stages.add(isPaymentReceived)
        }
        
        if (paymentMode == PaymentMode.REIMBURSEMENT) {
            stages.add(isReimbursementReceived)
        }
        
        val totalStages = stages.size
        val completedStages = stages.count { it }
        return if (totalStages == 0) 0 else (completedStages * 100) / totalStages
    }
}