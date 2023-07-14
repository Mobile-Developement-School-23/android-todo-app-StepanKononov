package com.example.todo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.todo.Constants
import com.example.todo.R
import com.example.todo.TodoApplication
import com.example.todo.data.extensions.convertToStringWithFormat
import com.example.todo.data.model.TaskPriority
import com.example.todo.data.model.toPriorityString
import com.example.todo.data.viewModels.EditTaskViewModel
import com.example.todo.data.viewModels.factory.EditTaskViewModelFactory
import com.example.todo.di.components.FragmentComponent
import com.example.todo.di.scope.FragmentScope
import com.example.todocomposable.ui.theme.TodoAppTheme
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
    private lateinit var _taskID: String

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
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TodoAppTheme {
                    EditTaskScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.retrieveItem(_taskID).observe(this.viewLifecycleOwner) { item ->
            if (item != null) {
                viewModel.setTask(item)
                viewModel.itemNotNew()
            }
        }
    }

    private fun navigateToTaskListFragment() {
        val action = EditTaskFragmentDirections.actionEditTaskFragmentToTaskListFragment()
        findNavController().navigate(action)
    }

    private fun saveTask(taskText: String) {
        if (taskText.isNotBlank()) {
            viewModel.saveOrUpdateTask()
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
    }



    private fun injectDependencies() {
        val application = (requireNotNull(this.activity).application as TodoApplication)
        fragmentComponent = application.appComponent.fragmentComponent().create()
        fragmentComponent.inject(this)
    }

    @Composable
    private fun TopBar() {
        TopAppBar(
            title = { Text(text = "") },
            navigationIcon = {
                IconButton(onClick = { findNavController().navigateUp() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            },
            actions = {
                Button(
                    onClick = {
                        saveTask(viewModel.itemText.value)
                        navigateToTaskListFragment()
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(text = stringResource(R.string.save_button_text))
                }
            }
        )
    }

    @Composable
    private fun TaskTextField() {
        val taskText: String by viewModel.itemText.collectAsState()

        OutlinedTextField(
            value = taskText,
            onValueChange = { text -> viewModel.setText(text) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp).padding(8.dp),
            label = { Text(text = stringResource(R.string.task_text_placeholder)) }
        )
    }

    @Composable
    private fun PriorityDropdown() {
        val itemPriority: TaskPriority by viewModel.itemPriority.collectAsState()
        var priorityExpanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { priorityExpanded = !priorityExpanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = itemPriority.toPriorityString(resources),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            DropdownMenu(
                expanded = priorityExpanded,
                onDismissRequest = { priorityExpanded = false }
            ) {
                val priorityArray = resources.getStringArray(R.array.task_priority_type)
                priorityArray.forEach { priority ->
                    DropdownMenuItem(
                        onClick = {
                            viewModel.setPriority(priorityArray.indexOf(priority))
                            priorityExpanded = false
                        }
                    ) {
                        Text(text = priority)
                    }
                }
            }
        }
    }

    @Composable
    private fun DeadlineSwitch() {
        val itemDeadline: Date? by viewModel.itemDeadline.collectAsState()
        var datePickerVisible by remember { mutableStateOf(false) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.deadline_text),
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = itemDeadline != null,
                onCheckedChange = {
                    if (itemDeadline != null)
                        viewModel.setDeadline(null)
                    else
                        showDatePickerDialog()
                    datePickerVisible = !datePickerVisible
                }
            )
        }
        if (itemDeadline != null) {
            Text(
                text = itemDeadline!!.convertToStringWithFormat(),
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }

    @Composable
    private fun DeleteButton() {
        val isNewItem: Boolean by viewModel.isNewItem.collectAsState()


        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                enabled = !isNewItem,
                onClick = {
                    viewModel.removeItem()
                    navigateToTaskListFragment()
                }
            ) {
                Text(text = stringResource(R.string.delete))
            }
        }
    }

    @Composable
    fun EditTaskScreen() {
        Scaffold(
            topBar = { TopBar() }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
            ) {
                TaskTextField()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.priority),
                    modifier = Modifier.padding(top = 16.dp)
                )

                PriorityDropdown()

                Spacer(modifier = Modifier.height(32.dp))

                Column {
                    DeadlineSwitch()
                }

                Spacer(modifier = Modifier.height(16.dp))

                DeleteButton()
            }
        }
    }
}
