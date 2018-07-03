package com.square.renov.swipevoicechat.Activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.vending.billing.IInAppBillingService;
import com.igaworks.IgawCommon;
import com.igaworks.adpopcorn.IgawAdpopcorn;
import com.nextapps.naswall.NASWall;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Network.network.HttpMethod;
import com.square.renov.swipevoicechat.Network.network.HttpRequestVO;
import com.square.renov.swipevoicechat.Network.network.HttpResponseCallback;
import com.square.renov.swipevoicechat.Network.network.ProgressHandler;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.tapjoy.TJActionRequest;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJError;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.Tapjoy;
import com.tapjoy.TapjoyConnectFlag;
import com.tapjoy.TapjoyLog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Hashtable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ShopActivity extends AppCompatActivity {
    private static final String TAG = ShopActivity.class.getSimpleName();
    private static final String TAG_TAPJOY = "TAPJOY";
    IInAppBillingService mService;

    @BindView(R.id.layout_package1)
    LinearLayout layoutPackage1;
    @BindView(R.id.free_shop1)
    RelativeLayout freeShop1;
    @BindView(R.id.free_shop2)
    RelativeLayout freeshop2;
    @BindView(R.id.free_shop3)
    RelativeLayout freeshop3;
    @BindView(R.id.my_luna_point_desc)
    TextView myLunaPointDesc;

    TJPlacement placement;

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

        myLunaPointDesc.setText(Html.fromHtml(myLunaPointDesc.getText().toString()));
        setPackageView();
        setInAppBill();
        setTapjoy();
        setAddPopcorn();
        setNas();
    }

    private void setNas() {
        //Nas
        Context context = this;
        boolean testMode = true;
        NASWall.init(context, testMode);
    }

    private void setAddPopcorn() {
        IgawAdpopcorn.setSensorLandscapeEnable(this, false);
    }

    private void setPackageView() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        Tapjoy.onActivityStart(this);
    }

    @Override
    protected void onResume() {
        NASWall.embedOnResume();
        super.onResume();
        IgawCommon.startSession(this);

    }

    @Override
    protected void onStop() {
        Tapjoy.onActivityStop(this);
        super.onStop();
        IgawCommon.endSession();
    }


    private void setInAppBill() {
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    private void setTapjoy() {
        freeShop1.setClickable(false);
        Hashtable<String, Object> connectFlags = new Hashtable<String, Object>();
        connectFlags.put(TapjoyConnectFlag.ENABLE_LOGGING, "true");

        Tapjoy.setGcmSender("820314113548");
        String tapJoySdkKey = "zUbX14o2TSGGz7uAOv_frgEC6kNrABkFe49ysWz84mheMCRZFvTEVV1srrIW";
        Tapjoy.connect(this, tapJoySdkKey, connectFlags, new TJConnectListener() {
            @Override
            public void onConnectSuccess() {
                Toast.makeText(ShopActivity.this, "connect success!", Toast.LENGTH_SHORT).show();
                Log.d(TAG_TAPJOY, "Tapjoy SDK must finish connecting before requesting content.");
                placement = Tapjoy.getPlacement("test", new TJPlacementListener() {
                    // Called when the SDK has made contact with Tapjoy's servers. It does not necessarily mean that any content is available.
                    @Override
                    public void onRequestSuccess(TJPlacement tjPlacement) {
                        Log.e(TAG_TAPJOY, "onRequestSuccess for placement " + tjPlacement.getName());

                        if (!tjPlacement.isContentAvailable()) {
                            Log.e(TAG_TAPJOY, "No Offerwall content available");
                        }
                    }

                    // Called when there was a problem during connecting Tapjoy servers.
                    @Override
                    public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {
                        Log.e(TAG_TAPJOY, "onRequestFailure: " + tjError.message);
                    }

                    // Called when the content is actually available to display.
                    @Override
                    public void onContentReady(TJPlacement tjPlacement) {
                        TapjoyLog.i(TAG_TAPJOY, "onContentReady for placement " + tjPlacement.getName());
                        tjPlacement.showContent();
                    }

                    // Called when the content is showed.
                    @Override
                    public void onContentShow(TJPlacement tjPlacement) {
                        TapjoyLog.i(TAG, "onContentShow for placement " + tjPlacement.getName());
                    }

                    // Called when the content is dismissed.
                    @Override
                    public void onContentDismiss(TJPlacement tjPlacement) {
                        TapjoyLog.i(TAG, "onContentDismiss for placement " + tjPlacement.getName());
                    }

                    @Override
                    public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {

                    }

                    @Override
                    public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {

                    }
                });
                placement.requestContent();
                freeShop1.setClickable(true);
            }

            @Override
            public void onConnectFailure() {
                Toast.makeText(ShopActivity.this, "connect failed!", Toast.LENGTH_SHORT).show();
                Log.e(TAG_TAPJOY, "Tapjoy connect Failed");
            }
        });
        //TODO : 배포전에 끄기
        Tapjoy.setDebugEnabled(true);


    }

    /**
     * TapJoy
     */
    @OnClick(R.id.free_shop1)
    public void onClickFreeShop1() {
        if(placement.isContentReady())
            placement.showContent();
    }

    /**
     * OfferWall Add Popcorn
     * */
    @OnClick(R.id.free_shop2)
    public void onClickFreeShop2(){
        IgawAdpopcorn.openOfferWall(this);
    }

    /**
     * Nas
     */
    @OnClick(R.id.free_shop3)
    public void onClickFreeShop3(){
        Activity activity = this;
        String userData = "USER_DATA";
        int age = 20; // 연령 (연령 정보가 없을 경우 0 으로 설정)
        NASWall.SEX sex = NASWall.SEX.SEX_MALE; // 성별 (SEX_UNKNOWN=성별정보없음, SEX_MALE=남자, SEX_FEMALE=여자)
        NASWall.open(activity, userData, age, sex);
    }

    @OnClick(R.id.layout_package1)
    public void onClickPackage1() {
        buyItem("luna_package_te`st");
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

                    Log.d(TAG, " ShopActivity getOriginalJson : " + originalJson);
                    Log.d(TAG, " ShopActivity getSignature : " + signature);

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
                Log.d(TAG, " ShopActivity error result code : " + resultCode);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mServiceConn != null)
            unbindService(mServiceConn);

        unbinder.unbind();
    }


}
