package com.example.todo.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.todo.Constants
import com.example.todo.R
import com.example.todo.TodoApplication
import com.example.todo.data.viewModels.EditTaskViewModel
import com.example.todo.data.viewModels.EditTaskViewModelFactory
import com.example.todo.databinding.FragmentEditTaskBinding
import com.example.todo.model.TaskPriority
import com.example.todo.model.TodoItem
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class EditTaskFragment : Fragment() {
    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!
    private val _calendar = Calendar.getInstance()
    private var _deadlineDate: Date? = null
    private var _isNewTask = true
    private lateinit var _taskID: String
    private lateinit var _item: TodoItem

    @Inject
    lateinit var viewModelFactory: EditTaskViewModelFactory

    private val viewModel: EditTaskViewModel by viewModels {
        viewModelFactory
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireNotNull(this.activity).application as TodoApplication).appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            _taskID = bundle.getString(Constants.TASK_ID).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun bind(item: TodoItem) {
        binding.apply {
            taskEditText.setText(item.text)
            val priorityTypeList = resources.getStringArray(R.array.task_priority_type)
            prioritySpinner.setSelection(priorityTypeList.indexOf(getPriorityString(_item.priority)))
            deleteTaskButton.setOnClickListener {
                if (!_isNewTask) {
                    viewModel.removeItem(_item)
                    val action = EditTaskFragmentDirections.actionEditTaskFragmentToTaskListFragment()
                    findNavController().navigate(action)
                }
            }
            if (_item.deadline != null) {
                _deadlineDate = _item.deadline
                binding.deadlineSwitch.isChecked = true
                setDateInTextView(_item.deadline!!)
            }
            deadlineSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    _deadlineDate = choseDate()
                } else {
                    _deadlineDate = null
                    binding.deadlineText.text = null
                }
                viewModel.setDeadline(_deadlineDate)
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.currentItem.observe(this.viewLifecycleOwner) { item -> _item = item }

        viewModel.retrieveItem(_taskID).observe(this.viewLifecycleOwner) { item ->
            if (item != null) {
                viewModel.setTask(item)
                _isNewTask = false
            } else
                viewModel.createNewTask(_taskID)

            binding.deleteTaskButton.isEnabled = !_isNewTask
            bind(_item)
        }


        binding.topAppBar.setNavigationIcon(R.drawable.ic_close)
        binding.topAppBar.setNavigationOnClickListener { requireActivity().onNavigateUp() }

        binding.taskEditText.doOnTextChanged { inputText, _, _, _ ->
            viewModel.setText(inputText.toString())
        }
        binding.prioritySpinner.onSpinnerSelected {
            viewModel.setPriority(it)
        }
        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.save_button -> {
                    val stringInTextField = binding.taskEditText.text.toString()
                    if (stringInTextField.isNotBlank()) {
                        saveOrUpdateTask()

                        val action = EditTaskFragmentDirections.actionEditTaskFragmentToTaskListFragment()
                        val options = NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .build()
                        findNavController().navigate(action, options)
                    }
                    true
                }

                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun saveOrUpdateTask() {
        if (_isNewTask)
            viewModel.addTodoItem(_item)
        else
            viewModel.updateTodoItem(_item)
    }

    private fun choseDate(): Date {
        var date = Date()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, curYear, monthOfYear, dayOfMonth ->
                val calendar = GregorianCalendar(curYear, monthOfYear, dayOfMonth)
                date = calendar.time
                setDateInTextView(date)
            },
            _calendar.get(Calendar.YEAR),
            _calendar.get(Calendar.MONTH),
            _calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()

        return date;
    }

    private fun setDateInTextView(date: Date) {
        val dateFormat = SimpleDateFormat(Constants.DATA_PATTERN, Locale.getDefault())
        binding.deadlineText.text = dateFormat.format(date)
    }

    private fun getPriorityString(priority: TaskPriority): String {
        val priorityTypeList = resources.getStringArray(R.array.task_priority_type)

        return when (priority) {
            TaskPriority.MEDIUM -> priorityTypeList[0]
            TaskPriority.LOW -> priorityTypeList[1]
            else -> priorityTypeList[2]
        }
    }
}
