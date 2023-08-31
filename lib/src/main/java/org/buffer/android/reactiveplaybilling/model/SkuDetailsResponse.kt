package org.buffer.android.reactiveplaybilling.model

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.SkuDetails

data class SkuDetailsResponse(val billingResult: BillingResult,
                              val skuDetails: List<SkuDetails>? = null)