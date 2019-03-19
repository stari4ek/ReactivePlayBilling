package org.buffer.android.reactiveplaybilling.model

import com.android.billingclient.api.BillingClient

sealed class ConnectionResult

object ConnectionSuccess : ConnectionResult()

data class ConnectionFailure(@BillingClient.BillingResponse val responseCode: Int)
    : ConnectionResult()

object ConnectionDisconnected : ConnectionResult()