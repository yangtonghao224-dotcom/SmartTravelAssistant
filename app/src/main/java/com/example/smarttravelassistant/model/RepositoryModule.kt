package com.example.smarttravelassistant.model

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTravelRepository(): TravelRepository = TravelRepository()

    @Provides
    @Singleton
    fun provideExpenseRepository(): ExpenseRepository = ExpenseRepository()
}
