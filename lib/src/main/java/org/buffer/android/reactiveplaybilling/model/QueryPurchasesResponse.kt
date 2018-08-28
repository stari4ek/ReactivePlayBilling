package org.buffer.android.reactiveplaybilling.model

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase

sealed class QueryPurchasesResponse(@BillingClient.BillingResponse val responseCode: Int,
                                    val purchases: List<Purchase>? = null) {

    data class QueryPurchasesSuccess(@BillingClient.BillingResponse
                                     private val billingResponse: Int,
                                     private val items: List<Purchase>?)
        : QueryPurchasesResponse(billingResponse, items)

    data class QueryPurchasesFailure(@BillingClient.BillingResponse
                                     private val billingResponse: Int)
        : QueryPurchasesResponse(billingResponse)

}