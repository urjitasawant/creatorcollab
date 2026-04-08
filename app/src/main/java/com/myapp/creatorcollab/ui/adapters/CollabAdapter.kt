package com.myapp.creatorcollab.ui.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myapp.creatorcollab.databinding.ItemCollabBinding
import com.myapp.creatorcollab.data.local.CollabEntity
import com.myapp.creatorcollab.model.CollabType
import com.myapp.creatorcollab.model.PaymentMode
import java.text.SimpleDateFormat
import java.util.*

class CollabAdapter(private val onItemClick: (CollabEntity) -> Unit) :
    ListAdapter<CollabEntity, CollabAdapter.CollabViewHolder>(CollabDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollabViewHolder {
        val binding = ItemCollabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CollabViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CollabViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    inner class CollabViewHolder(private val binding: ItemCollabBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(collab: CollabEntity) {
            binding.apply {
                textViewBrand.text = collab.brandName
                
                textViewAmount.text = if (collab.collabType == CollabType.PAID) {
                    "₹${String.format("%.0f", collab.cashAmount)}"
                } else {
                    "MRP: ₹${String.format("%.0f", collab.productValue)}"
                }
                
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                textViewPlatform.text = "${collab.platform} | ${dateFormat.format(collab.deadline)}"
                
                val progress = collab.getProgressPercent()
                progressIndicator.progress = progress
                textViewProgressPercent.text = "$progress%"
                
                chipType.text = collab.collabType.name
                
                // Color Coding for Paid vs Barter
                if (collab.collabType == CollabType.PAID) {
                    chipType.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#F3E5F5"))
                    chipType.setTextColor(Color.parseColor("#6200EE"))
                    textViewAmount.setTextColor(Color.parseColor("#6200EE"))
                } else {
                    chipType.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#E0F2F1"))
                    chipType.setTextColor(Color.parseColor("#00796B"))
                    textViewAmount.setTextColor(Color.parseColor("#00796B"))
                }
                
                textViewReimbTag.visibility = if (collab.paymentMode == PaymentMode.REIMBURSEMENT) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    class CollabDiffCallback : DiffUtil.ItemCallback<CollabEntity>() {
        override fun areItemsTheSame(oldItem: CollabEntity, newItem: CollabEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CollabEntity, newItem: CollabEntity): Boolean {
            return oldItem == newItem
        }
    }
}