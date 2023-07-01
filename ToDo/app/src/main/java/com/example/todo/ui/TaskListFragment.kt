package com.example.todo.ui

import android.os.Bundle
import android.util.Log
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
import com.example.todo.data.viewModels.TodoViewModel
import com.example.todo.data.viewModels.TodoViewModelFactory
import com.example.todo.databinding.FragmentTaskListBinding
import com.example.todo.model.TodoItem
import com.example.todo.network.InternetConnectionWatcher
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.*


class TaskListFragment : Fragment() {


    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TodoViewModel by activityViewModels {
        val activity = requireNotNull(this.activity)
        TodoViewModelFactory(
            (activity.application as TodoApplication).database.todoAppDao(),
            activity.application
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        val view = binding.root


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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

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

                taskAdapter.submitList(it)
            }
        }
        viewModel.getCompleteItemsCount().observe(this.viewLifecycleOwner) { count ->
            setDoneTaskAmountText(count)
        }

        binding.hideDoneTaskButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lifecycle.coroutineScope.launch {
                    viewModel.getAllItems().collect { it ->
                        taskAdapter.submitList(it.filter { !it.isComplete })
                    }
                }
            } else {
                lifecycle.coroutineScope.launch {
                    viewModel.getAllItems().collect {
                        taskAdapter.submitList(it)
                    }
                }
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

    fun setDoneTaskAmountText(doneTaskAmount: Int) {
        Log.v("TASK", doneTaskAmount.toString())
        binding.amountDoneTasksText.text =
            getString(R.string.amount_of_done_task, doneTaskAmount.toString())
    }


}