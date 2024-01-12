package com.garg.billingsubv6;

public interface BillingConnectionListener {

    void onConnectionStart(boolean start);

    void onConnectionError(int errorCode);

}