package com.garg.billingsubv6;

public interface BillingStatusListener {

    void onPurchaseStatus(String prodId, String originalJson, String orderId, boolean isOwned);

    void onStatusError(int errorCode);

}