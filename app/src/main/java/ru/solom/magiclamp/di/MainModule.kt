package ru.solom.magiclamp.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.sync.Mutex
import ru.solom.magiclamp.ActivityProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {
    @Provides
    fun provideActivityProvider(@ApplicationContext context: Context): ActivityProvider {
        return context as ActivityProvider
    }

    @Provides
    @Singleton
    @LampJob
    fun provideLampJob(): Job = SupervisorJob()

    @Provides
    @Singleton
    fun provideMutex() = Mutex()
}
