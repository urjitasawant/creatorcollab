package com.myapp.creatorcollab.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.myapp.creatorcollab.data.local.AppDatabase
import com.myapp.creatorcollab.data.local.CollabEntity
import com.myapp.creatorcollab.data.repository.CollaborationRepository
import com.myapp.creatorcollab.databinding.ActivityMainBinding
import com.myapp.creatorcollab.model.CollabType
import com.myapp.creatorcollab.ui.adapters.CollabAdapter
import com.myapp.creatorcollab.ui.viewmodels.CollabViewModel
import com.myapp.creatorcollab.ui.viewmodels.CollabViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CollabViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = CollaborationRepository(database.collabDao())
        CollabViewModelFactory(repository)
    }
    private lateinit var adapter: CollabAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val showPendingReimb = intent.getBooleanExtra("SHOW_PENDING_REIMB", false)
        val showReceivedPayouts = intent.getBooleanExtra("SHOW_RECEIVED_PAYOUTS", false)
        val filterType = intent.getStringExtra("FILTER_TYPE")
        val filterStatus = intent.getStringExtra("FILTER_STATUS")
        val filterBrand = intent.getStringExtra("FILTER_BRAND")

        setupToolbar(showPendingReimb, showReceivedPayouts, filterType, filterStatus, filterBrand)
        setupRecyclerView(showPendingReimb, showReceivedPayouts, filterType, filterStatus, filterBrand)
        setupFilters(showPendingReimb, showReceivedPayouts, filterType, filterStatus, filterBrand)

        binding.fabAddCollab.setOnClickListener {
            startActivity(Intent(this, AddEditCollabActivity::class.java))
        }
    }

    private fun setupToolbar(pendingReimb: Boolean, receivedPayouts: Boolean, type: String?, status: String?, brand: String?) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        binding.toolbar.title = when {
            pendingReimb -> "Pending Reimbursements"
            receivedPayouts -> "Received Payouts"
            brand != null -> "Brand: $brand"
            type == "PAID" -> "Paid Campaigns"
            type == "BARTER" -> "Barter Campaigns"
            status == "PENDING" -> "In Progress"
            status == "COMPLETED" -> "Completed Campaigns"
            else -> "All Campaigns"
        }
    }

    private fun setupRecyclerView(pendingReimb: Boolean, receivedPayouts: Boolean, type: String?, status: String?, brand: String?) {
        adapter = CollabAdapter { collab ->
            val intent = Intent(this, CollabDetailActivity::class.java).apply {
                putExtra("EXTRA_ID", collab.id)
            }
            startActivity(intent)
        }
        
        binding.recyclerViewCollabs.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCollabs.adapter = adapter

        when {
            pendingReimb -> viewModel.pendingReimbursements.observe(this) { updateUI(it) }
            receivedPayouts -> viewModel.getReceivedPayouts().observe(this) { updateUI(it) }
            brand != null -> viewModel.getCollabsByBrand(brand).observe(this) { updateUI(it) }
            type == "PAID" -> viewModel.getFilteredCollabs(CollabType.PAID).observe(this) { updateUI(it) }
            type == "BARTER" -> viewModel.getFilteredCollabs(CollabType.BARTER).observe(this) { updateUI(it) }
            status == "PENDING" -> viewModel.getPendingCampaigns().observe(this) { updateUI(it) }
            status == "COMPLETED" -> viewModel.getCompletedCampaigns().observe(this) { updateUI(it) }
            else -> viewModel.allCollaborations.observe(this) { updateUI(it) }
        }
    }

    private fun updateUI(collabs: List<CollabEntity>) {
        if (collabs.isEmpty()) {
            binding.recyclerViewCollabs.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.recyclerViewCollabs.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            adapter.submitList(collabs)
        }
    }

    private fun setupFilters(pendingReimb: Boolean, receivedPayouts: Boolean, type: String?, status: String?, brand: String?) {
        if (pendingReimb || receivedPayouts || brand != null || type != null || status != null) {
            binding.chipGroupFilter.visibility = View.GONE
            return
        }
        
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                binding.chipAll.id -> viewModel.allCollaborations.observe(this) { updateUI(it) }
                binding.chipPaid.id -> viewModel.getFilteredCollabs(CollabType.PAID).observe(this) { updateUI(it) }
                binding.chipBarter.id -> viewModel.getFilteredCollabs(CollabType.BARTER).observe(this) { updateUI(it) }
            }
        }
    }
}