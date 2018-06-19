package com.example.renov.swipevoicechat.Fragment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.renov.swipevoicechat.Activity.MainActivity;
import com.example.renov.swipevoicechat.Activity.ShopActivity;
import com.example.renov.swipevoicechat.R;
import com.igaworks.IgawCommon;
import com.igaworks.adpopcorn.IgawAdpopcorn;
import com.onesignal.OneSignal;
import com.tapjoy.TJActionRequest;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJError;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.Tapjoy;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class SettingFragment extends Fragment implements TJPlacementListener, TJConnectListener {

    @BindView(R.id.btn_tapjoy)
    Button btnTapjoy;
    @BindView(R.id.iv_profile)
    ImageView profileImage;
    @BindView(R.id.tv_name)
    TextView NameText;
    @BindView(R.id.sw_reply_push)
    Switch swReplyPush;
    @BindView(R.id.sw_basic_push)
    Switch swBasicPush;

    public Unbinder unbinder;

    public static SettingFragment newInstance() {
        SettingFragment settingFragment = new SettingFragment();
        return settingFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Glide.with(getContext())
                .load(R.drawable.com_facebook_profile_picture_blank_square)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(profileImage);

//        Hashtable connectFlags = new Hashtable();
//        Tapjoy.connect(getContext(), "Ub_KBzFkRFypz8GBf_yYYAECuQQGr90J9QlVAlUQT9BTDC6N2rrty66AL9sq", connectFlags, this);
//        Tapjoy.setDebugEnabled(true);

        OneSignal.getTags(tags -> {
            if (tags != null){
                Log.d("onesignal", "get tag json: " + tags.toString());
                boolean isReplyable = tags.has("isReplyable");
                boolean isBasic = tags.has("isBasic");

                Log.d("onesignal", "isReplyable : " + (isReplyable ? "true" : "false") + "\nisBasic : " + (isBasic ? "true" : "false"));
                getActivity().runOnUiThread(() -> {
                    swReplyPush.setChecked(isReplyable);
                    swBasicPush.setChecked(isBasic);
                });
            }
        });

        swReplyPush.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                OneSignal.sendTag("isReplyable", "reply");
            else
                OneSignal.deleteTag("isReplyable");
        });

        swBasicPush.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                OneSignal.sendTag("isBasic", "true");
            else
                OneSignal.deleteTag("isBasic");
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.layout_profile)
    public void onClickProfile() {
        new MaterialDialog.Builder(getContext())
                .title("프로필 설정")
                .items(R.array.profile)
                .itemsCallback((dialog, view, which, text) -> Toast.makeText(getContext(), "click: " + text.toString(), Toast.LENGTH_SHORT).show())
                .show();
    }

    @OnClick(R.id.tv_shop)
    public void onClickGoToShop() {
        Intent intent = new Intent(getContext(), ShopActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_tapjoy)
    public void onClickTapjoy() {
        executeTapjoy();
    }

    @OnClick(R.id.btn_offerwall)
    public void onClickOfferWall() {
        executeOfferWall();
    }

    private void executeOfferWall() {
        IgawCommon.setUserId(getContext(), "bXlBY2NvdW50X25hbWU=");
        IgawAdpopcorn.openOfferWall(getActivity());
        IgawAdpopcorn.setSensorLandscapeEnable(getContext(), false);

    }

    private void executeTapjoy() {
        TJPlacementListener tjPlacementListener = this;
        TJPlacement tjPlacement = Tapjoy.getPlacement("AppLaunch", tjPlacementListener);

        if (Tapjoy.isConnected()) {
            tjPlacement.requestContent();
        } else {
            Toast.makeText(getContext(), "Tapjoy SDK must finish connecting before requesting content", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestSuccess(TJPlacement tjPlacement) {
//        Toast.makeText(getContext(), "onRequestSuccess", Toast.LENGTH_SHORT).show();
        if (tjPlacement.isContentReady()) {
            tjPlacement.showContent();
        } else {
            Log.d("onRequestSuccess", "handle situation where there is no content to show, or it has not yet download");
        }
    }

    @Override
    public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {
        Toast.makeText(getContext(), "onRequestFailure: " + tjError.message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onContentReady(TJPlacement tjPlacement) {
        Toast.makeText(getContext(), "onContentReady", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onContentShow(TJPlacement tjPlacement) {
        Toast.makeText(getContext(), "onContentShow", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onContentDismiss(TJPlacement tjPlacement) {
        Toast.makeText(getContext(), "onContentDismiss", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {
        Toast.makeText(getContext(), "onPurchaseRequest", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {
        Toast.makeText(getContext(), "onRewardRequest", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectSuccess() {
        Toast.makeText(getContext(), "onConnectSuccess", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectFailure() {
        Toast.makeText(getContext(), "onConnectFailure", Toast.LENGTH_SHORT).show();
    }
}
