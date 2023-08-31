package org.buffer.android.reactiveplaybilling.model

import com.android.billingclient.api.BillingResult

sealed class ConnectionResult

object ConnectionSuccess : ConnectionResult()

data class ConnectionFailure(val billingResult: BillingResult)
    : ConnectionResult()

object ConnectionDisconnected : ConnectionResult()