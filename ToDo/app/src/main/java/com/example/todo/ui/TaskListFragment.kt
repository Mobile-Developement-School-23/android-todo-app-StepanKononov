package com.example.todo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.TodoApplication
import com.example.todo.adapter.ItemTaskListAdapter
import com.example.todo.data.viewModels.TaskListViewModel
import com.example.todo.data.viewModels.TaskListViewModelFactory
import com.example.todo.databinding.FragmentTaskListBinding
import com.example.todo.di.FragmentComponent
import com.example.todo.di.FragmentScope
import com.example.todo.model.TodoItem
import com.example.todo.network.InternetConnectionWatcher
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

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private var items: List<TodoItem>? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        val application = (requireNotNull(this.activity).application as TodoApplication)
        fragmentComponent = application.appComponent.fragmentComponent().create()
        fragmentComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.addTaskButton.setOnClickListener {
            val action = TaskListFragmentDirections.actionTaskListFragmentToEditTaskFragment(
                taskId = UUID.randomUUID().toString()
            )
            findNavController().navigate(action)
        }

        viewModel.eventNetworkError.observe(viewLifecycleOwner) { isNetworkError ->
            if (isNetworkError)
                onNetworkError()
        }

        val recyclerView = binding.tasksRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        val taskAdapter = ItemTaskListAdapter({ todoItem: TodoItem, complete: Boolean ->
            lifecycle.coroutineScope.launch {
                viewModel.updateTodoItem(
                    todoItem.copy(isComplete = complete)
                )
            }
        }) {
            val action = TaskListFragmentDirections.actionTaskListFragmentToEditTaskFragment(taskId = it.id)
            view.findNavController().navigate(action)
        }
        recyclerView.adapter = taskAdapter

        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager(requireContext()).orientation
            )
        )


        lifecycle.coroutineScope.launch {
            viewModel.getAllItems().collect {
                items = it
                if (viewModel.isDoneTaskHide.value!!)
                    taskAdapter.submitList(items!!.filter { !it.isComplete })
                else
                    taskAdapter.submitList(items)
            }
        }
        viewModel.isDoneTaskHide.observe(this.viewLifecycleOwner) { isDoneTaskHide ->
            if (items != null) {
                if (isDoneTaskHide)
                    taskAdapter.submitList(items!!.filter { !it.isComplete })
                else
                    taskAdapter.submitList(items)
            }

        }
        viewModel.getCompleteItemsCount().observe(this.viewLifecycleOwner) { count ->
            setDoneTaskAmountText(count)
        }

        binding.hideDoneTaskButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.hideDoneTasks()
            } else {
                viewModel.showAllTasks()
            }
        }
        val internetConnectionWatcher = InternetConnectionWatcher(requireContext())
        internetConnectionWatcher.setOnInternetConnectedListener(viewModel::refreshDataFromRepository)

        internetConnectionWatcher.startWatching()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setDoneTaskAmountText(doneTaskAmount: Int) {
        binding.amountDoneTasksText.text =
            getString(R.string.amount_of_done_task, doneTaskAmount.toString())
    }


}