package com.octane.browser.domain.usecases.security

import java.net.URI

class CheckPhishingUseCase {
    operator fun invoke(url: String): SecurityCheckResult {
        val uri = try {
            URI(url)
        } catch (e: Exception) {
            return SecurityCheckResult.Invalid
        }
        
        val host = uri.host?.lowercase() ?: return SecurityCheckResult.Invalid
        
        // Basic phishing detection (expand this list)
        val suspiciousPatterns = listOf(
            "metamask-",
            "metam4sk",
            "uniswap-",
            "binance-",
            "coinbase-",
            "phantom-",
            "pancakeswap-",
            "-support.com",
            "-verify.com",
            "-secure.com"
        )
        
        val isSuspicious = suspiciousPatterns.any { pattern ->
            host.contains(pattern)
        }
        
        return if (isSuspicious) {
            SecurityCheckResult.Suspicious("Potential phishing site detected")
        } else {
            SecurityCheckResult.Safe
        }
    }
    
    sealed class SecurityCheckResult {
        object Safe : SecurityCheckResult()
        object Invalid : SecurityCheckResult()
        data class Suspicious(val reason: String) : SecurityCheckResult()
    }
}
