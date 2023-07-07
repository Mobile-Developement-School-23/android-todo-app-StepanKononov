package com.example.todo.di.components

import dagger.Module

@Module(subcomponents = [FragmentComponent::class, WorkerComponent::class])
class AppSubcomponents