# Crypto App Android

This Android app accompanies the Python CryptoApp program.

## Installation

To install, download the .zip of this project.

## Usage
Replace the two variables in the app level build.gradle file shown below with your personal Binance API Key and Binance API Secret.

Information on how to create these can be found here: [How to Create API](https://www.binance.com/en/support/faq/360002502072)

```gradle
buildConfigField "String", "APIKEY", "\"YOUR-API-KEY\""
buildConfigField "String", "APISECRET", "\"YOUR-SECRET\""
```

https://github.com/pythymcpyface/CryptoAppAndroid/new/main/


Pressing the Floating Action Button will initiate two workers:
- One which syncs the data from the Python API
- One which syncs trades from the Binance API