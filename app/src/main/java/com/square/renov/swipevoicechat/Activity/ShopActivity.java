package com.square.renov.swipevoicechat.Activity;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.vending.billing.IInAppBillingService;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Network.network.HttpMethod;
import com.square.renov.swipevoicechat.Network.network.HttpRequestVO;
import com.square.renov.swipevoicechat.Network.network.HttpResponseCallback;
import com.square.renov.swipevoicechat.Network.network.ProgressHandler;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ShopActivity extends AppCompatActivity {
    private static final String TAG = ShopActivity.class.getSimpleName();
    IInAppBillingService mService;

    @BindView(R.id.layout_package1)
    LinearLayout layoutPackage1;

    Unbinder unbinder;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        unbinder = ButterKnife.bind(this);

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    @OnClick(R.id.layout_package1)
    public void onClickPackage1(){
        buyItem("luna_package_test");
    }

    private void buyItem(String productId) {
        try {
            AlreadyPurchaseItems();

            Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), productId, "inapp", "develop_payload");
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

            if (pendingIntent != null) {
                startIntentSenderForResult(pendingIntent.getIntentSender(),
                        1001, new Intent(), 0, 0,
                        0);
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void AlreadyPurchaseItems() {
        try {
            Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {

                ArrayList purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                String[] tokens = new String[purchaseDataList.size()];
                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = (String) purchaseDataList.get(i);
                    JSONObject jo = new JSONObject(purchaseData);
                    tokens[i] = jo.getString("purchaseToken");
                    // 여기서 tokens를 모두 컨슘 해주기
                    mService.consumePurchase(3, getPackageName(), tokens[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
//            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String originalJson = data.getStringExtra("INAPP_PURCHASE_DATA");
            String signature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                if (originalJson != null) {

                    Log.d(TAG," ShopActivity getOriginalJson : " + originalJson);
                    Log.d(TAG," ShopActivity getSignature : " + signature);

                    List<NameValuePair> paramInfo = new ArrayList<>();
                    paramInfo.add(new BasicNameValuePair("signature", signature));
                    paramInfo.add(new BasicNameValuePair("originalJson", originalJson));

//                    TODO: 영수증 전달
//                    HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.PAYMENT_URL, User.class, paramInfo, HttpMethod.PUT, getApplicationContext());
//                    RequestFactory requestFactory = new RequestFactory();
//                    requestFactory.setProgressHandler(new ProgressHandler(ShopActivity.this, false));
//                    requestFactory.create(httpRequestVO, new HttpResponseCallback<User>() {
//                        @Override
//                        public void onResponse(User user) {
//                            if (user != null) {
//                                setPointText(user);
//                                SharedPrefHelper.getInstance(ShopActivity.this).setSharedPreferences(SharedPrefHelper.POINT, user.getPoint());
//                            }
//                        }
//                    }).execute();
//
//                    String UserId = SharedPrefHelper.getInstance(this).getSharedPreferences(SharedPrefHelper.USER_ID, "error");
//
//                    if (UserId != null && productID != null && productName != null && price != 0) {
//                        IgawAdbrix.purchase(this,
//                                UserId,
//                                IgawCommerceProductModel.create(
//                                        productID,
//                                        productName,
//                                        price,
//                                        0.0,
//                                        1,
//                                        IgawCommerce.Currency.KR_KRW,
//                                        null,
//                                        null),
//                                IgawCommerce.IgawPaymentMethod.MobilePayment);
//
//                        Answers.getInstance().logPurchase(new PurchaseEvent()
//                                .putItemPrice(BigDecimal.valueOf(price))
//                                .putCurrency(Currency.getInstance("KRW"))
//                                .putItemName(productName)
//                                .putItemType("inApp")
//                                .putItemId(productID)
//                                .putSuccess(true));
//                    }

                }
            } else {
                Log.d(TAG," ShopActivity error result code : " + resultCode);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mServiceConn != null)
            unbindService(mServiceConn);

        unbinder.unbind();
    }
}
