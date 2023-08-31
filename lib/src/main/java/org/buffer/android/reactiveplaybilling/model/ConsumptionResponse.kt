package org.buffer.android.reactiveplaybilling.model

import com.android.billingclient.api.BillingResult

data class ConsumptionResponse(val billingResult: BillingResult,
                               val outToken: String?)