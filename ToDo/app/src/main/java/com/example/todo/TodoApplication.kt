package com.example.todo

import android.app.Application
import com.example.todo.di.AppComponent
import com.example.todo.di.DaggerAppComponent

class TodoApplication : Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext, this)
    }
}