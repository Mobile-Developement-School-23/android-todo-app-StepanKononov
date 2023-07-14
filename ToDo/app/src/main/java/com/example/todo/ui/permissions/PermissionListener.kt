package com.example.todo.ui.permissions

interface PermissionListener {
    fun shouldShowRationaleInfo()
    fun isPermissionGranted(isGranted: Boolean)
}