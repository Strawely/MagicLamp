package ru.solom.magiclamp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AddressModule {
    @Provides
    @Singleton
    @MutableAddress
    fun provideMutableAddressFlow(): MutableStateFlow<String?> {
        return MutableStateFlow(null)
    }

    @Provides
    @Singleton
    @Address
    fun provideAddressFlow(
        @MutableAddress addressFlow: MutableStateFlow<String?>
    ): StateFlow<String?> = addressFlow.asStateFlow()
}
