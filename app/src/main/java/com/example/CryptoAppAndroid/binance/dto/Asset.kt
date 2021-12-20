package com.example.CryptoAppAndroid.binance.dto

/**
 * An asset Binance supports.
 */
data class Asset (
	val id: String,
	val assetCode: String,
	val assetName: String,
	val unit: String,
	val transactionFee: String,
	val commissionRate: String,
	val freeAuditWithdrawAmount: String,
	val freeUserChargeAmount: String,
	val minProductWithdraw: String,
	val withdrawIntegerMultiple: String,
	val confirmTimes: Long,
	val enableWithdraw: Boolean,
	val isLegalMoney: Boolean
)