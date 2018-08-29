package com.square.renov.swipevoicechat.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.AdbrixUtil;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.relex.circleindicator.CircleIndicator;

public class TutorialActivity extends AppCompatActivity {
    private static final String TAG = TutorialActivity.class.getSimpleName();
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.indicator)
    CircleIndicator indicator;
    @BindView(R.id.tv_bottom_button)
    TextView bottomButton;

    TutorialPagerAdapter adapter;

    Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        unbinder = ButterKnife.bind(this);

        adapter = new TutorialPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(adapter);
        indicator.setViewPager(viewPager);

        AdbrixUtil.setFirstTimeExperience(this, SharedPrefHelper.TUTORIAL);
    }

    @OnClick(R.id.tv_bottom_button)
    public void onClickBottomButton() {
        int position = viewPager.getCurrentItem();
        Log.e(TAG, "position:" + position);
        if (position == 2) {
            Intent intent = new Intent(this, LogInActivity.class);
            startActivity(intent);
            finish();
        } else {
            viewPager.setCurrentItem(position + 1);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private class TutorialPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();

        public TutorialPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments.add(TutorialFragment.newInstance(R.drawable.tutorial_1, "목소리로 털어놓는 \n당신의 가장 솔직한 이야기", "나를 알아주는 \n위로의 속삭임"));
            fragments.add(TutorialFragment.newInstance(R.drawable.tutorial_2, "아무도 나를 모르는 이곳에서 \n조금 더 솔직하게 얘기해보세요", "나를 알아주는 \n위로의 속삭임"));
            fragments.add(TutorialFragment.newInstance(R.drawable.tutorial_3, "나를 모르는 누군가에게\n당신의 이야기를 들려주세요", "나를 알아주는 \n위로의 속삭임"));

        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    public static class TutorialFragment extends Fragment {
        @BindView(R.id.tv_desc)
        TextView tvDesc;
        @BindView(R.id.iv_tutorial)
        ImageView Tutorial;
        @BindView(R.id.tv_title)
        TextView tvTitle;

        Unbinder unbinder;
        private static final String ARG_TITLE = "title";
        private static final String ARG_IMAGE = "image";
        private static final String ARG_DESC = "desc";

        public TutorialFragment() {

        }

        public static TutorialFragment newInstance(int imageId, String description, String title) {
            TutorialFragment fragment = new TutorialFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_IMAGE, imageId);
            args.putString(ARG_DESC, description);
            args.putString(ARG_TITLE, title);
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.item_pager_tutorial, container, false);
            unbinder = ButterKnife.bind(this, rootView);
            tvDesc.setText(getArguments().getString(ARG_DESC));
            Tutorial.setImageResource(getArguments().getInt(ARG_IMAGE));
            tvTitle.setText(getArguments().getString(ARG_TITLE));
            return rootView;
        }
    }
}
