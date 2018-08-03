package com.square.renov.swipevoicechat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.JsonObject;
import com.igaworks.IgawCommon;
import com.igaworks.adpopcorn.IgawAdpopcorn;
import com.igaworks.adpopcorn.IgawAdpopcornExtension;
import com.igaworks.adpopcorn.cores.model.APClientRewardItem;
import com.igaworks.adpopcorn.interfaces.IAPClientRewardCallbackListener;
import com.nextapps.naswall.NASWall;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.square.renov.swipevoicechat.Util.Utils;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopActivity extends AppCompatActivity {
    private static final String TAG = ShopActivity.class.getSimpleName();
    private static final String TAG_TAPJOY = "TAPJOY";
    IInAppBillingService mService;

    // Const
    private static final int PERMISSION_REQUEST_CODE = 8100;

    @BindView(R.id.layout_package1)
    LinearLayout layoutPackage1;
    @BindView(R.id.free_shop1)
    RelativeLayout freeShop1;
    @BindView(R.id.free_shop2)
    RelativeLayout freeShop2;
    @BindView(R.id.free_shop3)
    RelativeLayout freeShop3;
    @BindView(R.id.my_luna_point_desc)
    TextView myLunaPointDesc;
    @BindView(R.id.tv_title_bar)
    TextView titleBar;

    TJPlacement placement;

    Unbinder unbinder;

    User myInfo;

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

        myInfo = SharedPrefHelper.getInstance(this).getUserInfo();

        myLunaPointDesc.setText(Html.fromHtml(getResources().getString(R.string.shop_luna_desc, myInfo.getLuna())));

        titleBar.setText("루나 상점");
        checkPermissions();
        setInAppBill();

//        setTapjoy();
        setAdPopcorn();
        setNas();
    }

    @OnClick(R.id.tv_point_list)
    public void onClickPointList() {
        Intent intent = new Intent(this, PointLogActivity.class);
        startActivity(intent);
    }

    @OnClick({R.id.pay_error_layout, R.id.pay_error_button})
    public void onClickPayError() {
        User me = SharedPrefHelper.getInstance(this).getUserInfo();
        Uri uri = Uri.parse("mailto:help.sori@formationsquare.com");
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.do_qna));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_content_qna, me.getName()));
        startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionList = new ArrayList<String>();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.INTERNET);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.READ_PHONE_STATE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.ACCESS_WIFI_STATE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.GET_ACCOUNTS);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.RECEIVE_BOOT_COMPLETED);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionList.size() > 0) {
                String[] permissions = new String[permissionList.size()];
                permissions = permissionList.toArray(permissions);
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            } else {
                Toast.makeText(this, "권한 요청 완료", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "권한 요청 완료", Toast.LENGTH_SHORT).show();
        }
    }

    private void setNas() {
        //Nas
        boolean testMode = false;
        String userid = String.valueOf(myInfo.getId());
//        NASWall.init(context, testMode, userid);
        NASWall.init(this, testMode);
        NASWall.setOnCloseListener(() -> {
            Toast.makeText(this, "NASWall - closed", Toast.LENGTH_SHORT).show();
        });

        NASWall.setIsUseChargeLockScreen(false);
    }

    private void setAdPopcorn() {
        //TODO : 유저 식별값 입력
        IgawCommon.setUserId(getApplicationContext(), String.valueOf(myInfo.getId()));
        IgawAdpopcorn.setSensorLandscapeEnable(this, false);
    }

//    private void setTapjoy() {
//        Hashtable<String, Object> connectFlags = new Hashtable<String, Object>();
//        connectFlags.put(TapjoyConnectFlag.ENABLE_LOGGING, "true");
//
////        Tapjoy.setGcmSender("820314113548");
//        String tapJoySdkKey = "zUbX14o2TSGGz7uAOv_frgEC6kNrABkFe49ysWz84mheMCRZFvTEVV1srrIW";
//
//        Tapjoy.connect(this, tapJoySdkKey, connectFlags, new TJConnectListener() {
//            @Override
//            public void onConnectSuccess() {
//                Toast.makeText(ShopActivity.this, "connect success!", Toast.LENGTH_SHORT).show();
//                TapjoyLog.d(TAG_TAPJOY, "Tapjoy SDK must finish connecting before requesting content.");
//                Tapjoy.setUserID(String.valueOf(myInfo.getId()));
//                placement = Tapjoy.getPlacement("test", new TJPlacementListener() {
//                    // Called when the SDK has made contact with Tapjoy's servers. It does not necessarily mean that any content is available.
//                    @Override
//                    public void onRequestSuccess(TJPlacement tjPlacement) {
//                        TapjoyLog.e(TAG_TAPJOY, "onRequestSuccess for placement " + tjPlacement.getName());
//                        if (!tjPlacement.isContentAvailable()) {
//                            TapjoyLog.e(TAG_TAPJOY, "No Offerwall content available");
//                        }
//                    }
//
//                    // Called when there was a problem during connecting Tapjoy servers.
//                    @Override
//                    public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {
//                        TapjoyLog.e(TAG_TAPJOY, "onRequestFailure: " + tjError.message);
//                    }
//
//                    // Called when the content is actually available to display.
//                    @Override
//                    public void onContentReady(TJPlacement tjPlacement) {
//                        tjPlacement.showContent();
//                        TapjoyLog.i(TAG_TAPJOY, "onContentReady for placement " + tjPlacement.getName());
//                    }
//
//                    // Called when the content is showed.
//                    @Override
//                    public void onContentShow(TJPlacement tjPlacement) {
//                        TapjoyLog.i(TAG_TAPJOY, "onContentShow for placement " + tjPlacement.getName());
//                    }
//
//                    // Called when the content is dismissed.
//                    @Override
//                    public void onContentDismiss(TJPlacement tjPlacement) {
//                        TapjoyLog.i(TAG_TAPJOY, "onContentDismiss for placement " + tjPlacement.getName());
//                    }
//
//                    @Override
//                    public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {
//
//                    }
//
//                    @Override
//                    public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {
//
//                    }
//                });
//            }
//
//            @Override
//            public void onConnectFailure() {
//                Toast.makeText(ShopActivity.this, "connect failed!", Toast.LENGTH_SHORT).show();
//                Log.e(TAG_TAPJOY, "Tapjoy connect Failed");
//            }
//        });
//        //TODO : 배포전에 끄기
//        Tapjoy.setDebugEnabled(true);
//    }

    @Override
    protected void onStart() {
        super.onStart();
        Tapjoy.onActivityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IgawCommon.startSession(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        IgawCommon.endSession();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Tapjoy.onActivityStop(this);
    }


    private void setInAppBill() {
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }


    @OnClick(R.id.iv_back)
    public void onClickBack() {
        finish();
    }

    /**
     * TapJoy
     */
    @OnClick(R.id.free_shop1)
    public void onClickFreeShop1() {
//        placement.requestContent();
        Toast.makeText(this, "오류 수정중입니다.", Toast.LENGTH_SHORT).show();
//        if (placement.isContentReady())
//            placement.showContent();
//        else
//            Log.e(TAG, "placement is not ready");
    }

    /**
     * OfferWall Add Popcorn
     */
    @OnClick(R.id.free_shop2)
    public void onClickFreeShop2() {
        IgawAdpopcorn.openOfferWall(this);
    }

    /**
     * Nas
     */
    @OnClick(R.id.free_shop3)
    public void onClickFreeShop3() {
        String userData = String.valueOf(myInfo.getId());
//        int age = 20; // 연령 (연령 정보가 없을 경우 0 으로 설정)
//        NASWall.SEX sex = NASWall.SEX.SEX_MALE; // 성별 (SEX_UNKNOWN=성별정보없음, SEX_MALE=남자, SEX_FEMALE=여자)
//        NASWall.open(activity, userData, age, sex);
        NASWall.open(this, userData);
    }

    @OnClick({R.id.layout_package1, R.id.luna_package1, R.id.layout_package2, R.id.luna_package2, R.id.layout_package3, R.id.luna_package3, R.id.layout_package4, R.id.luna_package4, R.id.layout_package5, R.id.luna_package5})
    public void onClickPackage1(View view) {
        int id = view.getId();

        String packageName = "";
        if (id == R.id.layout_package1 || id == R.id.luna_package1)
            packageName = "luna_package_1";
        else if (id == R.id.layout_package2 || id == R.id.luna_package2)
            packageName = "luna_package_2";
        else if (id == R.id.layout_package3 || id == R.id.luna_package3)
            packageName = "luna_package_3";
        else if (id == R.id.layout_package4 || id == R.id.luna_package4)
            packageName = "luna_package_4";
        else if (id == R.id.layout_package5 || id == R.id.luna_package5)
            packageName = "luna_package_5";
        else
            packageName = "luna_package_test";

        buyItem(packageName);
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
                    Call<JsonObject> request = NetRetrofit.getInstance(this).getService().payInAppProduct(originalJson, signature);
                    request.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if(response.isSuccessful()){
                                JsonObject pointJsonObject = response.body();
                                int point = pointJsonObject.get("point").getAsInt();
                                //TODO: 포인트 업데이트 방식 어떻게??
                            } else {
                                try {
                                    Utils.toastError(getApplicationContext(), response);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            } else {
                Log.d(TAG, " ShopActivity error result code : " + resultCode);
            }
        }
    }

    @Override
    @SuppressLint("NewApi")
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int i = 0; i < permissions.length; ++i) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                }
                if (allGranted) {
                    Toast.makeText(this, "all granted", Toast.LENGTH_SHORT).show();
                    //TODO init nas
                } else {
                    Toast.makeText(this, "not all granted", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
