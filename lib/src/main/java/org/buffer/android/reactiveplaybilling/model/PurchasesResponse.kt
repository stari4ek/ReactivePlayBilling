package org.buffer.android.reactiveplaybilling.model

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase

data class PurchasesResponse(val billingResult: BillingResult,
                             val purchases: List<Purchase>? = null)