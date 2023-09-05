package org.buffer.android.reactiveplaybilling.model

import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingResult
import java.util.*

class BillingOperationException internal constructor(msg: String, val result: BillingResult) :
    Exception(
        String.format(
            Locale.US, "%s Error: %s: %s",
            msg,
            codeToString(result.responseCode),
            result.debugMessage
        )
    ) {
    companion object {
        @JvmStatic
        fun codeToString(@BillingResponseCode response: Int): String {
            return when (response) {
                BillingResponseCode.SERVICE_TIMEOUT -> "service_timeout"
                BillingResponseCode.FEATURE_NOT_SUPPORTED -> "feature_not_supported"
                BillingResponseCode.SERVICE_DISCONNECTED -> "service_disconnected"
                BillingResponseCode.OK -> "ok"
                BillingResponseCode.USER_CANCELED -> "user_cancelled"
                BillingResponseCode.SERVICE_UNAVAILABLE -> "service_unavailable"
                BillingResponseCode.BILLING_UNAVAILABLE -> "billing_unavailable"
                BillingResponseCode.ITEM_UNAVAILABLE -> "item_unavailable"
                BillingResponseCode.DEVELOPER_ERROR -> "developer_error"
                BillingResponseCode.ERROR -> "error"
                BillingResponseCode.ITEM_ALREADY_OWNED -> "item_already_owned"
                BillingResponseCode.ITEM_NOT_OWNED -> "item_not_owned"
                else -> "unknown"
            }
        }
    }
}