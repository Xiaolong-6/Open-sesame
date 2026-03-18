# 🚗 Open-Sesame: Autoparkki Carport Opener

[![MIT License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-orange.svg)](https://appinventor.mit.edu/)

A lightweight application built with **MIT App Inventor** to automate the opening process of the Autoparkki (EuroPark) carport doors. Originally designed for the Finnoonsilta P-Luoteisrinne location.

---

## 🇬🇧 English Description

### 🌟 Features
- **One-Tap Opening**: No more scanning QR codes or waiting for slow browser pages in the cold.
- **Auto-Remember**: Remembers your license plate locally on your phone (using TinyDB).
- **Session Handling**: Automatically manages CSRF tokens and cookies to prevent "Page Expired" errors.
- **Multi-Garage Support**: This app can work with **other Autoparkki locations**. Simply import the `.aia` file and update the URL in the blocks to match your specific garage's access link.

### 📱 Device Compatibility
- **Android**: Pre-built `.apk` is available in the Releases section.
- **iOS (iPhone/iPad)**: You can run this on iOS! Download the `.aia` source file, upload it to [MIT App Inventor](http://ai2.appinventor.mit.edu/), and use the **MIT AI2 Companion** app from the App Store to run it.

---

## 🇫🇮 Suomeksi (Finnish)

### 🌟 Ominaisuudet
- **Avaus yhdellä painalluksella**: Ei enää QR-koodien skannaamista tai hitaiden selainpivujen odottelua pakkasessa.
- **Automaattinen muisti**: Sovellus muistaa rekisterinumerosi (TinyDB).
- **Istuntojen hallinta**: Hallitsee automaattisesti turvatunnukset ja evästeet, estäen "Sivu vanhentunut" -virheet.
- **Monen autotallin tuki**: Toimii myös muissa Autoparkki-kohteissa. Tuo `.aia`-tiedosto ja päivitä lohkojen URL-osoite vastaamaan omaa kohdettasi.

### 📱 Laitteiden yhteensopivuus
- **Android**: Valmis `.apk`-tiedosto löytyy Releases-osiosta.
- **iOS (iPhone/iPad)**: Lataa `.aia`-lähdetiedosto, siirrä se MIT App Inventoriin ja käytä **MIT AI2 Companion** -sovellusta (App Store).

---

## 🇨🇳 中文说明 (Chinese)

### 🌟 功能特点
- **一键开门**：告别寒风中扫码或等待网页加载的痛苦，实现秒级开门。
- **自动记忆**：通过手机本地存储（TinyDB）保存您的车牌号，无需重复输入。
- **自动会话管理**：自动处理 CSRF 验证码和 Cookie，彻底解决网页常见的“页面已过期”报错。
- **多车库支持**：本程序适用于所有 Autoparkki 停车场。只需导入 `.aia` 源代码并修改 Block 里的网址即可适配不同区域。

### 📱 设备兼容性
- **Android**: 请在 Releases 页面直接下载 `.apk` 安装包。
- **iOS (iPhone/iPad)**: 支持 iOS 运行。下载 `.aia` 源码并上传至 MIT App Inventor 后，通过 App Store 下载 **MIT AI2 Companion** 即可运行。

---

## 🏗️ Development (Source Code)

To modify or adapt for a different garage:
1. Download the **`.aia`** file.
2. Import to [MIT App Inventor](http://ai2.appinventor.mit.edu/).
3. **To change the garage**: In the Blocks editor, locate the URL string in the `btnOpen.Click` and `webTool.GotText` blocks and replace it with your specific Autoparkki access URL.

### UI & Logic Overview
![Screenshot](Screenshot_1.jpg)
![Screenshot](Screenshot_2.jpg)
![Logic Blocks](blocks.png)

---

## 🔒 Security & Privacy (Turvallisuus ja tietosuoja / 安全与隐私)

- **🇬🇧 Local Storage Only**: Your license plate is stored exclusively on your device using `TinyDB`. No data is ever uploaded to any third-party servers or databases controlled by the developer.
- **🇫🇮 Vain paikallinen tallennus**: Rekisterinumerosi tallennetaan ainoastaan laitteellesi. Kehittäjä tai kolmannet osapuolet eivät kerää tai tallenna tietojasi.
- **🇨🇳 本地存储**：您的车牌号仅保存在手机本地。开发者或任何第三方均无法访问或收集您的个人数据。
- **Transparency**: The app communicates directly with `autoparkki.fi`. The source code is open for inspection.

---

## ⚖️ License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
