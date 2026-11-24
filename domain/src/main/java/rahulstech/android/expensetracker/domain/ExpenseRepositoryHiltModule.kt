package rahulstech.android.expensetracker.domain

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rahulstech.android.expensetracker.domain.impl.AccountRepositoryImpl
import rahulstech.android.expensetracker.domain.impl.BackupRepositoryImpl
import rahulstech.android.expensetracker.domain.impl.GroupRepositoryImpl
import rahulstech.android.expensetracker.domain.impl.HistoryRepositoryImpl
import rahulstech.android.expensetracker.domain.impl.LocalCacheImpl
import rahulstech.android.expensetracker.domain.impl.RestoreRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ExpenseRepositoryHiltModule {

    @Binds
    @Singleton
    fun expenseRepository(impl: ExpenseRepository): IExpenseRepository


    @Binds
    @Singleton
    fun accountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    fun groupRepository(impl: GroupRepositoryImpl): GroupRepository

    @Binds
    @Singleton
    fun historyRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    @Singleton
    fun localCache(impl: LocalCacheImpl): LocalCache

    @Binds
    @Singleton
    fun backupRepository(impl: BackupRepositoryImpl): BackupRepository

    @Binds
    @Singleton
    fun restoreRepository(impl: RestoreRepositoryImpl): RestoreRepository
}