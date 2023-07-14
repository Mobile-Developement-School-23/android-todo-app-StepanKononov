package com.example.todo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.TodoApplication
import com.example.todo.data.model.TaskPriority
import com.example.todo.data.model.TodoItem
import com.example.todo.data.viewModels.TaskListViewModel
import com.example.todo.data.viewModels.factory.TaskListViewModelFactory
import com.example.todo.di.components.FragmentComponent
import com.example.todo.di.scope.FragmentScope
import com.example.todo.network.InternetConnectionWatcher
import com.example.todocomposable.ui.theme.TodoAppTheme
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@FragmentScope
class TaskListFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: TaskListViewModelFactory
    private lateinit var fragmentComponent: FragmentComponent
    private val viewModel: TaskListViewModel by activityViewModels {
        viewModelFactory
    }

    private var items: List<TodoItem>? = null

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
                    TaskListScreen(
                        onCompleteClicked = { item, com -> onChangeTaskDone(item, com) },
                        onItemClicked = { item -> navigateToEditTaskFragment(item.id) },
                        onToggleHideDoneTasks = { onHideTask(it) },
                        navigateToEditTask = { id -> navigateToEditTaskFragment(id) },
                        onNetworkError = {}
                    )
                }
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeViewModelData()

        startInternetConnectionWatcher()
    }

    private fun injectDependencies() {
        val application = (requireNotNull(this.activity).application as TodoApplication)
        fragmentComponent = application.appComponent.fragmentComponent().create()
        fragmentComponent.inject(this)
    }


    private fun observeViewModelData() {
        observeItems()
        viewModel.apply {
            eventNetworkError.observe(viewLifecycleOwner) { isNetworkError ->
                if (isNetworkError)
                    onNetworkError()
            }
        }
    }

    private fun observeItems() {
        lifecycle.coroutineScope.launch {
            viewModel.getAllItems().collect {
                items = it
            }
        }
    }


    private fun startInternetConnectionWatcher() {
        val internetConnectionWatcher = InternetConnectionWatcher(requireContext())
        internetConnectionWatcher.setOnInternetConnectedListener(viewModel::refreshDataFromRepository)
        internetConnectionWatcher.startWatching()
    }

    private fun onHideTask(isChecked: Boolean) {
        if (isChecked) {
            viewModel.hideDoneTasks()
        } else {
            viewModel.showAllTasks()
        }
    }

    private fun navigateToEditTaskFragment(id: String) {
        val action = TaskListFragmentDirections.actionTaskListFragmentToEditTaskFragment(taskId = id)
        findNavController().navigate(action)
    }

    private fun onChangeTaskDone(todoItem: TodoItem, complete: Boolean) {
        lifecycle.coroutineScope.launch {
            viewModel.updateTodoItem(
                todoItem.copy(isComplete = complete)
            )
        }
    }

    private fun onNetworkError() {
        if (!viewModel.isNetworkErrorShown.value!!) {
            val rootView = view

            rootView?.let {
                Snackbar.make(it, getString(R.string.network_error_message), Snackbar.LENGTH_LONG)
                    .show()
            }
            viewModel.onNetworkErrorShown()
        }
    }


    @Composable
    fun ItemTaskListAdapter(
        items: List<TodoItem>,
        onCompleteClicked: (TodoItem, Boolean) -> Unit,
        onItemClicked: (TodoItem) -> Unit,
    ) {
        Card {
            LazyColumn {
                items(items = items, key = { it.id }) { item ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        ItemTaskViewHolder(
                            todoItem = item,
                            onCompleteClicked = onCompleteClicked,
                            onItemClicked = onItemClicked,
                        )
                    }
                }
            }
        }

    }


    @Composable
    fun ItemTaskViewHolder(
        todoItem: TodoItem,
        onCompleteClicked: (TodoItem, Boolean) -> Unit,
        onItemClicked: (TodoItem) -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClicked(todoItem) }
                .padding(8.dp)
        ) {
            Checkbox(
                checked = todoItem.isComplete,
                onCheckedChange = { isChecked ->
                    onCompleteClicked(todoItem, isChecked)
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            if (todoItem.priority != TaskPriority.MEDIUM) {
                Icon(
                    painter = painterResource(id = chosePriorityImage(todoItem)),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                        .align(Alignment.CenterVertically)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = todoItem.text,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 8.dp)
                )

                if (todoItem.deadline != null) {
                    Text(
                        text = todoItem.deadline.toString(),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_left),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(8.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }

    private fun chosePriorityImage(item: TodoItem): Int {
        return when (item.priority) {
            TaskPriority.LOW -> R.drawable.ic_arrow_downward
            else -> R.drawable.ic_priority_high
        }
    }

    @Composable
    fun TaskListScreen(
        onCompleteClicked: (TodoItem, Boolean) -> Unit,
        onItemClicked: (TodoItem) -> Unit,
        onToggleHideDoneTasks: (Boolean) -> Unit,
        navigateToEditTask: (String) -> Unit,
        onNetworkError: () -> Unit,
    ) {
        val hideDoneTasks by viewModel.isDoneTaskHide.collectAsState()
        val doneTaskAmount by viewModel.getCompleteItemsCount().collectAsState()
        val list by viewModel.getAllItems().collectAsState()
        val showList = if (hideDoneTasks) list.filter { !it.isComplete } else list


        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Мои дела", color = Color.Black) },
                    elevation = AppBarDefaults.TopAppBarElevation,
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigateToEditTask(UUID.randomUUID().toString()) },
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_new_task)
                    )
                }
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Количество выполненных дел $doneTaskAmount",
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(
                            checked = hideDoneTasks,
                            onCheckedChange = onToggleHideDoneTasks,
                        )
                    }
                    ItemTaskListAdapter(
                        items = showList,
                        onCompleteClicked = onCompleteClicked,
                        onItemClicked = onItemClicked
                    )
                }
            }
        )
    }
}
