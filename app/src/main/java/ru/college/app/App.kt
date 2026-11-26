package ru.college.app

import android.app.Application
import ru.college.app.di.AppContainer

class App : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
