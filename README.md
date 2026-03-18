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
- **Multi-Garage Support**: Easily adaptable to **any Autoparkki location**. 

### 🔧 How to Adapt for Your Garage
1. **Scan**: Scan the QR code on your specific garage door to get your unique access URL (e.g., `https://dc.autoparkki.fi/access/...`).
2. **Download**: Get the `.aia` source file from this repository.
3. **Import**: Upload the `.aia` to [MIT App Inventor](http://ai2.appinventor.mit.edu/).
4. **Update**: In the Blocks editor, locate the URL string in the `btnOpen.Click` and `webTool.GotText` blocks and replace it with your own URL.

---

## 🇫🇮 Suomeksi (Finnish)

### 🌟 Ominaisuudet
- **Avaus yhdellä painalluksella**: Ei enää QR-koodien skannaamista pakkasessa.
- **Automaattinen muisti**: Sovellus muistaa rekisterinumerosi (TinyDB).
- **Istuntojen hallinta**: Estää "Sivu vanhentunut" -virheet hallitsemalla evästeet ja tunnukset automaattisesti.

### 🔧 Näin mukautat sovelluksen omalle autotallillesi
1. **Skannaa**: Skannaa autotallisi oven QR-koodi saadaksesi oman pääsyosoitteesi.
2. **Lataa**: Lataa `.aia`-lähdetiedosto tästä arkistosta.
3. **Tuo**: Siirrä tiedosto [MIT App Inventoriin](http://ai2.appinventor.mit.edu/).
4. **Päivitä**: Etsi URL-osoite `btnOpen.Click`- ja `webTool.GotText`-lohkoista ja korvaa se omalla osoitteellasi.

---

## 🇨🇳 中文说明 (Chinese)

### 🌟 功能特点
- **一键开门**：告别寒风中扫码或等待网页加载的痛苦，实现秒级开门。
- **自动记忆**：通过本地存储保存车牌号，无需重复输入。
- **多车库支持**：适用于所有 Autoparkki 停车场。

### 🔧 如何适配您自己的车库
1. **扫码**：扫描您车库门上的二维码，获取专属访问网址（例如：`https://dc.autoparkki.fi/access/...`）。
2. **下载**：从本仓库下载 `.aia` 源代码文件。
3. **导入**：将源代码上传至 [MIT App Inventor](http://ai2.appinventor.mit.edu/)。
4. **修改**：在逻辑设计（Blocks）界面，找到 `btnOpen.Click` 和 `webTool.GotText` 模块中的网址字符串，将其替换为您扫描得到的网址即可。

---

## 🏗️ UI & Logic Overview
![Screenshot](Screenshot_1.jpg)
![Screenshot](Screenshot_2.jpg)
![Logic Blocks](blocks.png)

---

## 🔒 Security & Privacy (Turvallisuus ja tietosuoja / 安全与隐私)

- **🇬🇧 Local Storage Only**: Your license plate is stored exclusively on your device. No data is ever uploaded to third-party servers.
- **🇫🇮 Vain paikallinen tallennus**: Rekisterinumerosi tallennetaan ainoastaan laitteellesi. Kehittäjä ei kerää tai tallenna tietojasi.
- **🇨🇳 本地存储**：您的车牌号仅保存在手机本地。开发者或任何第三方均无法访问或收集您的个人数据。

---

## ⚖️ License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
