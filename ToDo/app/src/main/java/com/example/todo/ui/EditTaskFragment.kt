package com.example.todo.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.todo.Constants
import com.example.todo.R
import com.example.todo.TodoApplication
import com.example.todo.data.viewModels.TodoViewModel
import com.example.todo.data.viewModels.TodoViewModelFactory
import com.example.todo.databinding.FragmentEditTaskBinding
import com.example.todo.model.TaskPriority
import com.example.todo.model.TodoItem
import java.text.SimpleDateFormat
import java.util.*


class EditTaskFragment : Fragment() {
    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!

    private val _calendar = Calendar.getInstance()
    private var _deadlineDate: Date? = null
    private var _isNewTask = true

    private lateinit var _taskID: String
    private lateinit var _item: TodoItem

    private val viewModel: TodoViewModel by activityViewModels {
        val activity = requireNotNull(this.activity)
        TodoViewModelFactory(
            (activity.application as TodoApplication).database.todoAppDao(),
            activity.application
        )
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.retrieveItem(_taskID).observe(this.viewLifecycleOwner) {selectedItem ->
            if (selectedItem != null){
                _item = selectedItem
                _isNewTask = false
            }
            binding.deleteTaskButton.isEnabled = !_isNewTask
            if (!_isNewTask)
                updateViewForCurrentTask()
            updateDeadlineView()
        }

        binding.deleteTaskButton.setOnClickListener {
            if (!_isNewTask) {
                viewModel.removeItem(_item)
                val action = EditTaskFragmentDirections.actionEditTaskFragmentToTaskListFragment()
                findNavController().navigate(action)
            }
        }
        binding.topAppBar.setNavigationIcon(R.drawable.ic_close)
        binding.topAppBar.setNavigationOnClickListener { requireActivity().onNavigateUp() }


        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.save_button -> {
                    val stringInTextField = binding.taskEditText.text.toString()
                    if (stringInTextField.isNotEmpty()) {
                        setupTask(stringInTextField)
                        val action = EditTaskFragmentDirections.actionEditTaskFragmentToTaskListFragment()
                        findNavController().navigate(action)
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


    private fun createTaskItem(stringInTextField: String) = TodoItem(
        id = _taskID,
        text = stringInTextField,
        creationDate = _calendar.time
    )

    private fun updateDeadlineView() {
        binding.deadlineSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                _deadlineDate = choseDate()
            } else {
                _deadlineDate = null
                binding.deadlineText.text = null
            }
        }
    }

    private fun setupTask(stringInTextField: String) {
        if (_isNewTask)
            _item = createTaskItem(stringInTextField)

        _item.text = stringInTextField
        _item.priority = getTaskPriority(binding.prioritySpinner.selectedItem.toString())
        _item.deadline = _deadlineDate



        if (_isNewTask)
            viewModel.addTodoItem(_item)
        else
            viewModel.updateTodoItem(_item)
    }

    private fun updateViewForCurrentTask() {

        binding.taskEditText.setText(_item.text)

        updatePriority()

        if (_item.deadline != null) {
            binding.deadlineSwitch.isChecked = true
            setDateInTextView(_item.deadline!!)
        }
    }

    private fun updatePriority() {
        val priorityTypeList = resources.getStringArray(R.array.task_priority_type)
        binding.prioritySpinner.setSelection(priorityTypeList.indexOf(getPriorityString(_item.priority)))
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


    private fun getTaskPriority(selectedPriority: String): TaskPriority {
        val priorityTypeList = resources.getStringArray(R.array.task_priority_type)

        return when (selectedPriority) {
            priorityTypeList[0] -> TaskPriority.MEDIUM
            priorityTypeList[1] -> TaskPriority.LOW
            else -> TaskPriority.HIGH
        }
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