package com.garg.billingsubv6;

public interface BillingEventListener {


    void onPurchaseDone(int purchaseState, String orderId, boolean isAcknowledged, String originalJson);

    void onPurchaseAckNow(boolean isAckNow);

    void onError(int errorCode);

}





