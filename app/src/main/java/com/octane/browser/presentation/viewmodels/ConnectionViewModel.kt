package com.octane.browser.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.browser.domain.models.DAppConnection
import com.octane.browser.domain.repository.ConnectionRepository
import com.octane.browser.domain.usecases.connection.DisconnectDAppUseCase
import com.octane.browser.domain.usecases.connection.GetConnectionUseCase
import com.octane.browser.domain.usecases.connection.RequestConnectionUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for managing DApp connections
 * SHARED with BrowserViewModel for connection requests
 */
class ConnectionViewModel(
    private val connectionRepository: ConnectionRepository,
    private val requestConnectionUseCase: RequestConnectionUseCase,
    private val disconnectDAppUseCase: DisconnectDAppUseCase,
    private val getConnectionUseCase: GetConnectionUseCase
) : ViewModel() {

    val allConnections: StateFlow<List<DAppConnection>> = connectionRepository.getAllConnections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _connectionRequest = MutableSharedFlow<ConnectionRequest>()
    val connectionRequest: SharedFlow<ConnectionRequest> = _connectionRequest.asSharedFlow()

    fun requestConnection(
        domain: String,
        chainId: String,
        address: String,
        permissions: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _connectionRequest.emit(
                ConnectionRequest(domain, chainId, address, permissions)
            )
        }
    }

    fun approveConnection(
        domain: String,
        chainId: String,
        address: String,
        permissions: List<String>
    ) {
        viewModelScope.launch {
            requestConnectionUseCase(domain, chainId, address, permissions)
        }
    }

    fun disconnectDApp(connectionId: String) {
        viewModelScope.launch {
            disconnectDAppUseCase(connectionId)
        }
    }

    fun getConnectionForDomain(domain: String): StateFlow<DAppConnection?> {
        return getConnectionUseCase(domain)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    fun disconnectAll() {
        viewModelScope.launch {
            connectionRepository.clearAllConnections()
        }
    }

    data class ConnectionRequest(
        val domain: String,
        val chainId: String,
        val address: String,
        val permissions: List<String>
    )
}
