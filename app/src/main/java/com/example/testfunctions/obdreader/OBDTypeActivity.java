package com.example.testfunctions.obdreader;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.example.testfunctions.R;
import com.example.testfunctions.cardview.CardFragmentPagerAdapter;
import com.example.testfunctions.cardview.ShadowTransformer;

public class OBDTypeActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private CardFragmentPagerAdapter mFragmentCardAdapter;
    private ShadowTransformer mFragmentCardShadowTransformer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obdtype);
        mViewPager = (ViewPager)findViewById(R.id.viewPager);

        mFragmentCardAdapter = new CardFragmentPagerAdapter(getSupportFragmentManager(),
                dpToPixels(2, this));

        mFragmentCardShadowTransformer = new ShadowTransformer(mViewPager, mFragmentCardAdapter);
        mFragmentCardShadowTransformer.enableScaling(true);

        mViewPager.setAdapter(mFragmentCardAdapter);
        mViewPager.setPageTransformer(false, mFragmentCardShadowTransformer);
        mViewPager.setOffscreenPageLimit(3);//viewpager每次切换的时候， 会重新创建当前界面及左右界面三个界面， 每次切换都要重新oncreate,设置3表示三个界面之间来回切换都不会重新加载
    }

    public static float dpToPixels(int dp, Context context) {
        //根据手机屏幕参数，后面的density就是屏幕的密度，类似分辨率
        return dp * (context.getResources().getDisplayMetrics().density);
    }
}
