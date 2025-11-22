// presentation/viewmodels/TokenDetailViewModel.kt

package com.octane.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.core.util.LoadingState
import com.octane.domain.models.Asset
import com.octane.domain.repository.AssetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Token Detail Screen ViewModel.
 * Shows detailed info for a single asset (price chart, stats, actions).
 */
class TokenDetailViewModel(
    private val assetRepository: AssetRepository
) : ViewModel() {
    
    // ==================== UI State ====================
    
    private val _assetDetail = MutableStateFlow<LoadingState<Asset>>(LoadingState.Loading)
    val assetDetail: StateFlow<LoadingState<Asset>> = _assetDetail.asStateFlow()
    
    private val _selectedTimeframe = MutableStateFlow("1D")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()
    
    // ==================== Initialization ====================
    
    fun loadAsset(assetId: String, symbol: String) {
        viewModelScope.launch {
            try {
                val asset = assetRepository.observeAsset(assetId, symbol).first()
                _assetDetail.value = if (asset != null) {
                    LoadingState.Success(asset)
                } else {
                    LoadingState.Error(IllegalArgumentException("Asset not found"))
                }
            } catch (e: Exception) {
                _assetDetail.value = LoadingState.Error(e)
            }
        }
    }
    
    // ==================== Actions ====================
    
    fun onTimeframeSelected(timeframe: String) {
        _selectedTimeframe.value = timeframe
        // TODO: Fetch price history for selected timeframe
    }
    
    // ==================== Formatting ====================
    
    fun formatPrice(priceUsd: Double): String {
        return "$${"%.2f".format(priceUsd)}"
    }
    
    fun formatChange(changePercent: Double): Pair<androidx.compose.ui.graphics.Color, String> {
        val color = if (changePercent >= 0) {
            androidx.compose.ui.graphics.Color(0xFF4ECDC4)
        } else {
            androidx.compose.ui.graphics.Color(0xFFFF6B6B)
        }
        
        val sign = if (changePercent > 0) "+" else ""
        val formatted = "$sign${"%.2f".format(changePercent)}%"
        
        return color to formatted
    }
}