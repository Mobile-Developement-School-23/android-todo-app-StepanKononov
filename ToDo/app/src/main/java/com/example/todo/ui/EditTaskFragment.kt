package com.example.todo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.todo.Constants
import com.example.todo.R
import com.example.todo.TodoApplication
import com.example.todo.data.viewModels.EditTaskViewModel
import com.example.todo.data.viewModels.EditTaskViewModelFactory
import com.example.todo.databinding.FragmentEditTaskBinding
import com.example.todo.di.FragmentComponent
import com.example.todo.di.FragmentScope
import com.example.todo.model.TodoItem
import com.example.todo.model.toPriorityString
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@FragmentScope
class EditTaskFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: EditTaskViewModelFactory
    private val viewModel: EditTaskViewModel by viewModels {
        viewModelFactory
    }
    private lateinit var fragmentComponent: FragmentComponent

    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var _taskID: String
    private lateinit var _item: TodoItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            _taskID = bundle.getString(Constants.TASK_ID).toString()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injectDependencies()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.currentItem.observe(this.viewLifecycleOwner) { item -> _item = item }
        viewModel.retrieveItem(_taskID).observe(this.viewLifecycleOwner) { item ->
            if (item != null) {
                viewModel.setTask(item)
                viewModel.itemNotNew()
            } else
                viewModel.createNewTask(_taskID)
            bindTaskItem(_item)
            bindViewsToViewModel()
            bindTopAppBar()
        }
    }

    private fun bindViewsToViewModel() {
        binding.apply {
            taskEditText.doOnTextChanged { inputText, _, _, _ -> viewModel.setText(inputText.toString()) }
            prioritySpinner.onSpinnerSelected(viewModel::setPriority)

            deleteTaskButton.setOnClickListener {
                viewModel.removeItem(_item)
                navigateToTaskListFragment()
            }
            deadlineSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    showDatePickerDialog()
                } else {
                    clearDeadlineView()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindTopAppBar() {
        binding.topAppBar.apply {
            setNavigationIcon(R.drawable.ic_close)
            setNavigationOnClickListener { requireActivity().onNavigateUp() }
            setOnMenuItemClickListener { menuItem ->
                chooseMenuAction(menuItem.itemId)
            }
        }
    }

    private fun bindTaskItem(item: TodoItem) {
        binding.taskEditText.setText(item.text)
        binding.deleteTaskButton.isEnabled = !viewModel.isNewItem
        bindPrioritySpinner()
        bindDeadlineView(item.deadline)
    }

    private fun bindDeadlineView(deadline: Date?) {
        if (deadline != null) {
            binding.deadlineSwitch.isChecked = true
            setDateInTextView(_item.deadline!!)
        }
    }

    private fun bindPrioritySpinner() {
        val priorityTypeList = resources.getStringArray(R.array.task_priority_type)
        binding.prioritySpinner.setSelection(priorityTypeList.indexOf(_item.priority.toPriorityString(resources)))
    }

    private fun chooseMenuAction(itemId: Int) =
        when (itemId) {
            R.id.save_button -> {
                saveTask()
                navigateToTaskListFragment()
                true
            }

            else -> false
        }

    private fun saveTask() {
        val stringInTextField = binding.taskEditText.text.toString()
        if (stringInTextField.isNotBlank()) {
            viewModel.saveOrUpdateTask(_item)
        }
    }

    private fun showDatePickerDialog() {
        val builder = MaterialDatePicker.Builder.datePicker()
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { timestamp ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            updateDeadline(calendar.time)
        }
        picker.show(parentFragmentManager, picker.toString())
    }

    private fun updateDeadline(date: Date) {
        viewModel.setDeadline(date)
        setDateInTextView(date)
    }

    private fun navigateToTaskListFragment() {
        val action = EditTaskFragmentDirections.actionEditTaskFragmentToTaskListFragment()
        findNavController().navigate(action)
    }

    private fun clearDeadlineView() {
        viewModel.setDeadline(null)
        binding.deadlineText.text = null
    }

    private fun setDateInTextView(date: Date) {
        val dateFormat = SimpleDateFormat(Constants.DATA_PATTERN, Locale.getDefault())
        binding.deadlineText.text = dateFormat.format(date)
    }

    private fun injectDependencies() {
        val application = (requireNotNull(this.activity).application as TodoApplication)
        fragmentComponent = application.appComponent.fragmentComponent().create()
        fragmentComponent.inject(this)
    }
}
