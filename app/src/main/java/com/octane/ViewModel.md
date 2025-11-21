
class ActivityViewModel(
private val baseTransaction: BaseTransactionViewModel,
private val observeTransactionHistoryUseCase: ObserveTransactionHistoryUseCase
) : ViewModel() {

    // Delegate: Recent transactions
    val recentTransactions = baseTransaction.recentTransactions

    // Delegate: Pending count
    val pendingCount = baseTransaction.pendingCount

    // UI State: Filters and search
    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    // Filtered transactions
    val filteredTransactions: StateFlow<LoadingState<List<Transaction>>> = combine(
        recentTransactions,
        _uiState
    ) { txState, uiState ->
        when (txState) {
            is LoadingState.Success -> {
                val filtered = txState.data.filter { tx ->
                    matchesFilters(tx, uiState)
                }
                LoadingState.Success(filtered)
            }

            is LoadingState.Loading -> LoadingState.Loading
            is LoadingState.Error -> txState
            else -> LoadingState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LoadingState.Loading
    )

    /**
     * Show transaction details sheet.
     */
    fun showTransactionDetails(transaction: Transaction) {
        _uiState.update {
            it.copy(
                selectedTransaction = transaction,
                showDetailsSheet = true
            )
        }
    }

    fun hideTransactionDetails() {
        _uiState.update {
            it.copy(
                selectedTransaction = null,
                showDetailsSheet = false
            )
        }
    }

    /**
     * Update search query (debounced).
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Toggle transaction type filter.
     */
    fun toggleTypeFilter(type: TransactionType) {
        _uiState.update {
            val currentFilters = it.typeFilters.toMutableSet()
            if (currentFilters.contains(type)) {
                currentFilters.remove(type)
            } else {
                currentFilters.add(type)
            }
            it.copy(typeFilters = currentFilters)
        }
    }

    /**
     * Toggle status filter.
     */
    fun toggleStatusFilter(status: TransactionStatus) {
        _uiState.update {
            val currentFilters = it.statusFilters.toMutableSet()
            if (currentFilters.contains(status)) {
                currentFilters.remove(status)
            } else {
                currentFilters.add(status)
            }
            it.copy(statusFilters = currentFilters)
        }
    }

    /**
     * Set date range filter.
     */
    fun setDateRange(start: Long?, end: Long?) {
        _uiState.update {
            it.copy(
                dateRangeStart = start,
                dateRangeEnd = end
            )
        }
    }

    /**
     * Clear all filters.
     */
    fun clearFilters() {
        _uiState.update {
            ActivityUiState()
        }
    }

    /**
     * Check if transaction matches current filters.
     */
    private fun matchesFilters(tx: Transaction, state: ActivityUiState): Boolean {
        // Search query
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            val matchesHash = tx.txHash.lowercase().contains(query)
            val matchesAddress = tx.toAddress?.lowercase()?.contains(query) == true
            if (!matchesHash && !matchesAddress) return false
        }

        // Type filter
        if (state.typeFilters.isNotEmpty() && !state.typeFilters.contains(tx.type)) {
            return false
        }

        // Status filter
        if (state.statusFilters.isNotEmpty() && !state.statusFilters.contains(tx.status)) {
            return false
        }

        // Date range
        if (state.dateRangeStart != null && tx.timestamp < state.dateRangeStart) {
            return false
        }
        if (state.dateRangeEnd != null && tx.timestamp > state.dateRangeEnd) {
            return false
        }

        return true
    }

    /**
     * Export transactions as CSV.
     */
    fun exportTransactions() {
        viewModelScope.launch {
            val transactions = (filteredTransactions.value as? LoadingState.Success)?.data
            if (transactions != null) {
                val csv = generateCSV(transactions)
                // TODO: Save to file and share
            }
        }
    }

    /**
     * Generate CSV from transactions.
     */
    private fun generateCSV(transactions: List<Transaction>): String {
        val header = "Date,Type,Status,From,To,Amount,Token,Fee,Hash\n"
        val rows = transactions.joinToString("\n") { tx ->
            val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                .format(java.util.Date(tx.timestamp))

            "${date}," +
                    "${tx.type}," +
                    "${tx.status}," +
                    "${tx.fromAddress}," +
                    "${tx.toAddress ?: ""}," +
                    "${tx.amount}," +
                    "${tx.tokenSymbol}," +
                    "${tx.fee}," +
                    "${tx.txHash}"
        }
        return header + rows
    }

    /**
     * Format transaction for display (delegates to base).
     */
    fun formatTransactionType(type: TransactionType): String {
        return baseTransaction.formatTransactionType(type)
    }

    fun getTransactionIcon(type: TransactionType): String {
        return baseTransaction.getTransactionIcon(type)
    }

    fun getStatusColor(status: TransactionStatus): Color {
        return baseTransaction.getStatusColor(status)
    }
}

/**
* Activity UI state (filters, search, sheets).
  */
  data class ActivityUiState(
  val searchQuery: String = "",
  val typeFilters: Set<TransactionType> = emptySet(),
  val statusFilters: Set<TransactionStatus> = emptySet(),
  val dateRangeStart: Long? = null,
  val dateRangeEnd: Long? = null,
  val selectedTransaction: Transaction? = null,
  val showDetailsSheet: Boolean = false
  )

open class BasePortfolioViewModel(
private val observePortfolioUseCase: ObservePortfolioUseCase,
private val refreshAssetsUseCase: RefreshAssetsUseCase,
private val toggleAssetVisibilityUseCase: ToggleAssetVisibilityUseCase,
private val observeCurrencyUseCase: ObserveCurrencyPreferenceUseCase,
private val networkMonitor: NetworkMonitor
) : ViewModel() {

    // UI State: Portfolio data
    private val _portfolioState = MutableStateFlow<LoadingState<PortfolioState>>(LoadingState.Loading)
    val portfolioState: StateFlow<LoadingState<PortfolioState>> = _portfolioState.asStateFlow()

    // UI State: Refresh indicator
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // UI State: Network status (for offline banner)
    val networkStatus: StateFlow<NetworkStatus> = networkMonitor.isConnected
        .combine(networkMonitor.connectionType) { connected, type ->
            NetworkStatus(
                isConnected = connected,
                connectionType = type,
                isMetered = type == ConnectionType.CELLULAR
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkStatus(
                isConnected = true,
                connectionType = ConnectionType.WIFI,
                isMetered = false
            )
        )

    // UI State: Selected currency for formatting
    val selectedCurrency: StateFlow<String> = observeCurrencyUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "USD"
        )

    init {
        observePortfolio()
    }

    private fun observePortfolio() {
        viewModelScope.launch {
            observePortfolioUseCase()
                .distinctUntilChanged()
                .collect { state ->
                    _portfolioState.value = state
                }
        }
    }

    fun refresh() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true

            when (val result = refreshAssetsUseCase()) {
                is LoadingState.Success -> {
                    // Portfolio state updates automatically via observePortfolio()
                }
                is LoadingState.Error -> {
                    _portfolioState.value = LoadingState.Error(
                        result.throwable,
                        "Refresh failed. Showing cached data."
                    )
                }
                else -> {}
            }

            _isRefreshing.value = false
        }
    }

    fun toggleAssetVisibility(assetId: String, isHidden: Boolean) {
        viewModelScope.launch {
            toggleAssetVisibilityUseCase(assetId, isHidden)
                .onFailure { e ->
                    _portfolioState.value = LoadingState.Error(
                        e,
                        "Failed to update asset visibility"
                    )
                }
        }
    }

    fun formatCurrency(valueUsd: Double): String {
        val currency = selectedCurrency.value
        return when (currency) {
            "USD" -> "$${"%,.2f".format(valueUsd)}"
            "EUR" -> "€${"%,.2f".format(valueUsd * 0.92)}"
            "GBP" -> "£${"%,.2f".format(valueUsd * 0.79)}"
            else -> "$${"%,.2f".format(valueUsd)}"
        }
    }

    fun formatChange(changePercent: Double): Pair<Color, String> {
        val color = when {
            changePercent > 0 -> Color(0xFF4ECDC4)
            changePercent < 0 -> Color(0xFFFF6B6B)
            else -> Color.Gray
        }

        val sign = if (changePercent > 0) "+" else ""
        val formatted = "$sign${"%.2f".format(changePercent)}%"

        return color to formatted
    }
}

data class NetworkStatus(
val isConnected: Boolean,
val connectionType: ConnectionType,
val isMetered: Boolean
)


open class BaseTransactionViewModel(
private val observeTransactionHistoryUseCase: ObserveTransactionHistoryUseCase,
private val monitorPendingTransactionsUseCase: MonitorPendingTransactionsUseCase,
private val walletRepository: WalletRepository
) : ViewModel() {

    // UI State: Recent transactions (last 50 for home screen)
    val recentTransactions: StateFlow<LoadingState<List<Transaction>>> =
        observeTransactionHistoryUseCase(limit = 50)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = LoadingState.Loading
            )

    // UI State: Pending transactions count (for badge)
    val pendingCount: StateFlow<Int> = recentTransactions
        .map { state ->
            when (state) {
                is LoadingState.Success -> state.data.count {
                    it.status == TransactionStatus.PENDING
                }
                else -> 0
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        startPendingTransactionMonitor()
    }

    /**
     * Monitor pending transactions every 5 seconds.
     * Updates status when confirmed/failed.
     */
    private fun startPendingTransactionMonitor() {
        viewModelScope.launch {
            while (isActive) {
                monitorPendingTransactionsUseCase()
                delay(5000) // Poll every 5 seconds
            }
        }
    }

    /**
     * Get transaction by hash (for deep links, notifications).
     * @param txHash Solana transaction signature
     * @return Transaction or null if not found
     */
    suspend fun getTransactionByHash(txHash: String): Transaction? {
        return (recentTransactions.value as? LoadingState.Success)
            ?.data
            ?.find { it.txHash == txHash }
    }

    /**
     * Format transaction type for display.
     * @param type Transaction type enum
     * @return User-friendly string ("Sent", "Received", "Swapped")
     */
    fun formatTransactionType(type: TransactionType): String {
        return when (type) {
            TransactionType.SEND -> "Sent"
            TransactionType.RECEIVE -> "Received"
            TransactionType.SWAP -> "Swapped"
            TransactionType.STAKE -> "Staked"
            TransactionType.UNSTAKE -> "Unstaked"
            TransactionType.NFT_MINT -> "Minted"
            TransactionType.NFT_TRANSFER -> "Transferred NFT"
            else -> "Transaction"
        }
    }

    /**
     * Get icon for transaction type.
     * @param type Transaction type enum
     * @return Icon resource or emoji
     */
    fun getTransactionIcon(type: TransactionType): String {
        return when (type) {
            TransactionType.SEND -> "\u2197\uFE0F"      // ↗️
            TransactionType.RECEIVE -> "\u2198\uFE0F"   // ↘️
            TransactionType.SWAP -> "\uD83D\uDD04"      // 🔄
            TransactionType.STAKE -> "\uD83D\uDD12"     // 🔒
            TransactionType.UNSTAKE -> "\uD83D\uDD13"   // 🔓
            TransactionType.NFT_MINT -> "\uD83C\uDFA8"  // 🎨
            TransactionType.NFT_TRANSFER -> "\uD83D\uDDBC\uFE0F" // 🖼️
            else -> "\u26A1"                            // ⚡
        }
    }

    /**
     * Get status badge color.
     * @param status Transaction status
     * @return Color for status badge
     */
    fun getStatusColor(status: TransactionStatus): Color {
        return when (status) {
            TransactionStatus.CONFIRMED -> Color(0xFF4ECDC4) // Teal
            TransactionStatus.PENDING -> Color(0xFFF7DC6F) // Yellow
            TransactionStatus.FAILED -> Color(0xFFFF6B6B) // Red
        }
    }
}

open class BaseWalletViewModel (
private val observeWalletsUseCase: ObserveWalletsUseCase,
private val createWalletUseCase: CreateWalletUseCase,
private val importWalletUseCase: ImportWalletUseCase,
private val deleteWalletUseCase: DeleteWalletUseCase,
private val setActiveWalletUseCase: SetActiveWalletUseCase // You'll need to create this
) : ViewModel() {

    // UI State: All wallets
    val walletsState: StateFlow<LoadingState<List<Wallet>>> = observeWalletsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )
    
    // UI State: Active wallet
    val activeWallet: StateFlow<Wallet?> = walletsState
        .map { state ->
            (state as? LoadingState.Success)?.data?.find { it.isActive }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // One-time events (navigation, toasts)
    private val _events = MutableSharedFlow<WalletEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<WalletEvent> = _events.asSharedFlow()
    
    /**
     * Create new wallet with generated keypair.
     * Shows seed phrase screen after creation.
     */
    fun createWallet(
        name: String,
        iconEmoji: String? = null,
        colorHex: String? = null
    ) {
        viewModelScope.launch {
            createWalletUseCase(name, iconEmoji, colorHex)
                .onSuccess { wallet ->
                    _events.emit(WalletEvent.WalletCreated(wallet))
                }
                .onFailure { e ->
                    _events.emit(WalletEvent.Error("Failed to create wallet: ${e.message}"))
                }
        }
    }
    
    /**
     * Import existing wallet from seed phrase.
     * @param seedPhrase 12 or 24-word BIP39 phrase
     * @param name Wallet name
     */
    fun importWallet(
        seedPhrase: String,
        name: String,
        iconEmoji: String? = null,
        colorHex: String? = null
    ) {
        viewModelScope.launch {
            importWalletUseCase(seedPhrase, name, iconEmoji, colorHex)
                .onSuccess { wallet ->
                    _events.emit(WalletEvent.WalletImported(wallet))
                }
                .onFailure { e ->
                    _events.emit(WalletEvent.Error(e.message ?: "Import failed"))
                }
        }
    }
    
    /**
     * Delete wallet permanently.
     * Shows confirmation dialog before deletion.
     * @param walletId Wallet database ID
     */
    fun deleteWallet(walletId: String) {
        viewModelScope.launch {
            deleteWalletUseCase(walletId)
                .onSuccess {
                    _events.emit(WalletEvent.WalletDeleted)
                }
                .onFailure { e ->
                    _events.emit(WalletEvent.Error(e.message ?: "Delete failed"))
                }
        }
    }
    
    /**
     * Switch active wallet.
     * Triggers portfolio refresh, updates all screens.
     * @param walletId Wallet to activate
     */
    fun switchWallet(walletId: String) {
        viewModelScope.launch {
            setActiveWalletUseCase(walletId)
                .onSuccess {
                    _events.emit(WalletEvent.WalletSwitched)
                }
                .onFailure { e ->
                    _events.emit(WalletEvent.Error("Failed to switch wallet"))
                }
        }
    }
    
    /**
     * Update wallet metadata (name, emoji, color).
     */
    fun updateWalletMetadata(
        walletId: String,
        name: String? = null,
        iconEmoji: String? = null,
        colorHex: String? = null
    ) {
        viewModelScope.launch {
            // You'll need UpdateWalletUseCase
            // updateWalletUseCase(walletId, name, iconEmoji, colorHex)
        }
    }
}

/**
* One-time wallet events for navigation/toasts.
  */
  sealed interface WalletEvent {
  data class WalletCreated(val wallet: Wallet) : WalletEvent
  data class WalletImported(val wallet: Wallet) : WalletEvent
  data object WalletDeleted : WalletEvent
  data object WalletSwitched : WalletEvent
  data class Error(val message: String) : WalletEvent
  }

class ReceiveViewModel(
private val baseWallet: BaseWalletViewModel,
private val basePortfolio: BasePortfolioViewModel,
private val qrCodeGenerator: QRCodeGenerator // You'll need to create this
) : ViewModel() {

    // UI State: Receive screen
    private val _receiveState = MutableStateFlow(ReceiveState())
    val receiveState: StateFlow<ReceiveState> = _receiveState.asStateFlow()
    
    // Delegate: Active wallet
    val activeWallet = baseWallet.activeWallet
    
    // Delegate: Portfolio (for token selection)
    val portfolioState = basePortfolio.portfolioState
    
    // One-time events
    private val _events = MutableSharedFlow<ReceiveEvent>(replay = 0)
    val events: SharedFlow<ReceiveEvent> = _events.asSharedFlow()
    
    init {
        observeActiveWallet()
    }
    
    /**
     * Observe active wallet and generate QR code.
     */
    private fun observeActiveWallet() {
        viewModelScope.launch {
            activeWallet.collect { wallet ->
                if (wallet != null) {
                    generateQRCode(wallet.publicKey)
                    _receiveState.update {
                        it.copy(
                            address = wallet.publicKey,
                            truncatedAddress = truncateAddress(wallet.publicKey)
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Select token to receive (changes instructions).
     */
    fun selectToken(tokenSymbol: String) {
        _receiveState.update { it.copy(selectedToken = tokenSymbol) }
        
        // For SPL tokens, show token-specific instructions
        if (tokenSymbol != "SOL") {
            _receiveState.update {
                it.copy(
                    instructionMessage = "Send $tokenSymbol to this Solana address. Your wallet will automatically create a token account if needed."
                )
            }
        } else {
            _receiveState.update {
                it.copy(
                    instructionMessage = "Send SOL to this address from any wallet or exchange."
                )
            }
        }
    }
    
    /**
     * Generate QR code bitmap from address.
     */
    private fun generateQRCode(address: String) {
        viewModelScope.launch {
            try {
                _receiveState.update { it.copy(isGeneratingQR = true) }
                
                // Generate QR code (you'll implement this)
                val qrBitmap = qrCodeGenerator.generate(
                    content = address,
                    size = 512, // 512x512 pixels
                    foregroundColor = android.graphics.Color.BLACK,
                    backgroundColor = android.graphics.Color.WHITE
                )
                
                _receiveState.update {
                    it.copy(
                        qrCodeBitmap = qrBitmap,
                        isGeneratingQR = false
                    )
                }
            } catch (e: Exception) {
                _receiveState.update { it.copy(isGeneratingQR = false) }
                _events.emit(ReceiveEvent.Error("Failed to generate QR code"))
            }
        }
    }
    
    /**
     * Copy address to clipboard.
     */
    fun copyAddress() {
        viewModelScope.launch {
            _events.emit(ReceiveEvent.AddressCopied(_receiveState.value.address))
        }
    }
    
    /**
     * Share address via system share sheet.
     */
    fun shareAddress() {
        viewModelScope.launch {
            _events.emit(ReceiveEvent.ShareAddress(_receiveState.value.address))
        }
    }
    
    /**
     * Share QR code image.
     */
    fun shareQRCode() {
        viewModelScope.launch {
            val bitmap = _receiveState.value.qrCodeBitmap
            if (bitmap != null) {
                _events.emit(ReceiveEvent.ShareQRCode(bitmap))
            } else {
                _events.emit(ReceiveEvent.Error("QR code not ready"))
            }
        }
    }
    
    /**
     * Truncate address for display.
     * Example: "ABC...XYZ" (4 chars + ... + 4 chars)
     */
    private fun truncateAddress(address: String): String {
        if (address.length <= 12) return address
        return "${address.take(4)}...${address.takeLast(4)}"
    }
}

/**
* Receive screen state.
  */
  data class ReceiveState(
  val address: String = "",
  val truncatedAddress: String = "",
  val qrCodeBitmap: android.graphics.Bitmap? = null,
  val selectedToken: String = "SOL",
  val instructionMessage: String = "Send SOL to this address from any wallet or exchange.",
  val isGeneratingQR: Boolean = false
  )

/**
* Receive events.
  */
  sealed interface ReceiveEvent {
  data class AddressCopied(val address: String) : ReceiveEvent
  data class ShareAddress(val address: String) : ReceiveEvent
  data class ShareQRCode(val bitmap: android.graphics.Bitmap) : ReceiveEvent
  data class Error(val message: String) : ReceiveEvent
  }

/**
* QR Code Generator interface (implement in infrastructure layer).
  */
  interface QRCodeGenerator {
  suspend fun generate(
  content: String,
  size: Int,
  foregroundColor: Int,
  backgroundColor: Int
  ): android.graphics.Bitmap
  }

class SendViewModel (
private val estimateFeeUseCase: EstimateTransactionFeeUseCase,
private val sendTokenUseCase: SendTokenUseCase,
private val authenticateWithBiometricsUseCase: AuthenticateWithBiometricsUseCase,
private val validateSolanaAddressUseCase: ValidateSolanaAddressUseCase,

    // Shared state
    private val basePortfolio: BasePortfolioViewModel,
    private val baseWallet: BaseWalletViewModel
) : ViewModel() {

    // UI State: Send form
    private val _sendState = MutableStateFlow(SendState())
    val sendState: StateFlow<SendState> = _sendState.asStateFlow()
    
    // Delegate: Active wallet
    val activeWallet = baseWallet.activeWallet
    
    // Delegate: Portfolio (for token selection)
    val portfolioState = basePortfolio.portfolioState
    
    // One-time events
    private val _events = MutableSharedFlow<SendEvent>(replay = 0)
    val events: SharedFlow<SendEvent> = _events.asSharedFlow()
    
    /**
     * Update amount input.
     * Validates against max balance, updates fee estimate.
     */
    fun onAmountChanged(amount: String) {
        val parsed = amount.toDoubleOrNull() ?: 0.0
        _sendState.update { it.copy(amount = amount, amountUsd = parsed * it.tokenPriceUsd) }
        estimateFee()
    }
    
    /**
     * Update recipient address.
     * Validates Solana address format, checks for SNS domains.
     */
    fun onRecipientChanged(recipient: String) {
        viewModelScope.launch {
            val isValid = validateSolanaAddressUseCase(recipient)
            _sendState.update { 
                it.copy(
                    recipient = recipient,
                    recipientValid = isValid
                ) 
            }
        }
    }
    
    /**
     * Select token to send.
     * Updates max balance, price, fee estimate.
     */
    fun selectToken(tokenSymbol: String) {
        val asset = (portfolioState.value as? LoadingState.Success)
            ?.data?.assets?.find { it.symbol == tokenSymbol }
        
        if (asset != null) {
            _sendState.update {
                it.copy(
                    selectedToken = tokenSymbol,
                    maxBalance = asset.balance.toDouble(),
                    tokenPriceUsd = asset.priceUsd ?: 0.0
                )
            }
            estimateFee()
        }
    }
    
    /**
     * Quick amount buttons (25%, 50%, 75%, Max).
     */
    fun onQuickAmountClick(percentage: Float) {
        val amount = _sendState.value.maxBalance * percentage
        onAmountChanged(amount.toString())
    }
    
    /**
     * Estimate transaction fee (dynamic based on network load).
     */
    private fun estimateFee() {
        viewModelScope.launch {
            val state = _sendState.value
            if (state.amount.toDoubleOrNull() == null) return@launch
            
            val feeResult = estimateFeeUseCase(
                tokenSymbol = state.selectedToken,
                amount = state.amount.toDouble()
            )
            
            feeResult.onSuccess { fee ->
                _sendState.update { it.copy(estimatedFee = fee) }
            }
        }
    }
    
    /**
     * Submit transaction with biometric confirmation.
     */
    fun onSendClick(activity: FragmentActivity) {
        viewModelScope.launch {
            val state = _sendState.value
            
            // Validate
            if (!state.isValid) {
                _events.emit(SendEvent.Error("Invalid send details"))
                return@launch
            }
            
            // Biometric auth
            _sendState.update { it.copy(isSubmitting = true) }
            
            val authResult = authenticateWithBiometricsUseCase(
                activity = activity,
                title = "Confirm Send",
                subtitle = "Send ${state.amount} ${state.selectedToken}"
            )
            
            if (authResult.isFailure) {
                _sendState.update { it.copy(isSubmitting = false) }
                _events.emit(SendEvent.Error("Authentication failed"))
                return@launch
            }
            
            // Send transaction
            val sendResult = sendTokenUseCase(
                recipient = state.recipient,
                tokenSymbol = state.selectedToken,
                amount = state.amount.toDouble()
            )
            
            sendResult.onSuccess { txHash ->
                _events.emit(SendEvent.Success(txHash))
                resetForm()
            }.onFailure { e ->
                _events.emit(SendEvent.Error(e.message ?: "Send failed"))
            }
            
            _sendState.update { it.copy(isSubmitting = false) }
        }
    }
    
    private fun resetForm() {
        _sendState.value = SendState()
    }
}

/**
* Send form state.
  */
  data class SendState(
  val selectedToken: String = "SOL",
  val amount: String = "",
  val amountUsd: Double = 0.0,
  val recipient: String = "",
  val recipientValid: Boolean = false,
  val maxBalance: Double = 0.0,
  val tokenPriceUsd: Double = 0.0,
  val estimatedFee: Double = 0.000005,
  val isSubmitting: Boolean = false
  ) {
  val isValid: Boolean
  get() = amount.toDoubleOrNull() != null
  && amount.toDouble() > 0
  && amount.toDouble() <= maxBalance
  && recipientValid
  }

/**
* Send events.
  */
  sealed interface SendEvent {
  data class Success(val txHash: String) : SendEvent
  data class Error(val message: String) : SendEvent
  }


class SettingsViewModel(
private val updateCurrencyUseCase: UpdateCurrencyPreferenceUseCase,
private val togglePrivacyModeUseCase: TogglePrivacyModeUseCase,
private val switchRpcEndpointUseCase: SwitchRpcEndpointUseCase,
private val observeNetworkStatusUseCase: ObserveNetworkStatusUseCase,
private val observeCurrencyUseCase: ObserveCurrencyPreferenceUseCase,
private val checkBiometricUseCase: CheckBiometricAvailabilityUseCase,
private val userPreferencesStore: UserPreferencesStore // For observing all preferences
) : ViewModel() {

    // UI State: Settings data
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    // Observe preferences
    val selectedCurrency = observeCurrencyUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "USD")
    
    val networkStatus = observeNetworkStatusUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            NetworkStatus(true, ConnectionType.WIFI, false)
        )
    
    val privacyMode = userPreferencesStore.privacyMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    init {
        checkBiometricAvailability()
    }
    
    /**
     * Check if biometrics are available.
     */
    private fun checkBiometricAvailability() {
        val availability = checkBiometricUseCase()
        _settingsState.update {
            it.copy(biometricAvailable = availability == BiometricAvailability.Available)
        }
    }
    
    // ==================== GENERAL SETTINGS ====================
    
    /**
     * Update currency preference.
     */
    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            updateCurrencyUseCase(currency)
        }
    }
    
    /**
     * Toggle privacy mode (hide balances).
     */
    fun togglePrivacyMode(enabled: Boolean) {
        viewModelScope.launch {
            togglePrivacyModeUseCase(enabled)
        }
    }
    
    // ==================== SECURITY SETTINGS ====================
    
    /**
     * Toggle biometric authentication.
     */
    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesStore.setBiometricEnabled(enabled)
            _settingsState.update { it.copy(biometricEnabled = enabled) }
        }
    }
    
    /**
     * Set auto-lock timeout (seconds).
     */
    fun setAutoLockTimeout(seconds: Int) {
        viewModelScope.launch {
            userPreferencesStore.setAutoLockTimeout(seconds)
            _settingsState.update { it.copy(autoLockTimeout = seconds) }
        }
    }
    
    /**
     * Show seed phrase (requires biometric auth).
     */
    fun requestShowSeedPhrase() {
        _settingsState.update { it.copy(showSeedPhraseDialog = true) }
    }
    
    fun hideShowSeedPhrase() {
        _settingsState.update { it.copy(showSeedPhraseDialog = false) }
    }
    
    // ==================== NETWORK SETTINGS ====================
    
    /**
     * Switch to custom RPC endpoint.
     */
    fun setCustomRpc(url: String) {
        switchRpcEndpointUseCase(url)
    }
    
    /**
     * Switch to next RPC endpoint (fallback).
     */
    fun switchToNextRpc() {
        switchRpcEndpointUseCase(null)
    }
    
    /**
     * Toggle testnet/mainnet.
     */
    fun toggleTestnet(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesStore.setTestnetEnabled(enabled)
            _settingsState.update { it.copy(testnetEnabled = enabled) }
        }
    }
    
    // ==================== APPEARANCE SETTINGS ====================
    
    /**
     * Set theme (light/dark/auto).
     */
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesStore.setTheme(theme.name)
            _settingsState.update { it.copy(theme = theme) }
        }
    }
    
    /**
     * Set language.
     */
    fun setLanguage(language: String) {
        viewModelScope.launch {
            userPreferencesStore.setLanguage(language)
            _settingsState.update { it.copy(language = language) }
        }
    }
    
    // ==================== ACCOUNT MANAGEMENT ====================
    
    /**
     * Request backup seed phrase.
     */
    fun requestBackup() {
        _settingsState.update { it.copy(showBackupDialog = true) }
    }
    
    fun hideBackup() {
        _settingsState.update { it.copy(showBackupDialog = false) }
    }
    
    /**
     * Export private key (requires biometric auth).
     */
    fun requestExportPrivateKey() {
        _settingsState.update { it.copy(showExportKeyDialog = true) }
    }
    
    fun hideExportPrivateKey() {
        _settingsState.update { it.copy(showExportKeyDialog = false) }
    }
    
    // ==================== ABOUT ====================
    
    /**
     * Get app version.
     */
    fun getAppVersion(): String {
        return "1.0.0" // TODO: Get from BuildConfig
    }
    
    /**
     * Open support/feedback.
     */
    fun openSupport() {
        // TODO: Open support URL
    }
    
    /**
     * Open terms of service.
     */
    fun openTerms() {
        // TODO: Open terms URL
    }
    
    /**
     * Open privacy policy.
     */
    fun openPrivacyPolicy() {
        // TODO: Open privacy URL
    }
}

/**
* Settings state.
  */
  data class SettingsState(
  // Security
  val biometricAvailable: Boolean = false,
  val biometricEnabled: Boolean = false,
  val autoLockTimeout: Int = 300, // 5 minutes

  // Network
  val testnetEnabled: Boolean = false,

  // Appearance
  val theme: AppTheme = AppTheme.AUTO,
  val language: String = "en",

  // Dialogs
  val showSeedPhraseDialog: Boolean = false,
  val showBackupDialog: Boolean = false,
  val showExportKeyDialog: Boolean = false
  )

/**
* App theme options.
  */
  enum class AppTheme {
  LIGHT, DARK, AUTO
  }


class SwapViewModel(
private val swapTokensUseCase: SwapTokensUseCase,
private val estimateFeeUseCase: EstimateTransactionFeeUseCase,
private val authenticateWithBiometricsUseCase: AuthenticateWithBiometricsUseCase,
private val jupiterApi: JupiterApiService,
private val basePortfolio: BasePortfolioViewModel,
private val baseWallet: BaseWalletViewModel
) : ViewModel() {

    // UI State: Swap form
    private val _swapState = MutableStateFlow(SwapState())
    val swapState: StateFlow<SwapState> = _swapState.asStateFlow()
    
    // Delegate: Portfolio (for token list)
    val portfolioState = basePortfolio.portfolioState
    
    // Delegate: Active wallet
    val activeWallet = baseWallet.activeWallet
    
    // One-time events
    private val _events = MutableSharedFlow<SwapEvent>(replay = 0)
    val events: SharedFlow<SwapEvent> = _events.asSharedFlow()
    
    // Real-time rate updates
    private var rateUpdateJob: Job? = null
    
    init {
        // Default: SOL → USDC
        selectFromToken("SOL")
        selectToToken("USDC")
    }
    
    /**
     * Update "You pay" amount.
     * Triggers rate refresh and output amount calculation.
     */
    fun onFromAmountChanged(amount: String) {
        val parsed = amount.toDoubleOrNull() ?: 0.0
        _swapState.update { it.copy(fromAmount = amount) }
        
        // Debounce rate updates (wait 300ms after typing stops)
        rateUpdateJob?.cancel()
        rateUpdateJob = viewModelScope.launch {
            delay(300)
            if (parsed > 0) {
                fetchQuote()
            } else {
                _swapState.update { it.copy(toAmount = "", rate = null, priceImpact = null) }
            }
        }
    }
    
    /**
     * Select "You pay" token.
     * Updates max balance and triggers rate refresh.
     */
    fun selectFromToken(tokenSymbol: String) {
        val asset = (portfolioState.value as? LoadingState.Success)
            ?.data?.assets?.find { it.symbol == tokenSymbol }
        
        if (asset != null) {
            _swapState.update {
                it.copy(
                    fromToken = tokenSymbol,
                    fromTokenBalance = asset.balance.toDouble(),
                    fromTokenPriceUsd = asset.priceUsd ?: 0.0
                )
            }
            
            // Re-fetch quote if amount exists
            if (_swapState.value.fromAmount.toDoubleOrNull() != null) {
                fetchQuote()
            }
        }
    }
    
    /**
     * Select "You receive" token.
     */
    fun selectToToken(tokenSymbol: String) {
        val asset = (portfolioState.value as? LoadingState.Success)
            ?.data?.assets?.find { it.symbol == tokenSymbol }
        
        if (asset != null) {
            _swapState.update {
                it.copy(
                    toToken = tokenSymbol,
                    toTokenPriceUsd = asset.priceUsd ?: 0.0
                )
            }
            
            // Re-fetch quote
            if (_swapState.value.fromAmount.toDoubleOrNull() != null) {
                fetchQuote()
            }
        }
    }
    
    /**
     * Reverse token pair (swap button).
     */
    fun reverseTokens() {
        val currentFrom = _swapState.value.fromToken
        val currentTo = _swapState.value.toToken
        
        selectFromToken(currentTo)
        selectToToken(currentFrom)
        
        // Swap amounts if output amount exists
        val outputAmount = _swapState.value.toAmount
        if (outputAmount.isNotBlank()) {
            onFromAmountChanged(outputAmount)
        }
    }
    
    /**
     * Quick amount buttons (25%, 50%, 75%, Max).
     */
    fun onQuickAmountClick(percentage: Float) {
        val amount = _swapState.value.fromTokenBalance * percentage
        onFromAmountChanged(amount.toString())
    }
    
    /**
     * Update slippage tolerance (basis points).
     * 100 bps = 1%
     */
    fun onSlippageChanged(bps: Int) {
        _swapState.update { it.copy(slippageBps = bps) }
        
        // Re-fetch quote with new slippage
        if (_swapState.value.fromAmount.toDoubleOrNull() != null) {
            fetchQuote()
        }
    }
    
    /**
     * Toggle MEV protection (Jito bundles).
     */
    fun toggleMevProtection(enabled: Boolean) {
        _swapState.update { it.copy(mevProtectionEnabled = enabled) }
    }
    
    /**
     * Fetch quote from Jupiter.
     * Updates rate, output amount, routes, price impact.
     */
    private fun fetchQuote() {
        viewModelScope.launch {
            try {
                val state = _swapState.value
                val amount = state.fromAmount.toDoubleOrNull() ?: return@launch
                
                _swapState.update { it.copy(isFetchingQuote = true) }
                
                // Get token mint addresses
                val inputMint = getTokenMintAddress(state.fromToken)
                val outputMint = getTokenMintAddress(state.toToken)
                
                // Convert to lamports
                val amountLamports = (amount * 1_000_000_000).toLong()
                
                // Get quote
                val quote = jupiterApi.getQuote(
                    inputMint = inputMint,
                    outputMint = outputMint,
                    amount = amountLamports,
                    slippageBps = state.slippageBps
                )
                
                // Calculate output amount
                val outputAmount = quote.outAmount.toLong() / 1_000_000_000.0
                
                // Calculate rate (1 FROM = X TO)
                val rate = outputAmount / amount
                
                // Extract route info
                val routes = quote.routePlan.map { plan ->
                    SwapRoute(
                        dex = plan.swapInfo.label,
                        percentage = plan.percent
                    )
                }
                
                _swapState.update {
                    it.copy(
                        toAmount = outputAmount.toString(),
                        rate = rate,
                        priceImpact = quote.priceImpactPct,
                        routes = routes,
                        jupiterQuote = quote,
                        isFetchingQuote = false
                    )
                }
                
                // Estimate fee
                estimateFee()
                
            } catch (e: Exception) {
                _swapState.update { it.copy(isFetchingQuote = false) }
                _events.emit(SwapEvent.Error("Failed to fetch quote: ${e.message}"))
            }
        }
    }
    
    /**
     * Estimate transaction fee.
     */
    private fun estimateFee() {
        viewModelScope.launch {
            val state = _swapState.value
            val feeResult = estimateFeeUseCase(
                tokenSymbol = state.fromToken,
                amount = state.fromAmount.toDouble()
            )
            
            feeResult.onSuccess { fee ->
                _swapState.update { it.copy(estimatedFee = fee) }
            }
        }
    }
    
    /**
     * Execute swap with biometric confirmation.
     */
    fun onSwapClick(activity: FragmentActivity) {
        viewModelScope.launch {
            val state = _swapState.value
            
            // Validate
            if (!state.isValid) {
                _events.emit(SwapEvent.Error("Invalid swap details"))
                return@launch
            }
            
            // Warn on high price impact (>5%)
            if ((state.priceImpact ?: 0.0) > 5.0) {
                _events.emit(SwapEvent.HighPriceImpactWarning(state.priceImpact!!))
                return@launch
            }
            
            // Biometric auth
            _swapState.update { it.copy(isSubmitting = true) }
            
            val authResult = authenticateWithBiometricsUseCase(
                activity = activity,
                title = "Confirm Swap",
                subtitle = "Swap ${state.fromAmount} ${state.fromToken} → ${state.toAmount} ${state.toToken}"
            )
            
            if (authResult.isFailure) {
                _swapState.update { it.copy(isSubmitting = false) }
                _events.emit(SwapEvent.Error("Authentication failed"))
                return@launch
            }
            
            // Execute swap
            val swapResult = swapTokensUseCase(
                fromToken = state.fromToken,
                toToken = state.toToken,
                amount = state.fromAmount.toDouble(),
                slippageBps = state.slippageBps
            )
            
            swapResult.onSuccess { txHash ->
                _events.emit(SwapEvent.Success(txHash))
                resetForm()
            }.onFailure { e ->
                _events.emit(SwapEvent.Error(e.message ?: "Swap failed"))
            }
            
            _swapState.update { it.copy(isSubmitting = false) }
        }
    }
    
    /**
     * Confirm swap after high price impact warning.
     */
    fun confirmHighImpactSwap(activity: FragmentActivity) {
        viewModelScope.launch {
            _swapState.update { it.copy(highImpactConfirmed = true) }
            onSwapClick(activity)
        }
    }
    
    private fun resetForm() {
        _swapState.value = SwapState(
            fromToken = "SOL",
            toToken = "USDC"
        )
        selectFromToken("SOL")
        selectToToken("USDC")
    }
    
    private fun getTokenMintAddress(symbol: String): String {
        return when (symbol) {
            "SOL" -> "So11111111111111111111111111111111111111112"
            "USDC" -> "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"
            "USDT" -> "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB"
            "BONK" -> "DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263"
            else -> throw IllegalArgumentException("Unsupported token: $symbol")
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        rateUpdateJob?.cancel()
    }
}

/**
* Swap form state.
  */
  data class SwapState(
  val fromToken: String = "SOL",
  val toToken: String = "USDC",
  val fromAmount: String = "",
  val toAmount: String = "",
  val fromTokenBalance: Double = 0.0,
  val fromTokenPriceUsd: Double = 0.0,
  val toTokenPriceUsd: Double = 0.0,
  val rate: Double? = null, // 1 FROM = X TO
  val priceImpact: Double? = null, // Percentage
  val routes: List<SwapRoute> = emptyList(),
  val slippageBps: Int = 100, // 1%
  val mevProtectionEnabled: Boolean = false,
  val estimatedFee: Double = 0.000005,
  val isFetchingQuote: Boolean = false,
  val isSubmitting: Boolean = false,
  val highImpactConfirmed: Boolean = false,
  val jupiterQuote: JupiterQuoteResponse? = null
  ) {
  val isValid: Boolean
  get() = fromAmount.toDoubleOrNull() != null
  && fromAmount.toDouble() > 0
  && fromAmount.toDouble() <= fromTokenBalance
  && toAmount.isNotBlank()
  && jupiterQuote != null

  val priceImpactColor: Color
  get() = when {
  priceImpact == null -> Color.Gray
  priceImpact < 1.0 -> Color(0xFF4ECDC4) // Teal (good)
  priceImpact < 5.0 -> Color(0xFFF7DC6F) // Yellow (warning)
  else -> Color(0xFFFF6B6B) // Red (danger)
  }
  }

data class SwapRoute(
val dex: String, // "Raydium", "Orca", etc.
val percentage: Int // Percentage of route
)

/**
* Swap events.
  */
  sealed interface SwapEvent {
  data class Success(val txHash: String) : SwapEvent
  data class Error(val message: String) : SwapEvent
  data class HighPriceImpactWarning(val impact: Double) : SwapEvent
  }



class WalletsViewModel(
private val baseWallet: BaseWalletViewModel
) : ViewModel() {

    // Delegate: Wallet list
    val walletsState = baseWallet.walletsState
    
    // Delegate: Active wallet
    val activeWallet = baseWallet.activeWallet
    
    // Delegate: Wallet events
    val walletEvents = baseWallet.events
    
    // UI State: Bottom sheets
    private val _uiState = MutableStateFlow(WalletsUiState())
    val uiState: StateFlow<WalletsUiState> = _uiState.asStateFlow()
    
    /**
     * Show create wallet sheet.
     */
    fun showCreateWallet() {
        _uiState.update { it.copy(showCreateSheet = true) }
    }
    
    fun hideCreateWallet() {
        _uiState.update { it.copy(showCreateSheet = false) }
    }
    
    /**
     * Show import wallet sheet.
     */
    fun showImportWallet() {
        _uiState.update { it.copy(showImportSheet = true) }
    }
    
    fun hideImportWallet() {
        _uiState.update { it.copy(showImportSheet = false) }
    }
    
    /**
     * Show delete confirmation dialog.
     */
    fun showDeleteConfirmation(walletId: String) {
        _uiState.update { 
            it.copy(
                showDeleteConfirmation = true,
                walletToDelete = walletId
            ) 
        }
    }
    
    fun hideDeleteConfirmation() {
        _uiState.update { 
            it.copy(
                showDeleteConfirmation = false,
                walletToDelete = null
            ) 
        }
    }
    
    /**
     * Show edit wallet sheet.
     */
    fun showEditWallet(walletId: String) {
        val wallet = (walletsState.value as? LoadingState.Success)
            ?.data?.find { it.id == walletId }
        
        if (wallet != null) {
            _uiState.update {
                it.copy(
                    showEditSheet = true,
                    editingWallet = wallet
                )
            }
        }
    }
    
    fun hideEditWallet() {
        _uiState.update {
            it.copy(
                showEditSheet = false,
                editingWallet = null
            )
        }
    }
    
    /**
     * Create new wallet (delegates to base).
     */
    fun createWallet(
        name: String,
        iconEmoji: String?,
        colorHex: String?
    ) {
        baseWallet.createWallet(name, iconEmoji, colorHex)
        hideCreateWallet()
    }
    
    /**
     * Import existing wallet (delegates to base).
     */
    fun importWallet(
        seedPhrase: String,
        name: String,
        iconEmoji: String?,
        colorHex: String?
    ) {
        baseWallet.importWallet(seedPhrase, name, iconEmoji, colorHex)
        hideImportWallet()
    }
    
    /**
     * Delete wallet with confirmation.
     */
    fun deleteWallet() {
        val walletId = _uiState.value.walletToDelete
        if (walletId != null) {
            baseWallet.deleteWallet(walletId)
            hideDeleteConfirmation()
        }
    }
    
    /**
     * Switch active wallet (delegates to base).
     */
    fun switchWallet(walletId: String) {
        baseWallet.switchWallet(walletId)
    }
    
    /**
     * Update wallet metadata.
     */
    fun updateWallet(
        walletId: String,
        name: String?,
        iconEmoji: String?,
        colorHex: String?
    ) {
        baseWallet.updateWalletMetadata(walletId, name, iconEmoji, colorHex)
        hideEditWallet()
    }
    
    /**
     * Get wallet display name with fallback.
     */
    fun getWalletDisplayName(wallet: Wallet): String {
        return wallet.name.ifBlank { "Wallet ${wallet.id.take(4)}" }
    }
    
    /**
     * Get wallet color as Compose Color.
     */
    fun getWalletColor(wallet: Wallet): Color {
        return try {
            Color(android.graphics.Color.parseColor(wallet.colorHex))
        } catch (e: Exception) {
            Color(0xFF4ECDC4) // Default teal
        }
    }
}

/**
* Wallets UI state (sheets, dialogs).
  */
  data class WalletsUiState(
  val showCreateSheet: Boolean = false,
  val showImportSheet: Boolean = false,
  val showEditSheet: Boolean = false,
  val showDeleteConfirmation: Boolean = false,
  val walletToDelete: String? = null,
  val editingWallet: Wallet? = null
  )