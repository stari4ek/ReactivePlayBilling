# Reactive Play Billing

This project acts us a simple wrapper for the [Play Billing Library](https://developer.android.com/google/play/billing/billing_library.html) from Google for Android. This allows you to interact with the library in a reactive manner and use it within your reactive streams.

# Functionality

Reactive Play Billing currently supports most of the operations that you will find within the library itself.

## Connecting to Play Billing

You can connect to Play Billing by using the `connect()` method and subscribing to a changes in the connection status.

```kotlin
reactiveBilling.connect()
    .subscribe({
        // Play billing connection successful
    }, {
        // Play billing connection failed / disconnected
    })
```


## Observing purchase changes

You can observe purchase change by using the observePurchaseUpdates()` method. This will be called when the user purchases and item / subscription to provide you with the status of the change. You **must** subscribe to this method if you are carrying our purchases.

```kotlin
reactiveBilling.observePurchaseUpdates()
  .subscribe({
        // Purchase complete, handle result
    }, {
        // Purchase failed, handle result
    })
```

## Querying in-app items for purchase

You can query items that are available for purchase using the `queryInAppsForPurchase()` will be returned.

```kotlin
reactiveBilling.queryInAppsForPurchase(productsIdsList)
    .subscribe({
        // Handle items returned
    }, {
        // Handle item retrieval failure
    })
```
            
## Querying subscriptions for purchase

You can query subscriptions that are available for purchase using the `querySubscriptionsForPurchase()` will be returned.

```kotlin
reactiveBilling.querySubscriptionsForPurchase(productsIdsList)
    .subscribe({
        // Handle items returned
    }, {
        // Handle item retrieval failure
    })
```
            
## Purchasing an in-app Item or subscription

You can purchase in-app items by calling the `purchase` method. When calling this you need to pass in the product id of the item which you wish to perform the purchase request on, followed by a reference to the current activity - this is required for activity result events.

```kotlin
reactiveBilling.purchaseItem(productDetails, isOfferPersonalized, activity)
    .subscribe({
        // Handle purchase success
    }, {
        // Handle purchase failure
    })
```
            

## Querying purchases of in-app items or subscriptions

You can query purchases for the current user by using `queryInAppPurchases()`/`querySubscriptions()` observables. This will return you a list of Purchase instances upon a successful request.

```kotlin
reactiveBilling.querySubscriptions()
    .subscribe({
        // Handle purchase 
    }, {
        // Handle failure
    })
```
            
 ## Consuming an in-app item purchase

You can consume an item that has been purchases by using `consumeItem()`.

```kotlin
reactiveBilling.consumeItem()
    .subscribe({
        // Handle consumption success 
    }, {
        // Handle consumption failure
    })
```
    
