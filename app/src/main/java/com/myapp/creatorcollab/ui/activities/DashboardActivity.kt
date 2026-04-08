package com.myapp.creatorcollab.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.myapp.creatorcollab.R
import com.myapp.creatorcollab.data.local.AppDatabase
import com.myapp.creatorcollab.data.repository.CollaborationRepository
import com.myapp.creatorcollab.databinding.ActivityDashboardBinding
import com.myapp.creatorcollab.model.CollabType
import com.myapp.creatorcollab.ui.adapters.CollabAdapter
import com.myapp.creatorcollab.ui.viewmodels.CollabViewModel
import com.myapp.creatorcollab.ui.viewmodels.CollabViewModelFactory

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: CollabViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = CollaborationRepository(database.collabDao())
        CollabViewModelFactory(repository)
    }
    private lateinit var adapter: CollabAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigationDrawer()
        setupRecyclerView()
        observeDashboardData()
        setupChartClickListeners()

        binding.buttonViewAll.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Entire purple card is now clickable
        binding.cardTotalPayout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("SHOW_RECEIVED_PAYOUTS", true)
            }
            startActivity(intent)
        }

        binding.cardPendingReimb.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("SHOW_PENDING_REIMB", true)
            }
            startActivity(intent)
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditCollabActivity::class.java))
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupNavigationDrawer() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            val intent = Intent(this, MainActivity::class.java)
            when (menuItem.itemId) {
                R.id.nav_dashboard -> { /* Stay here */ }
                R.id.nav_all -> startActivity(intent)
                R.id.nav_paid -> {
                    intent.putExtra("FILTER_TYPE", "PAID")
                    startActivity(intent)
                }
                R.id.nav_barter -> {
                    intent.putExtra("FILTER_TYPE", "BARTER")
                    startActivity(intent)
                }
                R.id.nav_pending -> {
                    intent.putExtra("FILTER_STATUS", "PENDING")
                    startActivity(intent)
                }
                R.id.nav_completed -> {
                    intent.putExtra("FILTER_STATUS", "COMPLETED")
                    startActivity(intent)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupRecyclerView() {
        adapter = CollabAdapter { collab ->
            val intent = Intent(this, CollabDetailActivity::class.java).apply {
                putExtra("EXTRA_ID", collab.id)
            }
            startActivity(intent)
        }
        binding.recyclerViewRecent.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRecent.adapter = adapter
    }

    private fun observeDashboardData() {
        viewModel.dashboardStats.observe(this) { stats ->
            binding.textViewTotalCash.text = "₹${String.format("%.1f", stats.totalCashPayoutReceived / 1000)}K"
            binding.textViewPendingReimb.text = "₹${String.format("%.1f", stats.totalReimbursementPending / 1000)}K"
            
            updateCountChart(stats.paidCount, stats.barterCount)
            updateBarterBrandChart(stats.barterBrandValues.map { PieEntry(it.value.toFloat(), it.name) })
            updatePaidBrandChart(stats.paidBrandValues.map { PieEntry(it.value.toFloat(), it.name) })
        }

        viewModel.recentCollaborations.observe(this) { collabs ->
            adapter.submitList(collabs)
        }
    }

    private fun setupChartClickListeners() {
        // Chart 1: Mix Distribution
        binding.chartCountDistribution.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                if (e is PieEntry) {
                    val label = e.label
                    val intent = Intent(this@DashboardActivity, MainActivity::class.java).apply {
                        putExtra("FILTER_TYPE", label.uppercase())
                    }
                    startActivity(intent)
                }
            }
            override fun onNothingSelected() {}
        })

        // Chart 2: Barter Value (Brand Filter)
        binding.chartBarterValue.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                if (e is PieEntry) {
                    val brandName = e.label
                    val intent = Intent(this@DashboardActivity, MainActivity::class.java).apply {
                        putExtra("FILTER_BRAND", brandName)
                    }
                    startActivity(intent)
                }
            }
            override fun onNothingSelected() {}
        })

        // Chart 3: Paid Value (Brand Filter)
        binding.chartPaidValue.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                if (e is PieEntry) {
                    val brandName = e.label
                    val intent = Intent(this@DashboardActivity, MainActivity::class.java).apply {
                        putExtra("FILTER_BRAND", brandName)
                    }
                    startActivity(intent)
                }
            }
            override fun onNothingSelected() {}
        })
    }

    private fun updateCountChart(paid: Int, barter: Int) {
        val entries = listOf(PieEntry(paid.toFloat(), "Paid"), PieEntry(barter.toFloat(), "Barter"))
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(Color.parseColor("#6200EE"), Color.parseColor("#03DAC5"))
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }
        binding.chartCountDistribution.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            animateY(800)
            invalidate()
        }
    }

    private fun updateBarterBrandChart(entries: List<PieEntry>) {
        if (entries.isEmpty()) {
            binding.chartBarterValue.clear()
            return
        }
        val dataSet = PieDataSet(entries, "").apply {
            colors = getChartColors()
            valueTextSize = 10f
        }
        binding.chartBarterValue.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            animateY(800)
            invalidate()
        }
    }

    private fun updatePaidBrandChart(entries: List<PieEntry>) {
        if (entries.isEmpty()) {
            binding.chartPaidValue.clear()
            return
        }
        val dataSet = PieDataSet(entries, "").apply {
            colors = getChartColors()
            valueTextSize = 10f
        }
        binding.chartPaidValue.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            animateY(800)
            invalidate()
        }
    }

    private fun getChartColors(): List<Int> {
        return listOf(
            Color.parseColor("#FF6D00"), Color.parseColor("#2979FF"),
            Color.parseColor("#00E676"), Color.parseColor("#D500F9"),
            Color.parseColor("#FFEA00"), Color.parseColor("#F50057")
        )
    }
}