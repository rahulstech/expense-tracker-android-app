package dreammaker.android.expensetracker.ui.history.historyinput



//
//@OptIn(ExperimentalCoroutinesApi::class)
//@HiltViewModel
//class HistoryInputViewModel @Inject constructor(
//    private val savedState: SavedStateHandle,
//    private val historyRepo: HistoryRepository,
//    private val accountRepo: AccountRepository,
//    private val groupRepo: GroupRepository,
//) : ViewModel() {
//
//    companion object {
//        private val TAG = HistoryInputViewModel::class.simpleName
//    }
//
//    private val configuration: HistoryInputConfiguration = getConfiguration()
//
//    private val _uiState = MutableStateFlow(HistoryInputUiState(configuration.screen.initialType))
//
//    val uiState: StateFlow<HistoryInputUiState> = _uiState.asStateFlow()
//
//    private val _uiEffect = Channel<HistoryInputUiEffect>(Channel.BUFFERED)
//
//    val uiEffect: Flow<HistoryInputUiEffect> = _uiEffect.receiveAsFlow()
//
//    val accountState = flow {
//        accountRepo.getAllAccounts()
//            .collect { accounts ->
//
//                emit(AccountState(isLoading = true))
//
//                val recentAccounts = accounts.sortedByDescending { it.lastUsed }.take(3)
//
//                emit(AccountState(false, accounts, recentAccounts))
//            }
//    }
//        .onFirst { state ->
//            if (configuration.mode is Mode.Add && configuration.mode.groupId != null) {
//                val account = state.allAccounts.find { it.id == configuration.mode.accountId }
//                _uiState.update {
//                    it.copy(sourceAccount = account)
//                }
//            }
//        }
//        .stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(5000),
//            AccountState()
//        )
//
//    val groupState = flow {
//        groupRepo.getAllGroups()
//            .collect { groups ->
//
//                emit(GroupState(isLoading = true))
//
//                val recentGroups = groups.sortedByDescending { it.lastUsed }.take(3)
//
//                emit(GroupState(false, groups, recentGroups))
//            }
//    }
//        .onFirst { state ->
//            if (configuration.mode is Mode.Add && configuration.mode.groupId != null) {
//                val group = state.allGroups.find { it.id == configuration.mode.groupId }
//                _uiState.update {
//                    it.copy(group = group)
//                }
//            }
//        }
//        .stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(5000),
//            GroupState()
//        )
//
//
//    private fun getConfiguration(): HistoryInputConfiguration {
//        return when (val route = savedState.toRoute<AppDestination.HistoryInput>()) {
//            is AppDestination.AddCreditDebit -> {
//                HistoryInputConfiguration(
//                    HistoryInputConfiguration.Screen.CREDIT_DEBIT,
//                    Mode.Add(route.accountId, route.groupId),
//                )
//            }
//
//            is AppDestination.EditCreditDebit -> {
//                HistoryInputConfiguration(
//                    HistoryInputConfiguration.Screen.CREDIT_DEBIT,
//                    Mode.Edit(route.historyId)
//                )
//            }
//
//            is AppDestination.AddTransfer -> {
//                HistoryInputConfiguration(
//                    HistoryInputConfiguration.Screen.TRANSFER,
//                    Mode.Add(route.sourceAccountId)
//                )
//            }
//
//            is AppDestination.EditTransfer -> {
//                HistoryInputConfiguration(
//                    HistoryInputConfiguration.Screen.CREDIT_DEBIT,
//                    Mode.Edit(route.historyId)
//                )
//            }
//
//            is AppDestination.AddIncomeExpense -> {
//                HistoryInputConfiguration(
//                    HistoryInputConfiguration.Screen.INCOME_EXPENSE,
//                    Mode.Add(route.accountId)
//                )
//            }
//
//            is AppDestination.EditIncomeExpense -> {
//                HistoryInputConfiguration(
//                    HistoryInputConfiguration.Screen.INCOME_EXPENSE,
//                    Mode.Edit(route.historyId)
//                )
//            }
//        }
//    }
//
//    init {
//
//        if (configuration.mode is Mode.Edit) {
//            findHistory(configuration.mode.historyId)
//        }
//    }
//
//    fun handleUiEvent(event: HistoryInputUiEvent) {
//        when(event) {
//            is HistoryInputUiEvent.DateChange -> {
//                _uiState.update {
//                    it.copy(date = event.date)
//                }
//            }
//
//            is HistoryInputUiEvent.AmountChange -> {
//                _uiState.update {
//                    it.copy(
//                        amount = event.amount,
//                        amountError = null
//                    )
//                }
//            }
//
//            is HistoryInputUiEvent.HistoryTypeChange -> {
//                _uiState.update {
//                    it.copy(type = event.type)
//                }
//            }
//
//            is HistoryInputUiEvent.NoteChange -> {
//                _uiState.update {
//                    it.copy(note = event.note)
//                }
//            }
//
//            is HistoryInputUiEvent.SourceAccountChange -> {
//                _uiState.update {
//                    it.copy(
//                        sourceAccount = event.account,
//                        sourceAccountError = null
//                    )
//                }
//            }
//
//            is HistoryInputUiEvent.DestinationAccountChange -> {
//                _uiState.update {
//                    it.copy(
//                        destinationAccount = event.account,
//                        destinationAccountError = null
//                    )
//                }
//            }
//
//            is HistoryInputUiEvent.GroupChange -> {
//                _uiState.update {
//                    it.copy(group = event.group)
//                }
//            }
//
//            is HistoryInputUiEvent.Save -> {
//                saveHistory()
//            }
//
//            is HistoryInputUiEvent.AddMore -> {
//                _uiState.update {
//                    it.reset()
//                }
//            }
//
//            is HistoryInputUiEvent.DismissDialog -> {
//                _uiState.update {
//                    it.copy(dialog = HistoryInputDialog.None)
//                }
//            }
//        }
//    }
//
//    private fun findHistory(id: Long) {
//        viewModelScope.launch {
//            historyRepo.getHistoryById(id)
//                .onStart {
//                    _uiState.update {
//                        it.copy(isEditing = true, isLoading = true)
//                    }
//                }
//                .take(1)
//                .catch { cause ->
//                    Log.e(TAG, "history load error", cause)
//                    _uiEffect.send(HistoryInputUiEffect.ShowLoadErrorAndExit(cause))
//                }
//                .collectLatest {
//                    it?.let { history ->
//                        _uiState.update { state ->
//                            state.copy(
//                                isLoading = false,
//                                id = history.id,
//                                date = history.date,
//                                amount = history.amount.toPlainString(),
//                                type = history.type,
//                                sourceAccount = history.primaryAccount,
//                                destinationAccount = history.secondaryAccount,
//                                group = history.group,
//                                note = history.note ?: ""
//                            )
//                        }
//                    } ?: _uiEffect.send(HistoryInputUiEffect.ShowNotFoundAndExit)
//                }
//        }
//    }
//
//    private fun saveHistory() {
//        if (_uiState.value.isSaving) return
//
//        viewModelScope.launch(Dispatchers.IO) {
//            _uiState.update {
//                it.resetInputErrors()
//                    .copy(isSaving = true)
//            }
//
//            val state = _uiState.value
//            if (validateInput(state)) {
//                return@launch
//            }
//            try {
//                state.toHistory().let {
//                    if (state.isEditing) {
//                        historyRepo.editHistory(it)
//                    } else {
//                        historyRepo.createHistory(it)
//                    }
//                }
//                _uiState.update {
//                    it.copy(
//                        isSaving = false,
//                        dialog = if (!it.isEditing) HistoryInputDialog.AddMore else HistoryInputDialog.None
//                    )
//                }
//                _uiEffect.send(HistoryInputUiEffect.ShowSaveSuccessful(_uiState.value.isEditing))
//            }
//            catch (e: CancellationException) {
//                throw e
//            }
//            catch (cause: Throwable) {
//                Log.e(TAG, "history save error", cause)
//                _uiState.update {
//                    it.copy(
//                        isSaving = false,
//                        dialog = HistoryInputDialog.SaveError(cause)
//                    )
//                }
//            }
//        }
//    }
//
//    private fun validateInput(state: HistoryInputUiState): Boolean {
//        val amountParsed = state.amount.toBigDecimalOrNull()
//        val destAccount = state.destinationAccount
//        val type = state.type
//        val amountError = when {
//            state.amount.isBlank() -> R.string.error_empty_input
//            amountParsed == null -> R.string.error_non_numeric_input
//            amountParsed <= BigDecimal.ZERO -> R.string.error_negative_numeric_input
//            else -> null
//        }
//        val sourceAccountError = if (state.sourceAccount == null) R.string.error_empty_input else null
//        val destinationAccountError = if (type == HistoryType.TRANSFER) {
//            when {
//                destAccount == null -> R.string.error_empty_input
//                state.sourceAccount?.id == destAccount.id ->  R.string.error_same_account_money_transfer
//                else -> null
//            }
//        }
//        else null
//
//        if (amountError == null && sourceAccountError == null && destinationAccountError == null) {
//            return true
//        }
//
//        _uiState.update {
//            it.copy(
//                amountError = amountError,
//                sourceAccountError = sourceAccountError,
//                destinationAccountError = destinationAccountError
//            )
//        }
//        return false
//    }
//}
