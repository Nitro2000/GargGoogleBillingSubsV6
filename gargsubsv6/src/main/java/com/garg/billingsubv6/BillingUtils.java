package com.garg.billingsubv6;



import static com.garg.billingsubv6.BillConstants.SERVICE_DISCONNECTED;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BillingUtils {

    private static BillingEventListener eventHandler;


    private BillingClient billingClientInstance = null;
    private boolean isConnected = false;


    private final Handler handler = new Handler(Looper.getMainLooper());

    private static volatile BillingUtils instance;

    public static BillingUtils getInstance(@NonNull Context context, @NonNull BillingEventListener billEventHandler) {
        if (instance == null) {
            synchronized (BillingUtils.class) {
                if (instance == null) {
                    instance = new BillingUtils(context);
                }
            }
        }
        eventHandler = billEventHandler;
        return instance;
    }

    private void initiateBillingClient(Context context) {

        billingClientInstance = BillingClient.newBuilder(context)
                .setListener(getPurchasesUpdatedListener(context))
                .enablePendingPurchases()
                .build();
//        startConnection();
    }


    private PurchasesUpdatedListener getPurchasesUpdatedListener(Context context) {

        return (billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                for (Purchase purchase : list) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        handlePurchase(purchase);
                        callPurchaseDone(purchase.getPurchaseState(), purchase.getOrderId(), purchase.isAcknowledged(), purchase.getOriginalJson());
                    }
                }
            } else {
                eventHandler.onError(billingResult.getResponseCode());
            }
        };
    }


    private void handlePurchase(Purchase purchase) {

        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

            billingClientInstance.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        callPurchaseAckNow(true);
                    } else {
                        callPurchaseAckNow(false);
                        callError(BillConstants.ACKN_FAILED);
                    }
                }
            });
        }
    }

    private BillingUtils() {
    }

    private BillingUtils(@NonNull Context context) {

        initiateBillingClient(context);

    }

    private void startConnection(BillingConnectionListener billingConnectionListener) {
        billingClientInstance.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

                callError(SERVICE_DISCONNECTED);

                billingConnectionListener.onConnectionError(SERVICE_DISCONNECTED);
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    isConnected = true;
                    billingConnectionListener.onConnectionStart(true);
                } else {
                    callError(SERVICE_DISCONNECTED);
                    billingConnectionListener.onConnectionError(SERVICE_DISCONNECTED);
                }
            }
        });
    }

    public void subscribe(@NonNull Activity activity,@NonNull String prodId) {
        if (!isConnected) {

            startConnection(new BillingConnectionListener() {
                @Override
                public void onConnectionStart(boolean start) {
                    utilsSubscribe(activity, prodId);
                }

                @Override
                public void onConnectionError(int errorCode) {

                }
            });
        } else {

            utilsSubscribe(activity, prodId);
        }
    }

    private void utilsSubscribe(Activity activity, String prodId) {

        List<QueryProductDetailsParams.Product> prodList = new ArrayList<>();
        prodList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(prodId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build());
        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(prodList)
                        .build();

        billingClientInstance.queryProductDetailsAsync(
                queryProductDetailsParams,
                (billingResult1, productDetailsList) -> {

                    for (ProductDetails prod : productDetailsList) {

                        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
                        if (prod.getSubscriptionOfferDetails() != null) {
                            if (prod.getSubscriptionOfferDetails().get(0) != null) {
                                productDetailsParamsList.add(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                                .setProductDetails(prod)
                                                .setOfferToken(prod.getSubscriptionOfferDetails().get(0).getOfferToken())
                                                .build()
                                );
                            } else {
                                productDetailsParamsList.add(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                                .setProductDetails(prod)
                                                .build()
                                );
                            }
                        } else {
                            productDetailsParamsList.add(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(prod)
                                            .build()
                            );
                        }

                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build();
                        billingClientInstance.launchBillingFlow(activity, billingFlowParams);
                    }

                }
        );

    }

    public void checkStatus(@NonNull String productId, @NonNull BillingStatusListener statusHandler) {
        if (!isConnected) {
            startConnection(new BillingConnectionListener() {
                @Override
                public void onConnectionStart(boolean start) {
                    utilsCheckStatus(productId, statusHandler);
                }

                @Override
                public void onConnectionError(int errorCode) {

                }
            });
        } else {
            utilsCheckStatus(productId, statusHandler);
        }
    }

    private void utilsCheckStatus(String productId, BillingStatusListener statusHandler) {
        billingClientInstance.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                new PurchasesResponseListener() {
                    @Override
                    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {

                        if (list.size() == 0) handler.post(() -> statusHandler.onPurchaseStatus(null, null, null,false));
                        else {
                            for (Purchase p: list) {

                                String prodId;
                                String jsonData = p.getOriginalJson();
                                try {
                                    JSONObject purchase = new JSONObject(jsonData);
                                    prodId = purchase.getString("productId");
                                    if (p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                        if (prodId.equals(productId)) {
                                            handler.post(() -> statusHandler.onPurchaseStatus(prodId, p.getOriginalJson(), p.getOrderId(), true));
                                            break;
                                        }
                                    } else {
                                        if (prodId.equals(productId)) {
                                            handler.post(() -> statusHandler.onPurchaseStatus(prodId, p.getOriginalJson(), p.getOrderId(),false));
                                            break;
                                        }
                                    }
                                } catch (JSONException e) {
                                    handler.post(() -> statusHandler.onStatusError(BillConstants.UNABLE_TO_FIND_PROD_ID));
                                    break;
                                }

                            }
                        }
                    }
                });
    }

    public void getProductInfo(@NonNull String productId, @NonNull BillingProdInfoListener prodInfoListener) {
        if (!isConnected) {

            startConnection(new BillingConnectionListener() {
                @Override
                public void onConnectionStart(boolean start) {
                    utilsProductInfo(productId, prodInfoListener);
                }

                @Override
                public void onConnectionError(int errorCode) {
                    callError(errorCode);
                }
            });
        } else {

            utilsProductInfo(productId, prodInfoListener);
        }



    }

    private void utilsProductInfo(String productId, BillingProdInfoListener prodInfoListener) {
        if (billingClientInstance.isReady()) {
            List<QueryProductDetailsParams.Product> prodList = new ArrayList<>();
            prodList.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build());
            QueryProductDetailsParams queryProductDetailsParams =
                    QueryProductDetailsParams.newBuilder()
                            .setProductList(prodList)
                            .build();

            billingClientInstance.queryProductDetailsAsync(
                    queryProductDetailsParams,
                    (billingResult1, productDetailsList) -> {

                        for (ProductDetails prod : productDetailsList) {

                            ProductDetails.PricingPhase prodGetInfo = null;
                            if (prod.getSubscriptionOfferDetails() != null) {
                                prodGetInfo = prod.getSubscriptionOfferDetails().get(0)
                                        .getPricingPhases().getPricingPhaseList().get(0);
                            }
                            String formattedPrice;
                            if (prodGetInfo != null) {
                                formattedPrice = prodGetInfo.getFormattedPrice();
                            } else {
                                formattedPrice = null;
                            }
                            handler.post(() -> prodInfoListener.productInfo(prod.getDescription(), prod.getTitle(), formattedPrice));

                        }
                    });

        }
    }


    private void callPurchaseDone(int purchaseState, String orderId, boolean isAcknowledged, String originalJson) {
        handler.post(() -> eventHandler.onPurchaseDone(purchaseState, orderId, isAcknowledged, originalJson));
    }

    private void callPurchaseAckNow(boolean isAck) {
        handler.post(() -> eventHandler.onPurchaseAckNow(isAck));
    }

    private void callError(int errorCode) {
        handler.post(() -> eventHandler.onError(errorCode));
    }

//    private void releaseResource() {
//        if (billingClientInstance != null) {
//            billingClientInstance.endConnection();
//            isConnected = false;
//        }
//    }


}
