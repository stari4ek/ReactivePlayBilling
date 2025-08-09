package org.buffer.android.reactiveplaybilling

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams
import io.reactivex.Completable
import io.reactivex.Maybe
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
                    if (billingResult.responseCode == BillingResponseCode.OK) {
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

    fun queryInAppsForPurchase(productsIds: List<String>): Single<List<ProductDetails>> {
        return queryProductsDetails(BillingClient.ProductType.INAPP, productsIds)
    }

    fun querySubscriptionsForPurchase(productsIds: List<String>): Single<List<ProductDetails>> {
        return queryProductsDetails(BillingClient.ProductType.SUBS, productsIds)
    }

    fun queryInAppPurchases(): Single<List<Purchase>> {
        return queryPurchases(BillingClient.ProductType.INAPP)
    }

    fun querySubscriptions(): Single<List<Purchase>> {
        return queryPurchases(BillingClient.ProductType.SUBS)
    }

    /**
     * Should be subscribed on UI thread
     */
    fun purchase(
        productDetails: ProductDetails,
        offerToken: String,
        isOfferPersonalized: Boolean,
        activity: Activity
    ): Completable {
        return Completable.create {
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(
                    BillingFlowParams.ProductDetailsParams
                        .newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                ))
                .setIsOfferPersonalized(isOfferPersonalized)
                .build()
            val billingResult = billingClient.launchBillingFlow(activity, flowParams)
            if (billingResult.responseCode == BillingResponseCode.OK) {
                it.onComplete()
            } else {
                it.onError(BillingOperationException("Failed to launch billing flow", billingResult))
            }
        }
    }

    /**
     * Should be subscribed on UI thread
     */
    fun updatePurchase(
        oldPurchaseToken: String,
        newProductDetails: ProductDetails,
        offerToken: String,
        isOfferPersonalized: Boolean,
        activity: Activity
    ): Completable {
        return Completable.create {
            val flowParams = BillingFlowParams.newBuilder()
                .setSubscriptionUpdateParams(
                    SubscriptionUpdateParams
                        .newBuilder()
                        .setOldPurchaseToken(oldPurchaseToken)
                        .build()
                )
                .setProductDetailsParamsList(listOf(
                    BillingFlowParams.ProductDetailsParams
                        .newBuilder()
                        .setProductDetails(newProductDetails)
                        .setOfferToken(offerToken)
                        .build()
                ))
                .setIsOfferPersonalized(isOfferPersonalized)
                .build()
            val billingResult = billingClient.launchBillingFlow(activity, flowParams)
            if (billingResult.responseCode == BillingResponseCode.OK) {
                it.onComplete()
            } else {
                it.onError(BillingOperationException("Failed to launch update billing flow", billingResult))
            }
        }
    }

    /**
     * Should be subscribed on UI thread
     */
    fun showInAppMessages(activity: Activity): Maybe<String> {
        return Maybe.create {
            val inAppMessageParams = InAppMessageParams.newBuilder()
                .addInAppMessageCategoryToShow(InAppMessageParams.InAppMessageCategoryId.TRANSACTIONAL)
                .build()

            val billingResult = billingClient.showInAppMessages(activity, inAppMessageParams) {
                    inAppMessageResult ->
                if (inAppMessageResult.responseCode == InAppMessageResult.InAppMessageResponseCode.NO_ACTION_NEEDED) {
                    // The flow has finished and there is no action needed from developers.
                    it.onComplete()
                } else if (inAppMessageResult.responseCode
                    == InAppMessageResult.InAppMessageResponseCode.SUBSCRIPTION_STATUS_UPDATED) {
                    // The subscription status changed. For example, a subscription
                    // has been recovered from a suspend state. Developers should
                    // expect the purchase token to be returned with this response
                    // code and use the purchase token with the Google Play
                    // Developer API.

                    it.onSuccess(inAppMessageResult.purchaseToken!!)
                }
            }
            if (billingResult.responseCode != BillingResponseCode.OK) {
                it.onError(BillingOperationException("Failed to show in-app messages", billingResult))
            }
        }
    }

    fun consumeItem(purchaseToken: String): Single<String> {
        return Single.create {
            billingClient.consumeAsync(ConsumeParams
                .newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
            ) { billingResult, outToken ->
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    it.onSuccess(outToken)
                } else {
                    it.onError(BillingOperationException("Failed to consume purchase", billingResult))
                }
            }
        }
    }

    //

    private fun queryProductsDetails(
        @BillingClient.ProductType productType: String,
        skuList: List<String>
    ): Single<List<ProductDetails>> {

        val queryProductDetailsParams = QueryProductDetailsParams
            .newBuilder()
            .setProductList(
                skuList.map { sku -> QueryProductDetailsParams.Product
                    .newBuilder()
                    .setProductId(sku)
                    .setProductType(productType)
                    .build()
                }
            )
            .build()

        return Single.create {
            billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetails ->
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    it.onSuccess(productDetails)
                } else {
                    it.onError(BillingOperationException("Failed to query product details", billingResult))
                }
            }
        }
    }


    private fun queryPurchases(@BillingClient.ProductType productType: String): Single<List<Purchase>> {
        return Single.create {
            billingClient.queryPurchasesAsync(QueryPurchasesParams
                .newBuilder()
                .setProductType(productType)
                .build()
            ) {
                    billingResult, purchases ->

                if (billingResult.responseCode == BillingResponseCode.OK) {
                    it.onSuccess(purchases)
                } else {
                    it.onError(BillingOperationException("Failed to query purchases", billingResult))
                }
            }
        }
    }

}