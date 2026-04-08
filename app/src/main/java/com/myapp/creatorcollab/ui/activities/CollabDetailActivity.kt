package com.myapp.creatorcollab.ui.activities

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.myapp.creatorcollab.data.local.AppDatabase
import com.myapp.creatorcollab.data.local.CollabEntity
import com.myapp.creatorcollab.data.repository.CollaborationRepository
import com.myapp.creatorcollab.databinding.ActivityCollabDetailBinding
import com.myapp.creatorcollab.model.CollabType
import com.myapp.creatorcollab.model.PaymentMode
import com.myapp.creatorcollab.ui.viewmodels.CollabViewModel
import com.myapp.creatorcollab.ui.viewmodels.CollabViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class CollabDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCollabDetailBinding
    private val viewModel: CollabViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = CollaborationRepository(database.collabDao())
        CollabViewModelFactory(repository)
    }

    private var currentCollab: CollabEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollabDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val collabId = intent.getIntExtra("EXTRA_ID", -1)
        if (collabId == -1) {
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        viewModel.allCollaborations.observe(this) { collabs ->
            currentCollab = collabs.find { it.id == collabId }
            currentCollab?.let { setupUI(it) }
        }

        setupChecklistListeners()
        
        binding.buttonEditCollab.setOnClickListener {
            currentCollab?.let { collab ->
                val intent = Intent(this, AddEditCollabActivity::class.java).apply {
                    putExtra("EXTRA_ID", collab.id)
                    putExtra("EXTRA_BRAND", collab.brandName)
                    putExtra("EXTRA_TYPE", collab.collabType.name)
                    putExtra("EXTRA_CASH_AMOUNT", collab.cashAmount)
                    putExtra("EXTRA_PRODUCT_VALUE", collab.productValue)
                    putExtra("EXTRA_REIMB_AMOUNT", collab.reimbursementAmount)
                    putExtra("EXTRA_PLATFORM", collab.platform)
                    putExtra("EXTRA_AGENCY", collab.agency)
                    putExtra("EXTRA_NOTES", collab.notes)
                    putExtra("EXTRA_DEADLINE", collab.deadline.time)
                    putExtra("EXTRA_ARRIVAL_DATE", collab.arrivalDate?.time ?: 0L)
                    putExtra("EXTRA_MANDATE_DATE", collab.mandateDate?.time ?: 0L)
                    putExtra("EXTRA_PAYMENT_MODE", collab.paymentMode?.name)
                    // Deliverables selection
                    putExtra("EXTRA_HAS_REEL", collab.hasInstaReel)
                    putExtra("EXTRA_HAS_CAROUSEL", collab.hasInstaCarousel)
                    putExtra("EXTRA_HAS_STORIES", collab.hasInstaStories)
                    putExtra("EXTRA_HAS_SHORTS", collab.hasYouTubeShorts)
                }
                startActivity(intent)
            }
        }

        binding.buttonDelete.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun setupUI(collab: CollabEntity) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.apply {
            textViewBrandDetail.text = collab.brandName
            textViewAgencyDetail.text = if (collab.agency.isNullOrEmpty()) "No Agency" else "Agency: ${collab.agency}"
            textViewNotesDetail.text = if (collab.notes.isNullOrEmpty()) "No internal notes added." else collab.notes
            
            val valueText = if (collab.collabType == CollabType.PAID) {
                "₹${collab.cashAmount} Cash + ₹${collab.productValue} MRP - (${collab.paymentMode?.name})"
            } else {
                "MRP: ₹${collab.productValue} - Barter"
            }
            textViewValueDetail.text = valueText

            textViewArrivalDetail.text = "Expected Arrival: ${collab.arrivalDate?.let { dateFormat.format(it) } ?: "N/A"}"
            textViewMandateDetail.text = "Mandate Shared: ${collab.mandateDate?.let { dateFormat.format(it) } ?: "N/A"}"
            textViewDeadlineDetail.text = "Deadline: ${dateFormat.format(collab.deadline)}"

            val progress = collab.getProgressPercent()
            animateProgressBar(progress)
            textViewProgressPercentDetail.text = "$progress% Complete"

            checkProductReceived.isChecked = collab.isProductReceived
            checkMandateReceived.isChecked = collab.isMandateReceived
            
            // Deliverables Checklist Visibility
            checkInstaReelDone.visibility = if (collab.hasInstaReel) View.VISIBLE else View.GONE
            checkInstaReelDone.isChecked = collab.isInstaReelDone

            checkInstaCarouselDone.visibility = if (collab.hasInstaCarousel) View.VISIBLE else View.GONE
            checkInstaCarouselDone.isChecked = collab.isInstaCarouselDone

            checkInstaStoriesDone.visibility = if (collab.hasInstaStories) View.VISIBLE else View.GONE
            checkInstaStoriesDone.isChecked = collab.isInstaStoriesDone

            checkYouTubeShortsDone.visibility = if (collab.hasYouTubeShorts) View.VISIBLE else View.GONE
            checkYouTubeShortsDone.isChecked = collab.isYouTubeShortsDone

            checkVideoShot.isChecked = collab.isVideoShot
            checkVideoEdited.isChecked = collab.isVideoEdited
            checkSentForApproval.isChecked = collab.isSentForApproval
            checkApproved.isChecked = collab.isApproved
            checkPosted.isChecked = collab.isPosted
            
            // Payout Received Visibility Logic
            if (collab.collabType == CollabType.PAID) {
                checkPaymentReceived.visibility = View.VISIBLE
                checkPaymentReceived.isChecked = collab.isPaymentReceived
            } else {
                checkPaymentReceived.visibility = View.GONE
            }
            
            if (collab.paymentMode == PaymentMode.REIMBURSEMENT) {
                checkReimbursementReceived.visibility = View.VISIBLE
                checkReimbursementReceived.isChecked = collab.isReimbursementReceived
            } else {
                checkReimbursementReceived.visibility = View.GONE
            }
        }
    }

    private fun animateProgressBar(progress: Int) {
        ObjectAnimator.ofInt(binding.progressIndicatorDetail, "progress", progress).apply {
            duration = 500
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun setupChecklistListeners() {
        val clickListener = View.OnClickListener {
            currentCollab?.let { collab ->
                val updatedCollab = collab.copy(
                    isProductReceived = binding.checkProductReceived.isChecked,
                    isMandateReceived = binding.checkMandateReceived.isChecked,
                    isInstaReelDone = binding.checkInstaReelDone.isChecked,
                    isInstaCarouselDone = binding.checkInstaCarouselDone.isChecked,
                    isInstaStoriesDone = binding.checkInstaStoriesDone.isChecked,
                    isYouTubeShortsDone = binding.checkYouTubeShortsDone.isChecked,
                    isVideoShot = binding.checkVideoShot.isChecked,
                    isVideoEdited = binding.checkVideoEdited.isChecked,
                    isSentForApproval = binding.checkSentForApproval.isChecked,
                    isApproved = binding.checkApproved.isChecked,
                    isPosted = binding.checkPosted.isChecked,
                    isPaymentReceived = binding.checkPaymentReceived.isChecked,
                    isReimbursementReceived = binding.checkReimbursementReceived.isChecked
                )
                viewModel.update(updatedCollab)
            }
        }

        binding.apply {
            checkProductReceived.setOnClickListener(clickListener)
            checkMandateReceived.setOnClickListener(clickListener)
            checkInstaReelDone.setOnClickListener(clickListener)
            checkInstaCarouselDone.setOnClickListener(clickListener)
            checkInstaStoriesDone.setOnClickListener(clickListener)
            checkYouTubeShortsDone.setOnClickListener(clickListener)
            checkVideoShot.setOnClickListener(clickListener)
            checkVideoEdited.setOnClickListener(clickListener)
            checkSentForApproval.setOnClickListener(clickListener)
            checkApproved.setOnClickListener(clickListener)
            checkPosted.setOnClickListener(clickListener)
            checkPaymentReceived.setOnClickListener(clickListener)
            checkReimbursementReceived.setOnClickListener(clickListener)
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Collaboration")
            .setMessage("Are you sure you want to delete this campaign?")
            .setPositiveButton("Delete") { _, _ ->
                currentCollab?.let {
                    viewModel.delete(it)
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}