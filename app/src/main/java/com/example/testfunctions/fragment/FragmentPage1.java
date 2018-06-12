package com.example.testfunctions.fragment;

import android.app.Service;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.testfunctions.R;

public class FragmentPage1 extends Fragment implements View.OnClickListener {


    private MapView mapview;
    private BaiduMap bdMap;

    private LocationClient locationClient;
    private BDLocationListener locationListener;
    private BDNotifyListener notifyListener;

    private double longitude;// 精度
    private double latitude;// 维度
    private float radius;// 定位精度半径，单位是米
    private String addrStr;// 反地理编码
    private String province;// 省份信息
    private String city;// 城市信息
    private String district;// 区县信息
    private float direction;// 手机方向信息

    private int locType;

    // 定位按钮
    private Button locateBtn;
    // 定位模式 （普通-跟随-罗盘）
    private MyLocationConfiguration.LocationMode currentMode;
    // 定位图标描述
    private BitmapDescriptor currentMarker = null;
    // 记录是否第一次定位
    private boolean isFirstLoc = true;

    //振动器设备
    private Vibrator mVibrator;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SDKInitializer.initialize(getActivity().getApplicationContext());
        view = inflater.inflate(R.layout.fragment_1, container, false);

        mapview = (MapView)view.findViewById(R.id.bd_mapview);
        bdMap = mapview.getMap();
        locateBtn = (Button)view.findViewById(R.id.locate_btn);
        locateBtn.setOnClickListener(this);
        currentMode = MyLocationConfiguration.LocationMode.NORMAL;
        locateBtn.setText("普通");
        mVibrator =(Vibrator)getActivity().getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        init();
        return view;
    }

    private void init() {
        bdMap.setMyLocationEnabled(true);
        // 1. 初始化LocationClient类
        locationClient = new LocationClient(getActivity().getApplicationContext());
        // 2. 声明LocationListener类
        locationListener = new MyLocationListener();
        // 3. 注册监听函数
        locationClient.registerLocationListener(locationListener);
        // 4. 设置参数
        LocationClientOption locOption = new LocationClientOption();
        locOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        locOption.setCoorType("bd09ll");// 设置定位结果类型
        locOption.setScanSpan(5000);// 设置发起定位请求的间隔时间,ms
        locOption.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        locOption.setNeedDeviceDirect(true);// 设置返回结果包含手机的方向

        locationClient.setLocOption(locOption);
        // 5. 注册位置提醒监听事件
        notifyListener = new MyNotifyListener();
        notifyListener.SetNotifyLocation(longitude, latitude, 3000, "bd09ll");//精度，维度，范围，坐标类型
        locationClient.registerNotify(notifyListener);
        // 6. 开启/关闭 定位SDK
        locationClient.start();
        // locationClient.stop();
        // 发起定位，异步获取当前位置，因为是异步的，所以立即返回，不会引起阻塞
        // 定位的结果在ReceiveListener的方法onReceive方法的参数中返回。
        // 当定位SDK从定位依据判定，位置和上一次没发生变化，而且上一次定位结果可用时，则不会发生网络请求，而是返回上一次的定位结果。
        // 返回值，0：正常发起了定位 1：service没有启动 2：没有监听函数
        // 6：两次请求时间太短（前后两次请求定位时间间隔不能小于1000ms）
		/*
		 * if (locationClient != null && locationClient.isStarted()) {
		 * requestResult = locationClient.requestLocation(); } else {
		 * Log.d("LocSDK5", "locClient is null or not started"); }
		 */

    }

    //下面这个重载是从网上找到的，据说是避免因fragment跳转导致地图崩溃
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser == true) {
            // if this view is visible to user, start to request user location
            startRequestLocation();
        } else if (isVisibleToUser == false) {
            // if this view is not visible to user, stop to request user
            // location
            stopRequestLocation();
        }
    }

    private void stopRequestLocation() {
        if (locationClient != null) {
            locationClient.unRegisterLocationListener(locationListener);
            locationClient.stop();
        }
    }

    long startTime;
    long costTime;

    private void startRequestLocation() {
        // this nullpoint check is necessary
        if (locationClient != null) {
            locationClient.registerLocationListener(locationListener);
            locationClient.start();
            locationClient.requestLocation();
            startTime = System.currentTimeMillis();
        }
    }

    class MyLocationListener implements BDLocationListener {
        // 异步返回的定位结果
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                return;
            }
            locType = location.getLocType();
            //Toast.makeText(getActivity(), "当前定位的返回值是："+locType, Toast.LENGTH_SHORT).show();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            if (location.hasRadius()) {// 判断是否有定位精度半径
                radius = location.getRadius();
            }
            if (locType == BDLocation.TypeGpsLocation) {//
              /*  Toast.makeText(
                        getActivity(),
                        "当前速度是：" + location.getSpeed() + "~~定位使用卫星数量："
                                + location.getSatelliteNumber(),
                        Toast.LENGTH_SHORT).show();*/
            } else if (locType == BDLocation.TypeNetWorkLocation) {
                addrStr = location.getAddrStr();// 获取反地理编码(文字描述的地址)
             /*   Toast.makeText(getActivity(), addrStr,
                        Toast.LENGTH_SHORT).show();*/
            }
            direction = location.getDirection();// 获取手机方向，【0~360°】,手机上面正面朝北为0°
            province = location.getProvince();// 省份
            city = location.getCity();// 城市
            district = location.getDistrict();// 区县
           /* Toast.makeText(getActivity(),
                    province + "~" + city + "~" + district, Toast.LENGTH_SHORT)
                    .show();*/
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(radius)//
                    .direction(direction)// 方向
                    .latitude(latitude)//
                    .longitude(longitude)//
                    .build();
            // 设置定位数据
            bdMap.setMyLocationData(locData);
            LatLng ll = new LatLng(latitude, longitude);
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(ll);
            bdMap.animateMapStatus(msu);

        }
    }

    class MyNotifyListener extends BDNotifyListener {
        @Override
        public void onNotify(BDLocation bdLocation, float distance) {
            super.onNotify(bdLocation, distance);
            mVibrator.vibrate(1000);//振动提醒已到设定位置附近
            Toast.makeText(getActivity(), "震动提醒", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locate_btn:// 定位
                switch (currentMode) {
                    case NORMAL:
                        locateBtn.setText("跟随");
                        currentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                        break;
                    case FOLLOWING:
                        locateBtn.setText("罗盘");
                        currentMode = MyLocationConfiguration.LocationMode.COMPASS;
                        break;
                    case COMPASS:
                        locateBtn.setText("普通");
                        currentMode = MyLocationConfiguration.LocationMode.NORMAL;
                        break;
                }
                bdMap.setMyLocationConfigeration(new MyLocationConfiguration(
                        currentMode, true, currentMarker));
                break;
        }
    }

}
