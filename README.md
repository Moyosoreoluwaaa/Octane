# Octane Wallet

A high-performance, self-custody Solana wallet app for hardcore blockchain users. Inspired by Phantom Wallet, but faster, more secure, and packed with power-user features. Built for personal use and sharing with friends—no ads, no KYC, pure degen vibes.

## Project Overview

Octane Wallet is an Android mobile app built with Kotlin and Jetpack Compose, designed for crypto power users who execute dozens of transactions daily. It emphasizes speed (e.g., <15s sends/swaps), institutional-grade security (biometric re-prompts, token approval revoker), and engaging micro-interactions (confetti on success, morphing buttons). The app is Solana-native at its core, with stubs for future multi-chain support.

This is a sandbox project focused on self-custody and privacy. No custodial services, no social features—just raw utility for you and your crew. The architecture follows a modular, feature-flagged design to allow easy iteration without refactors.

Key tech stack:
- **Language**: Kotlin (1.9+)
- **UI Framework**: Jetpack Compose (for declarative, responsive UIs)
- **Blockchain Integration**: Solana SDK (via solana-mobile/mobile-wallet-adapter or similar)
- **Dependencies**: Coroutines for async, Hilt for DI, Retrofit for APIs (Jupiter swaps), Coil for image loading (NFTs), etc.
- **Target**: Android 8.0+ (API 26+)

## Features

Here's a detailed breakdown of the 20 merged features (from Doc 2 base + 8 Doc 1 exclusives), grouped by category for clarity:

### Core Wallet & Onboarding
1. **Create/Import Wallet + 12-Word Quiz**: Generate or import a Solana wallet with secure phrase backup and a verification quiz to prevent errors.
2. **Biometric Unlock**: Use Android BiometricPrompt for Face/Fingerprint unlock on app launch—and re-prompt *every time* the app resumes from background (your custom rule for max security).

### Portfolio & Assets
3. **Portfolio Dashboard**: Central home screen showing total value + 24h change, asset cards with SOL/USD toggles. Supports pull-to-refresh and offline caching.
4. **Privacy Mode**: Long-press on dashboard to hide balances (blurs or masks values for screenshots/privacy).
5. **Portfolio Home-Screen Widget**: Android widget displaying total value + 24h change for quick glances without opening the app.
6. **Swipe Gestures on Asset Cards**: Swipe left on cards to trigger Send; swipe right for Swap—fast actions for power users.

### Transactions & Flows
7. **Quick Send Bottom Sheet**: <15s flow: Paste/scan address, amount input, fee selector. Includes swipe integration and hold-to-confirm (1.5s circle progress).
8. **Quick Swap Bottom Sheet**: Jupiter aggregator integration for SOL↔tokens, with live previews, slippage settings, and swipe-right trigger.
9. **Transaction History + Detail Screens**: Chronological list with filters (date/type/asset); tap for details (status, hash, fees, explorer link).
10. **Transaction Simulation**: Full-screen preview before signing—shows potential outcomes, risks, and warnings.
11. **Hold-to-Confirm 1.5s Circle Progress**: Tactile feedback for destructive actions like sends/swaps—prevents accidents with a progress animation.
12. **Particle Confetti + SOL Rain + Fire Streaks**: Visual celebrations on successful txns (confetti burst, raining SOL coins, fire for streaks)—dopamine hits for degens.

### NFTs & Advanced
13. **Full NFT Gallery Tab**: Dedicated tab with grid view, floor price cards (via Magic Eden API), rarity tags, and send NFT flow.
14. **xNFT Launchpad**: Run Backpack-style executable NFTs directly inside the wallet—embedded dApp runtime for interactive experiences.
15. **MorphButton**: Dynamic button that shape-shifts between Send ↔ Swap based on context—smooth animations via Compose.

### Security & Network
16. **Token Approval & Revocation Manager + Security Health Score**: Dashboard to audit/revoke approvals; scores risk (e.g., "7 active—revoke now?").
17. **Network Status Indicator + RPC Manager Sheet**: Header indicator (green/yellow/red) for RPC health; bottom sheet to switch nodes (default/fast/private/custom).
18. **Hardware Wallet Bluetooth Flow**: Integrate Ledger/Trezor via Bluetooth for cold signing—secure flow with device scanning.
19. **Global Banner Stack**: Priority-based alerts (offline, low balance, RPC lag)—max 2 visible, auto-dismiss non-critical ones.

### Micro-Interactions & Polish
20. **Universal Polish**: Haptics on every tap/swipe, shimmer skeletons for loading, beautiful empty states (e.g., "Fund your wallet" with QR CTA), auto-save drafts on crash.

## Installation

### Prerequisites
- Android Studio (Koala or later recommended)
- Kotlin 1.9+
- JDK 17+
- Solana RPC key (e.g., from Helius or QuickNode)—store in `local.properties` as `RPC_KEY=your_key`
- Optional: Jupiter API key for swaps

### Steps
1. **Clone the Repo**:
   ```
   git clone https://github.com/Moyosoreoluwaaa/Octane
   cd octane
   ```

2. **Sync Gradle**:
   - Open in Android Studio.
   - Let it sync dependencies (build.gradle.kts includes Compose, Hilt, Coroutines, Solana SDK, etc.).
   - If needed, run `./gradlew build` from terminal.

3. **Configure APIs**:
   - Add to `local.properties`:
     ```
     SOLANA_RPC_URL=https://api.mainnet-beta.solana.com
     JUPITER_API_KEY=your_jupiter_key  # Optional for advanced swaps
     MAGIC_EDEN_API_KEY=your_me_key    # For NFT floors
     ```
   - For hardware wallets: Ensure Bluetooth permissions in AndroidManifest.xml.

4. **Build & Run**:
   - Connect an Android device/emulator (API 26+).
   - Click Run in Android Studio, or:
     ```
     ./gradlew installDebug
     ```
   - For release builds: `./gradlew assembleRelease` (sign with your keystore).

5. **Testing Setup**:
   - Use Android Emulator for offline simulation.
   - Enable developer options on device for USB debugging.
   - Run unit tests: `./gradlew test` (includes Compose previews).

Troubleshooting:
- Compose compiler issues? Update to latest in `build.gradle.kts`.
- RPC errors? Check network in emulator settings.
- No internet tools needed—app is offline-first.

## Usage

### Getting Started
- Launch the app: Biometric prompt appears immediately.
- New user: Follow onboarding—generate/import phrase, pass quiz, set biometric.
- Returning: Unlock → Dashboard loads with asset cards.

### Key Flows
- **View Portfolio**: Home screen shows total value widget-style. Swipe to refresh balances. Long-press for privacy blur.
- **Send SOL**: Swipe left on SOL card → Bottom sheet: Paste address (validates live), amount (MAX button), fee slider. Hold-to-confirm → Confetti if success.
- **Swap Tokens**: Swipe right → Jupiter sheet: Select tokens, preview simulation, slippage gear. Fire streaks on multi-swaps.
- **NFTs**: Navigate to gallery tab → Grid loads with floors (shimmer while fetching). Tap NFT → Details + Send button.
- **Security Check**: Go to settings → Approval manager: List approvals, swipe to revoke (simulation confirms).
- **RPC Switch**: Tap status indicator (header dot) → Sheet: Pick node, test latency.
- **xNFTs**: In gallery, tap executable NFT → Launches inline runtime (Backpack compat).
- **Widget**: Long-press home screen → Add "Octane Portfolio" widget → Shows value + change (updates every 10s).

### Tips for Power Users
- Offline mode: View cached balances/history with banner warning.
- Custom RPC: Add your node URL in RPC sheet—auto-fallback on lag.
- Hardware: Pair Ledger via Bluetooth in settings—sign txns cold.
- Dev Easter Eggs: Enable feature flags in hidden settings (long-press version number) for test modes.

## Development Notes

- **Architecture**: MVVM with Compose. Use `viewModel` for state, Coroutines/Flow for async (e.g., RPC calls). Feature flags via BuildConfig for toggling (e.g., xNFT runtime).
- **Modules**: 
  - `core/`: StatusHub (network/biometric), SyncEngine (local DB via Room + cloud stubs).
  - `components/`: Reusable Composables (MorphButton animation, confetti particles via Lottie/Coil).
  - `domain/`: Protocols for assets/signing (stubs for multi-chain).
- **Testing**: Compose UI tests for screens, instrumented for biometrics/hardware.
- **Animations**: Use AnimateContent for morphs, Particles library for confetti.
- **Security**: Keys in Keystore, no plaintext storage. Biometric re-prompt via `onResume()` lifecycle.

## Roadmap

Ultra-granular versions for iterative builds (solo ~10-12 weeks):

- **v0.1**: Wallet setup + biometric.
- **v0.2**: Dashboard + privacy mode + MorphButton.
- **v0.3**: Quick Send + swipes + hold/confetti.
- **v0.4**: Quick Swap + simulation.
- **v0.5**: Banners + RPC manager.
- **v0.6**: NFT gallery + send.
- **v0.7**: Approval revoker.
- **v0.8**: xNFT launchpad.
- **v0.9**: Home widget.
- **v0.10**: Biometric on every resume + polish.
- **v1.0**: Private alpha to friends → ship.

Post-v1.0: Multi-chain if needed (Ethereum stubs via protocols).

## Contributing

This is personal, but if you're a friend:
- Fork and PR: Add features like custom themes or extra animations.
- Issues: Use GitHub for bugs (e.g., Compose crashes on low-end devices).
- Code Style: Kotlin conventions, Compose best practices (no XML layouts).
- Branching: `feature/your-feature` from `main`.

## License

MIT License

Copyright (c) 2025 Your Name

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*Crypto Warning*: This app handles real assets—use at your own risk. Always back up your phrase!
