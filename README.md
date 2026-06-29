# Passport Photo PRO 📸

A premium, modern Android application built using **Jetpack Compose** that allows users to easily crop, align, and format standard passport and ID photos. The app is optimized for professional print preparation and complies with official biometric guidelines.

---

## ✨ Features

- **Biometric Crop & Alignment Canvas**: Super smooth pinch-to-zoom and drag/pan photo scaling that locks parent page scroll during interactions for precise chin and eye alignment.
- **Preset Sizing Options**: Predefined standards for PAN Card, National IDs, and standard passport photo formats.
- **HD Quality Rendering (300 DPI)**: Renders output sheets at professional-grade 300 DPI resolution, exported as lossless PNG formats for crisp print results.
- **Ad-Supported HD Download**: Integrated monetization option to watch ads in exchange for high-definition layout downloads.
- **Print Layout Customization**: Fine-tune sheet spacing, margin boundaries, grid spacing, toggles for cutting guide lines, and photo borders (supporting standard layouts like A4 and 4R sheets).
- **Accessibility & Contrast Compliance**: WCAG contrast compliant themes (light-blue daylight mode and deep slate dark mode) with large, friendly, custom-designed emoji controls (☀️/🌙).
- **Dismiss Keyboard Easily**: Software keyboards automatically slide away when tapping empty spaces or background areas.

---

## 🛠️ Build & Installation

### Prerequisites
- **Android Studio** (Koala or newer)
- **JDK 17 or 21** (embedded JDK from Android Studio is recommended)
- A connected Android device with **USB Debugging** enabled.

### Quick Run
1. Open the project folder in **Android Studio**.
2. Wait for Gradle sync to complete.
3. Build and deploy to your connected phone using Gradle:
   ```bash
   ./gradlew installDebug
   ```

*Note: The project comes pre-configured with a local `debug.keystore` in the root folder for seamless code-signing.*

---

## 💻 Tech Stack
- **Framework**: Jetpack Compose (Kotlin)
- **Design System**: Material Design 3
- **Image Processing**: Android Canvas & BitmapFactory
- **Build System**: Gradle (Kotlin Script - `build.gradle.kts`)

---

## 📬 Support & Contact

For support, feature requests, or report issues, reach out to the developer directly:
- 📧 **Email**: [nxdecore@gmail.com](mailto:nxdecore@gmail.com)
- 📸 **Instagram**: [@nishix_vamp](https://instagram.com/nishix_vamp)
