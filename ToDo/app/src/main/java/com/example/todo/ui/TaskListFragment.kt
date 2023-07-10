package com.example.todo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.R
import com.example.todo.TodoApplication
import com.example.todo.adapter.ItemTaskListAdapter
import com.example.todo.data.model.TodoItem
import com.example.todo.data.viewModels.TaskListViewModel
import com.example.todo.data.viewModels.factory.TaskListViewModelFactory
import com.example.todo.databinding.FragmentTaskListBinding
import com.example.todo.di.components.FragmentComponent
import com.example.todo.di.scope.FragmentScope
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
    private val themeData = ThemeData()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injectDependencies()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        observeViewModelData()
        bind()
        startInternetConnectionWatcher()
    }

    private fun injectDependencies() {
        val application = (requireNotNull(this.activity).application as TodoApplication)
        fragmentComponent = application.appComponent.fragmentComponent().create()
        fragmentComponent.inject(this)
    }

    private fun setupRecyclerView() {
        val taskAdapter = createTaskAdapter()
        val recyclerView = binding.tasksRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = taskAdapter
        addDecoration(recyclerView)
    }

    private fun createTaskAdapter(): ItemTaskListAdapter {
        return ItemTaskListAdapter(
            { todoItem: TodoItem, complete: Boolean ->
                onChangeTaskDone(todoItem, complete)
            }) { navigateToEditTaskFragment(it.id) }
    }

    private fun addDecoration(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager(requireContext()).orientation
            )
        )
    }

    private fun observeViewModelData() {
        val taskAdapter = binding.tasksRecyclerView.adapter as ItemTaskListAdapter

        observeItems(taskAdapter)

        viewModel.apply {
            getCompleteItemsCount().observe(viewLifecycleOwner) { count ->
                setDoneTaskAmountText(count)
            }
            eventNetworkError.observe(viewLifecycleOwner) { isNetworkError ->
                if (isNetworkError)
                    onNetworkError()
            }
        }
    }

    private fun observeItems(taskAdapter: ItemTaskListAdapter) {
        lifecycle.coroutineScope.launch {
            viewModel.getAllItems().collect {
                items = it
                setTaskList(viewModel.isDoneTaskHide, taskAdapter)
            }
        }
    }

    private fun setTaskList(hideDoneTask: Boolean, taskAdapter: ItemTaskListAdapter) {
        val filteredItems = if (hideDoneTask) {
            items?.filter { !it.isComplete }
        } else {
            items
        }
        taskAdapter.submitList(filteredItems)
    }


    private fun bind() {
        binding.apply {
            addTaskButton.setOnClickListener {
                navigateToEditTaskFragment(UUID.randomUUID().toString())
            }
            hideDoneTaskButton.setOnCheckedChangeListener { _, isChecked ->
                onHideTask(isChecked)
            }
            themePickerButton.tag = viewModel.themeTag
            initialTheme(themePickerButton)
            themePickerButton.setOnClickListener {
                onThemeButtonListener(themePickerButton)
            }
        }


    }

    private fun changeTheme(icon: Int, tag: String) {
        binding.apply {
            themePickerButton.setImageResource(icon)
            binding.themePickerButton.tag = tag
        }
        when (tag) {
            ThemeEnum.DAY.toString() -> viewModel.setDayTheme()
            ThemeEnum.NIGHT.toString() -> viewModel.setNightTheme()
            else -> viewModel.setSystemTheme()
        }
    }

    private fun onThemeButtonListener(themePickerButton: ImageButton) {
        when (themePickerButton.tag as? String) {
            null, ThemeEnum.DAY.toString() -> {
                changeTheme(themeData.night.icon, themeData.night.tag.toString())
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                animateThemeChange(this)
            }

            ThemeEnum.NIGHT.toString() -> {
                changeTheme(themeData.system.icon, themeData.system.tag.toString())
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                animateThemeChange(this)
            }

            else -> {
                changeTheme(themeData.day.icon, themeData.day.tag.toString())
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                animateThemeChange(this)
            }
        }
    }

    private fun initialTheme(themePickerButton: ImageButton) =
        when (viewModel.themeTag) {
            ThemeEnum.DAY.toString() -> {
                themePickerButton.setImageResource(themeData.day.icon)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            ThemeEnum.NIGHT.toString() -> {
                themePickerButton.setImageResource(themeData.night.icon)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            else -> {
                themePickerButton.setImageResource(themeData.system.icon)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }


    private fun startInternetConnectionWatcher() {
        val internetConnectionWatcher = InternetConnectionWatcher(requireContext())
        internetConnectionWatcher.setOnInternetConnectedListener(viewModel::refreshDataFromRepository)
        internetConnectionWatcher.startWatching()
    }

    private fun onHideTask(isChecked: Boolean) {
        val taskAdapter = binding.tasksRecyclerView.adapter as ItemTaskListAdapter

        if (isChecked) {
            viewModel.hideDoneTasks()
        } else {
            viewModel.showAllTasks()
        }

        setTaskList(viewModel.isDoneTaskHide, taskAdapter)
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

    private fun setDoneTaskAmountText(doneTaskAmount: Int) {
        binding.amountDoneTasksText.text =
            getString(R.string.amount_of_done_task, doneTaskAmount.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private  val FADE_ANIMATION_DURATION = 300L

    private fun animateThemeChange(fragment: Fragment) {
        val rootView = fragment.requireView() as ViewGroup
        val fadeAnimation = AlphaAnimation(0f, 1f).apply {
            duration = FADE_ANIMATION_DURATION
        }
        rootView.startAnimation(fadeAnimation)
    }
}

data class ThemeData(
    val night: ThemeDescription = ThemeDescription(R.drawable.moon_stars, ThemeEnum.NIGHT),
    val day: ThemeDescription = ThemeDescription(R.drawable.sun, ThemeEnum.DAY),
    val system: ThemeDescription = ThemeDescription(R.drawable.eclipse_alt, ThemeEnum.SYSTEM),
)

data class ThemeDescription(
    val icon: Int,
    val tag: ThemeEnum,
)

enum class ThemeEnum {
    NIGHT, DAY, SYSTEM
}

