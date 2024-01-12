package com.garg.billingsubv6;

public interface BillingProdInfoListener {

    void productInfo(String prodDescription, String prodTitle, String formattedPrice);

    void onStatusError(int errorCode);

}