package com.octane.core.network

object ApiConfig {
    // CoinGecko API
    const val COINGECKO_BASE_URL = "https://api.coingecko.com/api/v3/"

    // Jupiter Swap API
    const val JUPITER_BASE_URL = "https://quote-api.jup.ag/v6/"

    // ✅ FIXED: DeFiLlama FREE public API (no authentication needed)
    const val DEFILLAMA_BASE_URL = "https://api.llama.fi/"

    const val DRIFT_URL = "https://data.api.drift.trade/"

    // Solana RPC endpoints
    object Solana {
        const val MAINNET_PUBLIC = "https://api.mainnet-beta.solana.com/"
        const val DEVNET = "https://api.devnet.solana.com/"
        const val TESTNET = "https://api.testnet.solana.com/"

        fun alchemyUrl(apiKey: String) = "https://solana-mainnet.g.alchemy.com/v2/$apiKey/"
        fun heliusUrl(apiKey: String) = "https://rpc.helius.xyz/?api-key=$apiKey/"
    }
}