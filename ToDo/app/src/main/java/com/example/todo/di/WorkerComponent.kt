package com.example.todo.di

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