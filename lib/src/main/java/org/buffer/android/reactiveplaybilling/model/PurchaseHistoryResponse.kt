package org.buffer.android.reactiveplaybilling.model

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchaseHistoryRecord

data class PurchaseHistoryResponse(val billingResult: BillingResult,
                                   val purchaseHistoryRecords: List<PurchaseHistoryRecord>? = null)