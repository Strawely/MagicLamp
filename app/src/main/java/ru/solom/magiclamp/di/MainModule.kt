package ru.solom.magiclamp.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.solom.magiclamp.ActivityProvider

@Module
@InstallIn(SingletonComponent::class)
object MainModule {
    @Provides
    fun provideActivityProvider(@ApplicationContext context: Context): ActivityProvider {
        return context as ActivityProvider
    }
}
