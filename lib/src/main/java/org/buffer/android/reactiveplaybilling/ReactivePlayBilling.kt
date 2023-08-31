package org.buffer.android.reactiveplaybilling

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.buffer.android.reactiveplaybilling.model.*

class ReactivePlayBilling constructor(context: Context) {

    private val purchasesUpdatedSubject = PublishSubject.create<PurchasesUpdatedResponse>()

    private val purchasesUpdatedListener = PurchasesUpdatedListener {
        billingResult, purchases ->
            purchasesUpdatedSubject.onNext(PurchasesUpdatedResponse(billingResult, purchases))
    }

    private var billingClient: BillingClient = BillingClient
        .newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    //

    fun connect(): Observable<ConnectionResult> {
        return Observable.create {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        it.onNext(ConnectionSuccess)
                    } else {
                        it.onNext(ConnectionFailure(billingResult))
                    }
                }

                override fun onBillingServiceDisconnected() {
                    it.onNext(ConnectionDisconnected)
                }
            })
        }
    }

    fun disconnect(): Completable {
        return Completable.defer {
            billingClient.endConnection()
            Completable.complete()
        }
    }

    fun observePurchaseUpdates(): Observable<PurchasesUpdatedResponse> {
        return purchasesUpdatedSubject
    }

    fun queryInAppsForPurchase(skuList: List<String>): Single<SkuDetailsResponse> {
        return querySkuDetails(BillingClient.SkuType.INAPP, skuList)
    }

    fun querySubscriptionsForPurchase(skuList: List<String>): Single<SkuDetailsResponse> {
        return querySkuDetails(BillingClient.SkuType.SUBS, skuList)
    }

    fun queryInAppPurchases(): Single<PurchasesResponse> {
        return queryPurchases(BillingClient.SkuType.INAPP)
    }

    fun querySubscriptions(): Single<PurchasesResponse> {
        return queryPurchases(BillingClient.SkuType.SUBS)
    }

    fun queryInAppPurchaseHistory(): Single<PurchaseHistoryResponse> {
        return queryPurchaseHistory(BillingClient.SkuType.INAPP)
    }

    fun querySubscriptionHistory(): Single<PurchaseHistoryResponse> {
        return queryPurchaseHistory(BillingClient.SkuType.SUBS)
    }

    /**
     * Should be subscribed on UI thread
     */
    fun purchase(sku: SkuDetails, activity: Activity): Single<BillingResult> {
        return Single.create {
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(sku)
                .build()
            it.onSuccess(billingClient.launchBillingFlow(activity, flowParams))
        }
    }

    fun updatePurchase(oldPurchaseToken: String, newSku: SkuDetails, activity: Activity):
            Single<BillingResult> {
        return Single.create {
            val flowParams = BillingFlowParams.newBuilder()
                .setSubscriptionUpdateParams(
                    SubscriptionUpdateParams
                        .newBuilder()
                        .setOldSkuPurchaseToken(oldPurchaseToken)
                        .build()
                )
                .setSkuDetails(newSku)
                .build()
            it.onSuccess(billingClient.launchBillingFlow(activity, flowParams))
        }
    }

    fun consumeItem(purchaseToken: String): Single<ConsumptionResponse> {
        return Single.create {
            billingClient.consumeAsync(ConsumeParams
                .newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
            ) {
                    billingResult, outToken ->
                it.onSuccess(ConsumptionResponse(billingResult, outToken))
            }
        }
    }

    //

    private fun querySkuDetails(
            @BillingClient.SkuType skuType: String,
            skuList: List<String>): Single<SkuDetailsResponse> {

        return Single.create {
            billingClient.querySkuDetailsAsync(SkuDetailsParams
                .newBuilder()
                .setSkusList(skuList)
                .setType(skuType)
                .build()
            ) { billingResult, skus ->
                it.onSuccess(SkuDetailsResponse(billingResult, skus))
            }
        }
    }

    private fun queryPurchaseHistory(@BillingClient.SkuType skuType: String): Single<PurchaseHistoryResponse> {
        return Single.create {
            billingClient.queryPurchaseHistoryAsync(skuType) {
                    billingResult, historyRecords ->
                it.onSuccess(PurchaseHistoryResponse(billingResult, historyRecords))
            }
        }
    }

    private fun queryPurchases(@BillingClient.SkuType skuType: String): Single<PurchasesResponse> {
        return Single.create {
            billingClient.queryPurchasesAsync(skuType) {
                billingResult, purchases ->
                    it.onSuccess(PurchasesResponse(billingResult, purchases))
            }
        }
    }

}