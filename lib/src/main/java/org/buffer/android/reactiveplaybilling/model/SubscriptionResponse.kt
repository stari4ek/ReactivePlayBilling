package org.buffer.android.reactiveplaybilling.model

import com.android.billingclient.api.BillingClient

sealed class SubscriptionResponse(@BillingClient.BillingResponse val responseCode: Int) {

    data class SubscriptionSuccess(@BillingClient.BillingResponse private val billingResponse: Int)
        : SubscriptionResponse(billingResponse)

    data class SubscriptionFailure(@BillingClient.BillingResponse
                                   private val billingResponse: Int)
        : SubscriptionResponse(billingResponse)

}