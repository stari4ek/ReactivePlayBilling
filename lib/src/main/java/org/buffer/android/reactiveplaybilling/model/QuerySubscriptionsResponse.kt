package org.buffer.android.reactiveplaybilling.model

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase

sealed class QuerySubscriptionsResponse(@BillingClient.BillingResponse val responseCode: Int,
                                        val purchases: List<Purchase>? = null) {

    data class QuerySubscriptionsSuccess(@BillingClient.BillingResponse
                                         private val billingResponse: Int,
                                         private val items: List<Purchase>)
        : QuerySubscriptionsResponse(billingResponse, items)

    data class QuerySubscriptionsFailure(@BillingClient.BillingResponse
                                         private val billingResponse: Int)
        : QuerySubscriptionsResponse(billingResponse)

}