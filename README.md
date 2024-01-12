# GargGoogleBillingSubsV6

This library provides an easy-to-use interface for handling in-app billing subscriptions in Android apps. It simplifies the integration of Google Play Billing Library Version 6 for subscription-based products.

## Integration

### Step 1: Add the JitPack repository to your build file

Add the following code to your project's `build.gradle` file:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2: Add the dependency
Add the library dependency to your app's build.gradle file:

```gradle
dependencies {
    implementation 'com.github.Nitro2000:GargGoogleBillingSubsV6:1.0.1'
}
```

## Usage
To use the library, create an instance of BillingUtils in your activity or fragment:

```gradle
BillingUtils billingUtils = BillingUtils.getInstance(MainActivity.this, new BillingEventListener() {
    @Override
    public void onPurchaseDone(int purchaseState, String orderId, boolean isAcknowledged, String originalJson) {
        // Handle purchase state
    }

    @Override
    public void onPurchaseAckNow(boolean isAckNow) {
        // Handle purchase acknowledgment
    }

    @Override
    public void onError(int errorCode) {
        // Handle billing errors
    }
});
```
The BillingEventListener provides callback methods to handle various billing events.


