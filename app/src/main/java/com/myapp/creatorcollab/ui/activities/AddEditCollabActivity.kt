package com.myapp.creatorcollab.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.myapp.creatorcollab.data.local.AppDatabase
import com.myapp.creatorcollab.data.local.CollabEntity
import com.myapp.creatorcollab.data.repository.CollaborationRepository
import com.myapp.creatorcollab.databinding.ActivityAddEditCollabBinding
import com.myapp.creatorcollab.model.CollabType
import com.myapp.creatorcollab.model.PaymentMode
import com.myapp.creatorcollab.ui.viewmodels.CollabViewModel
import com.myapp.creatorcollab.ui.viewmodels.CollabViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class AddEditCollabActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditCollabBinding
    private val viewModel: CollabViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = CollaborationRepository(database.collabDao())
        CollabViewModelFactory(repository)
    }

    private var deadlineDate: Date = Date()
    private var arrivalDate: Date? = null
    private var mandateDate: Date? = null
    
    private var collabId: Int = 0
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditCollabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupDatePickers()
        setupSuggestions()
        setupLogic()
        checkIntent()

        binding.buttonSave.setOnClickListener { saveCollab() }
    }

    private fun setupSuggestions() {
        viewModel.distinctAgencies.observe(this) { agencies ->
            val agencyAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, agencies)
            binding.autoCompleteAgency.setAdapter(agencyAdapter)
        }
        
        viewModel.distinctBrands.observe(this) { brands ->
            val brandAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, brands)
            binding.editTextBrand.setAdapter(brandAdapter)
        }
    }

    private fun setupLogic() {
        binding.toggleGroupType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                binding.layoutPaidAmount.visibility = if (checkedId == binding.buttonPaid.id) View.VISIBLE else View.GONE
            }
        }

        binding.toggleGroupPayment.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                binding.layoutReimbAmount.visibility = if (checkedId == binding.buttonReimbursement.id) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupDatePickers() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        binding.editTextDeadline.setOnClickListener {
            showDatePicker { date ->
                deadlineDate = date
                binding.editTextDeadline.setText(dateFormat.format(date))
            }
        }

        binding.editTextArrivalDate.setOnClickListener {
            showDatePicker { date ->
                arrivalDate = date
                binding.editTextArrivalDate.setText(dateFormat.format(date))
            }
        }

        binding.editTextMandateDate.setOnClickListener {
            showDatePicker { date ->
                mandateDate = date
                binding.editTextMandateDate.setText(dateFormat.format(date))
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            calendar.set(year, month, day)
            onDateSelected(calendar.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun checkIntent() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        if (intent.hasExtra("EXTRA_ID")) {
            isEditMode = true
            collabId = intent.getIntExtra("EXTRA_ID", 0)
            binding.editTextBrand.setText(intent.getStringExtra("EXTRA_BRAND"))
            binding.autoCompleteAgency.setText(intent.getStringExtra("EXTRA_AGENCY"))
            binding.editTextNotes.setText(intent.getStringExtra("EXTRA_NOTES"))
            binding.editTextCashAmount.setText(intent.getDoubleExtra("EXTRA_CASH_AMOUNT", 0.0).toString())
            binding.editTextProductValue.setText(intent.getDoubleExtra("EXTRA_PRODUCT_VALUE", 0.0).toString())
            binding.editTextReimbAmount.setText(intent.getDoubleExtra("EXTRA_REIMB_AMOUNT", 0.0).toString())
            
            binding.checkHasInstaReel.isChecked = intent.getBooleanExtra("EXTRA_HAS_REEL", false)
            binding.checkHasInstaCarousel.isChecked = intent.getBooleanExtra("EXTRA_HAS_CAROUSEL", false)
            binding.checkHasInstaStories.isChecked = intent.getBooleanExtra("EXTRA_HAS_STORIES", false)
            binding.checkHasYouTubeShorts.isChecked = intent.getBooleanExtra("EXTRA_HAS_SHORTS", false)

            deadlineDate = Date(intent.getLongExtra("EXTRA_DEADLINE", Date().time))
            binding.editTextDeadline.setText(dateFormat.format(deadlineDate))

            val arrDateLong = intent.getLongExtra("EXTRA_ARRIVAL_DATE", 0L)
            if (arrDateLong != 0L) {
                arrivalDate = Date(arrDateLong)
                binding.editTextArrivalDate.setText(dateFormat.format(arrivalDate!!))
            }
            
            val manDateLong = intent.getLongExtra("EXTRA_MANDATE_DATE", 0L)
            if (manDateLong != 0L) {
                mandateDate = Date(manDateLong)
                binding.editTextMandateDate.setText(dateFormat.format(mandateDate!!))
            }
            
            val typeStr = intent.getStringExtra("EXTRA_TYPE") ?: "PAID"
            val type = CollabType.valueOf(typeStr)
            binding.toggleGroupType.check(if (type == CollabType.PAID) binding.buttonPaid.id else binding.buttonBarter.id)

            val modeStr = intent.getStringExtra("EXTRA_PAYMENT_MODE") ?: "UPFRONT"
            val mode = PaymentMode.valueOf(modeStr)
            binding.toggleGroupPayment.check(if (mode == PaymentMode.UPFRONT) binding.buttonUpfront.id else binding.buttonReimbursement.id)
            
            binding.buttonSave.text = "Update Campaign"
            binding.toolbar.title = "Edit Campaign"
        } else {
            binding.toggleGroupType.check(binding.buttonPaid.id)
            binding.toggleGroupPayment.check(binding.buttonUpfront.id)
        }
    }

    private fun saveCollab() {
        val brand = binding.editTextBrand.text.toString().trim()
        val agency = binding.autoCompleteAgency.text.toString().trim()
        val notes = binding.editTextNotes.text.toString().trim()
        val cashAmount = binding.editTextCashAmount.text.toString().toDoubleOrNull() ?: 0.0
        val productValue = binding.editTextProductValue.text.toString().toDoubleOrNull() ?: 0.0
        val reimbAmount = binding.editTextReimbAmount.text.toString().toDoubleOrNull() ?: 0.0
        
        val type = if (binding.toggleGroupType.checkedButtonId == binding.buttonPaid.id) CollabType.PAID else CollabType.BARTER
        val mode = if (binding.toggleGroupPayment.checkedButtonId == binding.buttonUpfront.id) PaymentMode.UPFRONT else PaymentMode.REIMBURSEMENT

        if (brand.isEmpty()) {
            Toast.makeText(this, "Enter brand name", Toast.LENGTH_SHORT).show()
            return
        }

        val platformList = mutableListOf<String>()
        if (binding.checkHasInstaReel.isChecked) platformList.add("Instagram Reel")
        if (binding.checkHasInstaCarousel.isChecked) platformList.add("Instagram Carousel")
        if (binding.checkHasInstaStories.isChecked) platformList.add("Instagram Stories")
        if (binding.checkHasYouTubeShorts.isChecked) platformList.add("YouTube Shorts")
        val platformStr = if (platformList.isEmpty()) "General" else platformList.joinToString(", ")

        val collab = CollabEntity(
            id = if (isEditMode) collabId else 0,
            brandName = brand,
            platform = platformStr,
            deadline = deadlineDate,
            arrivalDate = arrivalDate,
            mandateDate = mandateDate,
            collabType = type,
            paymentMode = mode,
            cashAmount = cashAmount,
            productValue = productValue,
            reimbursementAmount = reimbAmount,
            agency = agency,
            notes = notes,
            hasInstaReel = binding.checkHasInstaReel.isChecked,
            hasInstaCarousel = binding.checkHasInstaCarousel.isChecked,
            hasInstaStories = binding.checkHasInstaStories.isChecked,
            hasYouTubeShorts = binding.checkHasYouTubeShorts.isChecked
        )

        if (isEditMode) viewModel.update(collab) else viewModel.insert(collab)
        finish()
    }
}