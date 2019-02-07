/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.slonigiraf.homoksafe.billing;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class BillingManager implements PurchasesUpdatedListener {
    private final BillingClient mBillingClient;
    private final Activity mActivity;
    private Purchase.PurchasesResult purchasesResult;

    public BillingManager(Activity activity) {
        mActivity = activity;
        mBillingClient = BillingClient.newBuilder(mActivity).setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponse) {
                if (billingResponse == BillingClient.BillingResponse.OK) {
                    loadPurchases();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
            }
        });
    }

    private void loadPurchases(){
        purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
    }

    public boolean isPurchased(final String skuId){
        boolean isPurchased = false;
        if(purchasesResult != null){
            for(Purchase purchase : purchasesResult.getPurchasesList()){
                if(purchase.getSku().equals(skuId)){
                    isPurchased = true;
                    break;
                }
            }
        }
        return isPurchased;
    }

    public void startPurchaseFlow(final String skuId, final String billingType) {
        // Specify a runnable to start when connection to Billing client is established
        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setType(billingType)
                        .setSku(skuId)
                        .build();
                mBillingClient.launchBillingFlow(mActivity, billingFlowParams);
            }
        };

        // If Billing client was disconnected, we retry 1 time
        // and if success, execute the query
        startServiceConnectionIfNeeded(executeOnConnectedService);
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        loadPurchases();
    }


    private void startServiceConnectionIfNeeded(final Runnable executeOnSuccess) {
        if (mBillingClient.isReady()) {
            if (executeOnSuccess != null) {
                executeOnSuccess.run();
            }
        } else {
            mBillingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponse) {
                    if (billingResponse == BillingClient.BillingResponse.OK) {
                        if (executeOnSuccess != null) {
                            executeOnSuccess.run();
                        }
                    }
                }
                @Override
                public void onBillingServiceDisconnected() {
                }
            });
        }
    }


    public void destroy() {
        mBillingClient.endConnection();
    }

    //just for testing purposes
    @SuppressWarnings("unused")
    public void consume(final String skuId) {
        if(purchasesResult != null && purchasesResult.getPurchasesList().size() > 0){
            try {
                JSONObject o = new JSONObject(purchasesResult.getPurchasesList().get(0).getOriginalJson());
                String purchaseToken = o.optString("token", o.optString("purchaseToken"));
                mBillingClient.consumeAsync(purchaseToken, new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(int responseCode, String purchaseToken) {
                        System.out.println("CONSUMED! "+responseCode);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
