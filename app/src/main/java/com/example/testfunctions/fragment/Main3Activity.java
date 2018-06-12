package com.example.testfunctions.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testfunctions.R;
import com.example.testfunctions.fragment.FragmentPage1;
import com.example.testfunctions.fragment.FragmentPage2;
import com.example.testfunctions.fragment.FragmentPage3;
import com.example.testfunctions.fragment.FragmentPage4;
import com.example.testfunctions.fragment.FragmentPage5;
import com.example.testfunctions.login.LoginActivity;
import com.example.testfunctions.obdreader.DeviceListActivity;
import com.example.testfunctions.obdreader.MyService;
import com.example.testfunctions.obdreader.OBDTypeActivity;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

/**
 * @author yangyu
 *  功能描述：自定义TabHost
 */
public class Main3Activity extends FragmentActivity{
    //定义FragmentTabHost对象
    private FragmentTabHost mTabHost;

    //定义一个布局
    private LayoutInflater layoutInflater;

    //定义数组来存放Fragment界面
    private Class fragmentArray[] = {FragmentPage1.class,FragmentPage2.class,FragmentPage3.class,FragmentPage4.class,FragmentPage5.class};

    //定义数组来存放按钮图片
    private int mImageViewArray[] = {R.drawable.tab_home_btn,R.drawable.tab_message_btn,R.drawable.tab_selfinfo_btn,
            R.drawable.tab_square_btn,R.drawable.tab_more_btn};

    //Tab选项卡的文字
    private String mTextviewArray[] = {"首页", "消息", "好友", "定位", "更多"};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        initView();
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        tb.setTitle(getResources().getString(R.string.app_name));
        tb.setTitleTextColor(Color.parseColor("#FFFFFF"));

        new DrawerBuilder()
                .withActivity(this)
                .withHeader(R.layout.util_drawer_hdr)//左边菜单的上面的背景
                .withToolbar(tb)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("主页").withIcon(R.drawable.home),
                        new PrimaryDrawerItem().withName("登录").withIcon(R.drawable.login).withIdentifier(1),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("连接").withIcon(R.drawable.connect).withIdentifier(2),
                        new PrimaryDrawerItem().withName("自定义仪表盘").withIcon(R.drawable.custom).withIdentifier(3),
                        new PrimaryDrawerItem().withName("开始获取数据").withIcon(R.drawable.begin).withIdentifier(4),
                        new PrimaryDrawerItem().withName("上传数据").withIcon(R.drawable.upload),
                        new PrimaryDrawerItem().withName("停止获取数据").withIcon(R.drawable.stop),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("帮助").withIcon(R.drawable.help),
                        new PrimaryDrawerItem().withName("设置").withIcon(R.drawable.setting)

                        //new SecondaryDrawerItem().withName("skydianshi").withIcon(R.drawable.home),
                        /*new SecondarySwitchDrawerItem(),
                        new SecondaryToggleDrawerItem(),
                        new SecondaryDrawerItem(),
                        new SectionDrawerItem(),
                        new SwitchDrawerItem(),*/
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem != null) {
                            Fragment fragment = null;
                            FragmentManager fragmentManager = getFragmentManager();

                            switch ((int) drawerItem.getIdentifier()) {
                                case 1:
                                    Intent loginIntent = new Intent(Main3Activity.this, LoginActivity.class);
                                    startActivity(loginIntent);
                                    // fragment = new MainTabsFragment();
                                    break;
                                case 2:
                                    //fragment = new PermissionTabsFragment();

                                    break;
                                case 3:
                                    Intent getOBDIntent = new Intent(Main3Activity.this, OBDTypeActivity.class);
                                    startActivity(getOBDIntent);
                            }

                          /*  if (fragment != null) {
                                fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
                            }

                            if (drawerItem instanceof Nameable) {
                                setTitle(((Nameable) drawerItem).getName().getText(getApplicationContext()));
                            }*/
                        }

                        return false;
                    }
                })
                .withShowDrawerOnFirstLaunch(false)
                .withFireOnInitialOnClick(true)
                .withSavedInstance(savedInstanceState)
                .build();
    }

    /**
     * 初始化组件
     */
    private void initView(){
        //实例化布局对象
        layoutInflater = LayoutInflater.from(this);

        //实例化TabHost对象，得到TabHost
        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        //得到fragment的个数
        int count = fragmentArray.length;

        for(int i = 0; i < count; i++){
            //为每一个Tab按钮设置图标、文字和内容
            TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
            //将Tab按钮添加进Tab选项卡中
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
            //设置Tab按钮的背景
            mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
        }
    }

    /**
     * 给Tab按钮设置图标和文字
     */
    private View getTabItemView(int index){
        View view = layoutInflater.inflate(R.layout.tab_item_view, null);

        ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
        imageView.setImageResource(mImageViewArray[index]);

        TextView textView = (TextView) view.findViewById(R.id.textview);
        textView.setText(mTextviewArray[index]);

        return view;
    }

}
