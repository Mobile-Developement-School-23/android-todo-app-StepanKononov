package com.example.todo.di.components

import com.example.todo.di.scope.ActivityScope
import com.example.todo.workers.SynchronizeWorker
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface WorkerComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): WorkerComponent
    }

    fun inject(work: SynchronizeWorker)
}