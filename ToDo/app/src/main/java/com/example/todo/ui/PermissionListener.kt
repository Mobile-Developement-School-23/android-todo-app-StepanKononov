package com.example.todo.ui

interface PermissionListener {
    fun shouldShowRationaleInfo()
    fun isPermissionGranted(isGranted: Boolean)
}