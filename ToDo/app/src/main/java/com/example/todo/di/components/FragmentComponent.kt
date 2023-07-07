package com.example.todo.di.components

import com.example.todo.di.scope.FragmentScope
import com.example.todo.ui.EditTaskFragment
import com.example.todo.ui.TaskListFragment
import dagger.Subcomponent

@FragmentScope
@Subcomponent
interface FragmentComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): FragmentComponent
    }

    fun inject(fragment: TaskListFragment)
    fun inject(fragment: EditTaskFragment)

}