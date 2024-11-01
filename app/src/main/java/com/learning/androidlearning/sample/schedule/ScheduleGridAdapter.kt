package com.learning.androidlearning.sample.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.learning.androidlearning.R

class ScheduleGridAdapter(
    private val items: List<ScheduleItem>
) : RecyclerView.Adapter<ScheduleGridAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.itemText)
        val editText: EditText = view.findViewById(R.id.itemEdit)
        val doneButton: Button = view.findViewById(R.id.doneButton)
        val editContainer: LinearLayout = view.findViewById(R.id.editContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule_grid, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val item = items[position]
        
        holder.textView.text = item.content
        holder.editText.setText(item.content)

        // Set marquee effect
        holder.textView.isSelected = true

        // Set view state
        if (item.isEditable) {
            setupEditableItem(holder, item)
        } else {
            setupNonEditableItem(holder, item)
        }
    }

    private fun setupEditableItem(holder: ScheduleViewHolder, item: ScheduleItem) {
        holder.textView.setOnLongClickListener {
            // Show edit mode
            holder.textView.visibility = View.GONE
            holder.editContainer.visibility = View.VISIBLE
            holder.editText.requestFocus()
            true
        }

        // Handle keyboard done action
        holder.editText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveAndExitEditMode(holder, item)
                true
            } else {
                false
            }
        }

        holder.doneButton.setOnClickListener {
            saveAndExitEditMode(holder, item)
        }
    }

    private fun saveAndExitEditMode(holder: ScheduleViewHolder, item: ScheduleItem) {
        // Save edited content
        item.content = holder.editText.text.toString()
        holder.textView.text = item.content
        
        // Hide edit mode
        holder.textView.visibility = View.VISIBLE
        holder.editContainer.visibility = View.GONE
    }

    private fun setupNonEditableItem(holder: ScheduleViewHolder, item: ScheduleItem) {
        holder.editContainer.visibility = View.GONE
        holder.textView.visibility = View.VISIBLE
        
        // Set header and time slot style
        if (item.isHeader || item.isTimeSlot) {
            holder.textView.setBackgroundResource(R.drawable.bg_schedule_header)
            // Headers and time slots don't need marquee effect
            holder.textView.ellipsize = null
            holder.textView.isSingleLine = false
        }
    }

    override fun getItemCount() = items.size
} 