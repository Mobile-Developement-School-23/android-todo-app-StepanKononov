package com.example.todo.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.Constants
import com.example.todo.R
import com.example.todo.databinding.TaskItemBinding
import com.example.todo.model.TaskPriority
import com.example.todo.model.TodoItem
import java.text.SimpleDateFormat
import java.util.*

class ItemTaskListAdapter(
    private val onCompleteClicked: (TodoItem, Boolean) -> Unit,
    private val onItemClicked: (TodoItem) -> Unit
) : ListAdapter<TodoItem, ItemTaskListAdapter.ItemTaskViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemTaskViewHolder {
        val viewHolder = ItemTaskViewHolder(
            TaskItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onCompleteClicked
        )
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            onItemClicked(getItem(position))
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ItemTaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<TodoItem>() {
            override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    // Todo вынести onCompleteClicked (да и вообще bind перенести )
    class ItemTaskViewHolder(
        private var binding: TaskItemBinding,
        private val onCompleteClicked: (TodoItem, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(todoItem: TodoItem) {
            binding.isDoneCheckBox.setOnCheckedChangeListener { _, isChecked ->
                onCompleteClicked(todoItem, isChecked)
                updateCheckBoxView(todoItem, binding)
            }
            binding.taskText.text = todoItem.text
            binding.isDoneCheckBox.isChecked = todoItem.isComplete
            updatePriorityIcon(todoItem, binding)
            updateCheckBoxView(todoItem, binding)

            if (todoItem.deadline != null)
                binding.deadlineText.text = getDataString(todoItem.deadline!!)
        }

        // TODO тут скореее что-то через стили текста или тип того
        private fun updateCheckBoxView(
            item: TodoItem,
            binding: TaskItemBinding
        ) = if (item.isComplete) {
            binding.taskText.paintFlags = binding.taskText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            binding.taskText.paintFlags = binding.taskText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        private fun updatePriorityIcon(
            item: TodoItem,
            binding: TaskItemBinding
        ) {
            if (item.priority != TaskPriority.MEDIUM) {
                binding.priorityIcon.visibility = View.VISIBLE
                binding.priorityIcon.setImageResource(chosePriorityImage(item))
            } else binding.priorityIcon.visibility = View.GONE
        }

        private fun chosePriorityImage(item: TodoItem): Int =
            when (item.priority) {
                TaskPriority.LOW -> R.drawable.ic_arrow_downward
                else -> R.drawable.ic_priority_high
            }

        private fun getDataString(date: Date): String {
            val dateFormat = SimpleDateFormat(Constants.DATA_PATTERN, Locale.getDefault())
            return dateFormat.format(date)
        }
    }

}

