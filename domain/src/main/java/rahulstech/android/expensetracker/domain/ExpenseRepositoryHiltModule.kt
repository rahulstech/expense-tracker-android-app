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
abstract class ExpenseRepositoryHiltModule {

    @Binds
    @Singleton
    abstract fun accountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun groupRepository(impl: GroupRepositoryImpl): GroupRepository

    @Binds
    @Singleton
    abstract fun historyRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    @Singleton
    abstract fun localCache(impl: LocalCacheImpl): LocalCache

    @Binds
    @Singleton
    abstract fun backupRepository(impl: BackupRepositoryImpl): BackupRepository

    @Binds
    @Singleton
    abstract fun restoreRepository(impl: RestoreRepositoryImpl): RestoreRepository
}