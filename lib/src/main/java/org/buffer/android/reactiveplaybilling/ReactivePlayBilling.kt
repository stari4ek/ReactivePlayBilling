package org.buffer.android.reactiveplaybilling

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.buffer.android.reactiveplaybilling.model.ConnectionResult
import org.buffer.android.reactiveplaybilling.model.ConsumptionResponse
import org.buffer.android.reactiveplaybilling.model.ItemsForPurchaseResponse
import org.buffer.android.reactiveplaybilling.model.ItemsForSubscriptionResponse
import org.buffer.android.reactiveplaybilling.model.PurchaseResponse
import org.buffer.android.reactiveplaybilling.model.PurchasesUpdatedResponse
import org.buffer.android.reactiveplaybilling.model.QueryPurchasesResponse
import org.buffer.android.reactiveplaybilling.model.QuerySubscriptionsResponse
import org.buffer.android.reactiveplaybilling.model.SubscriptionResponse

open class ReactivePlayBilling constructor(context: Context) : PurchasesUpdatedListener {

    private val publishSubject = PublishSubject.create<PurchasesUpdatedResponse>()
    private var billingClient: BillingClient =
            BillingClient.newBuilder(context).setListener(this).build()

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.OK) {
            publishSubject.onNext(PurchasesUpdatedResponse.PurchasesUpdatedSuccess(responseCode,
                    purchases))
        } else {
            publishSubject.onNext(PurchasesUpdatedResponse.PurchaseUpdatedFailure(responseCode))
        }
    }

    open fun connect(): Observable<ConnectionResult> {
        return Observable.create {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(@BillingClient.BillingResponse
                                                    responseCode: Int) {
                    if (responseCode == BillingClient.BillingResponse.OK) {
                        it.onNext(ConnectionResult.ConnectionSuccess(responseCode))
                    } else {
                        it.onNext(ConnectionResult.ConnectionFailure(responseCode))
                    }
                }

                override fun onBillingServiceDisconnected() {
                    it.onNext(ConnectionResult.ConnectionFailure())
                }
            })
        }
    }

    open fun disconnect(): Completable {
        return Completable.defer {
            billingClient.endConnection()
            Completable.complete()
        }
    }

    open fun observePurchaseUpdates(): Observable<PurchasesUpdatedResponse> {
        return publishSubject
    }

    open fun queryItemsForPurchase(skuList: List<String>): Single<ItemsForPurchaseResponse> {
        return Single.create {
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
            billingClient.querySkuDetailsAsync(params.build()) { responseCode, p1 ->
                if (responseCode == BillingClient.BillingResponse.OK) {
                    it.onSuccess(ItemsForPurchaseResponse.ItemsForPurchaseSuccess(responseCode, p1))
                } else {
                    it.onSuccess(ItemsForPurchaseResponse.ItemsForPurchaseFailure(responseCode))
                }
            }
        }
    }

    fun querySubscriptionsForPurchase(skuList: List<String>):
            Single<ItemsForSubscriptionResponse> {
        return Single.create {
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
            billingClient.querySkuDetailsAsync(params.build()) { responseCode, p1 ->
                if (responseCode == BillingClient.BillingResponse.OK) {
                    it.onSuccess(ItemsForSubscriptionResponse.ItemsForSubscriptionSuccess(responseCode,
                                 p1))
                } else {
                    it.onSuccess(ItemsForSubscriptionResponse.ItemsForSubscriptionFailure(
                                 responseCode))
                }
            }
        }
    }

    open fun purchaseItem(skuId: String, activity: Activity): Single<PurchaseResponse> {
        return Single.create {
            val flowParams = BillingFlowParams.newBuilder()
                    .setSku(skuId)
                    .setType(BillingClient.SkuType.INAPP)
                    .build()
            val responseCode = billingClient.launchBillingFlow(activity, flowParams)
            if (responseCode == BillingClient.BillingResponse.OK) {
                it.onSuccess(PurchaseResponse.PurchaseSuccess(responseCode))
            } else {
                it.onSuccess(PurchaseResponse.PurchaseFailure(responseCode))
            }
        }
    }

    open fun queryPurchases(): Single<List<Purchase>> {
        return Single.create {
            val queryResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP)

            if (queryResult.responseCode == BillingClient.BillingResponse.OK) {
                it.onSuccess(queryResult.purchasesList)
            } else {
                it.onError(Throwable("Failed to query purchases. Response code: ${queryResult.responseCode}"))
            }
        }
    }

    open fun queryPurchaseHistory(): Single<QueryPurchasesResponse> {
        return Single.create {
            billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) {
                responseCode, result ->
                if (responseCode == BillingClient.BillingResponse.OK && result != null) {
                    it.onSuccess(QueryPurchasesResponse.QueryPurchasesSuccess(responseCode, result))
                } else {
                    it.onSuccess(QueryPurchasesResponse.QueryPurchasesFailure(responseCode))
                }
            }
        }
    }

    open fun querySubscriptions(): Single<List<Purchase>> {
        return Single.create {
            val queryResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS)

            if (queryResult.responseCode == BillingClient.BillingResponse.OK) {
                it.onSuccess(queryResult.purchasesList)
            } else {
                it.onError(Throwable("Failed to query subscriptions. Response code: ${queryResult.responseCode}"))
            }
        }
    }

    open fun querySubscriptionHistory(): Single<QuerySubscriptionsResponse> {
        return Single.create {
            billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS)
            { responseCode, result ->
                if (responseCode == BillingClient.BillingResponse.OK && result != null) {
                    it.onSuccess(QuerySubscriptionsResponse.QuerySubscriptionsSuccess(responseCode,
                                 result))
                } else {
                    it.onSuccess(QuerySubscriptionsResponse.QuerySubscriptionsFailure(responseCode))
                }

            }
        }
    }

    open fun consumeItem(purchaseToken: String): Single<ConsumptionResponse> {
        return Single.create {
            billingClient.consumeAsync(purchaseToken) { responseCode, outToken ->
                if (responseCode == BillingClient.BillingResponse.OK) {
                    it.onSuccess(ConsumptionResponse.ConsumptionSuccess(responseCode, outToken))
                } else {
                    it.onSuccess(ConsumptionResponse.ConsumptionFailure(responseCode))
                }
            }
        }
    }

    open fun purchaseSubscription(skuId: String, activity: Activity):
            Single<SubscriptionResponse> {
        return Single.create {
            val flowParams = BillingFlowParams.newBuilder()
                    .setSku(skuId)
                    .setType(BillingClient.SkuType.SUBS)
                    .build()
            val responseCode = billingClient.launchBillingFlow(activity, flowParams)
            if (responseCode == BillingClient.BillingResponse.OK) {
                it.onSuccess(SubscriptionResponse.SubscriptionSuccess(responseCode))
            } else {
                it.onSuccess(SubscriptionResponse.SubscriptionFailure(responseCode))
            }
        }
    }

    open fun upgradeSubscription(oldSkuId: String, newSkuId: String, activity: Activity):
            Single<SubscriptionResponse> {
        return Single.create {
            val flowParams = BillingFlowParams.newBuilder()
                    .setOldSku(oldSkuId)
                    .setSku(newSkuId)
                    .setType(BillingClient.SkuType.SUBS)
                    .build()
            val responseCode = billingClient.launchBillingFlow(activity, flowParams)
            if (responseCode == BillingClient.BillingResponse.OK) {
                it.onSuccess(SubscriptionResponse.SubscriptionSuccess(responseCode))
            } else {
                it.onSuccess(SubscriptionResponse.SubscriptionFailure(responseCode))
            }
        }
    }

}