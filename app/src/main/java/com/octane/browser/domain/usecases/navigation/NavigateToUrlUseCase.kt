package com.octane.browser.domain.usecases.navigation

import com.octane.browser.domain.usecases.history.RecordVisitUseCase
import com.octane.browser.domain.usecases.tab.UpdateTabContentUseCase
import com.octane.browser.domain.usecases.validation.ValidateUrlUseCase

class NavigateToUrlUseCase(
    private val validateUrlUseCase: ValidateUrlUseCase,
    private val updateTabContentUseCase: UpdateTabContentUseCase,
    private val recordVisitUseCase: RecordVisitUseCase
) {
    suspend operator fun invoke(
        tabId: String,
        input: String,
        currentTitle: String = ""
    ): NavigationResult {
        val validation = validateUrlUseCase(input)
        
        val finalUrl = when (validation) {
            is ValidateUrlUseCase.UrlValidationResult.Empty -> 
                return NavigationResult.Error("Empty URL")
            
            is ValidateUrlUseCase.UrlValidationResult.ValidUrl -> 
                validation.url
            
            is ValidateUrlUseCase.UrlValidationResult.SearchQuery -> 
                validation.toSearchUrl()
        }
        
        // Update tab
        updateTabContentUseCase(tabId, finalUrl, currentTitle)
        
        // Record visit (will be updated again with proper title when page loads)
        recordVisitUseCase(finalUrl, currentTitle.ifEmpty { input })
        
        return NavigationResult.Success(finalUrl)
    }
    
    sealed class NavigationResult {
        data class Success(val url: String) : NavigationResult()
        data class Error(val message: String) : NavigationResult()
    }
}
