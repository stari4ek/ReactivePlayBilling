package org.buffer.android.reactiveplaybilling.model

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails

sealed class ItemsForSubscriptionResponse(@BillingClient.BillingResponse val responseCode: Int,
                                          val skuDetails: List<SkuDetails>? = null) {

    data class ItemsForSubscriptionSuccess(@BillingClient.BillingResponse
                                         private val billingResponse: Int,
                                         val items: List<SkuDetails>)
        : ItemsForSubscriptionResponse(billingResponse, items)

    data class ItemsForSubscriptionFailure(@BillingClient.BillingResponse
                                         private val billingResponse: Int)
        : ItemsForSubscriptionResponse(billingResponse)

}