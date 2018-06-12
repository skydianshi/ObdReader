package com.example.testfunctions.fragment;


import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testfunctions.CompassView;
import com.example.testfunctions.R;
import com.example.testfunctions.RoundProgressBar;
import com.example.testfunctions.obdreader.DeviceListActivity;
import com.example.testfunctions.obdreader.IPostListener;
import com.example.testfunctions.obdreader.Main2Activity;
import com.example.testfunctions.obdreader.MyService;
import com.example.testfunctions.obdreader.ObdCommand;

public class FragmentPage2 extends Fragment{

    View view;
    private Button updateButton ;
    private MyService.MyBinder binder;
    private Button connectBTButton;
    private Handler handler;
    private Handler addJobHandler;
    private IPostListener callback;
    private Intent startServiceIntent;
    private String address;
    private ObdCommand command;
    private String message;

    private CompassView speedPointer;
    private RoundProgressBar speedRoundProgressBar;
    private TextView speed_show;
    private CompassView RPMPointer;
    private RoundProgressBar RPMRoundProgressBar;
    private TextView RPMshow;

    private BluetoothDevice _device = null;     //蓝牙设备
    private BluetoothSocket _socket = null;      //蓝牙通信socket
    private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号

    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();  //获取本地蓝牙适配器，即蓝牙设备

    private ServiceConnection conn = new ServiceConnection() {
        //当该activity与service连接成功时调用此方法
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            System.out.println("service connected");
            //获取service中返回的Mybind对象
            binder = (MyService.MyBinder)iBinder;
        }
        //断开连接时调用此方法
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("disconnected");
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_2, container, false);
        return  view;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
                // 响应返回结果
                if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
                    // MAC地址，由DeviceListActivity设置返回
                    address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    System.out.println("MAC address-------->"+address);
                    // 得到蓝牙设备句柄
                    _device = _bluetooth.getRemoteDevice(address);

                    Button btn = (Button) view.findViewById(R.id.connectButton);
                    Toast.makeText(getActivity(), "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
                    //btn.setText("已连接");
                    //btn.setEnabled(false);

                    //启动service,在这边启动时因为启动service需要时间，放在这里用户再按更新数据中间留有的时间足够初始化了，不然连续按会出错，service来不及初始化，binder对象未得到
                    startServiceIntent = new Intent(getActivity(),MyService.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("address",address);//这边一定要先连接蓝牙再传递数据，不然传过去的是空值
                    startServiceIntent.putExtras(bundle);
                    getActivity().bindService(startServiceIntent,conn, Service.BIND_AUTO_CREATE);
//这边要对service进行判断，判断其是否正在运行，如果正在运行则不操作，这里可以借用demo中的方法，也可以用一个bool变量进行记录
                    getActivity().startService(startServiceIntent);
                }
                else {
                    Toast.makeText(getActivity(),"蓝牙连接失败，请重试！",Toast.LENGTH_SHORT);
                }
                break;
            default:break;
        }
    }




    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateButton = (Button)view.findViewById(R.id.button2);
        connectBTButton = (Button)view.findViewById(R.id.connectButton);

        speedPointer = (CompassView)view.findViewById(R.id.speed_compass_pointer);
        speedRoundProgressBar = (RoundProgressBar)view.findViewById(R.id.speed_roundProgressBar1);
        speed_show = (TextView)view.findViewById(R.id.speedshow);
        speedRoundProgressBar.setRoundWidth(30);
        speedPointer.updateDirection(0);
        RPMPointer = (CompassView)view.findViewById(R.id.RPM_compass_pointer);
        RPMRoundProgressBar = (RoundProgressBar)view.findViewById(R.id.RPM_roundProgressBar1);
        RPMshow = (TextView)view.findViewById(R.id.RPMshow);
        RPMRoundProgressBar.setRoundWidth(30);
        RPMPointer.updateDirection(0);

        //如果打开本地蓝牙设备不成功，提示信息，结束程序
        if (_bluetooth == null){
            Toast.makeText(getActivity(), "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            getActivity().finish();
            return;
        }
        //打开蓝牙
        new Thread(){
            public void run(){
                if(_bluetooth.isEnabled()==false){
                    _bluetooth.enable();
                }

            }
        }.start();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binder.setListener(callback);//将service中的接口与这里的接口绑定起来·
                addJobHandler = new Handler();
                addJobHandler.post(addJobRunnable);
            }
        });

        connectBTButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_bluetooth.isEnabled()==false){  //如果蓝牙服务不可用则提示
                    Toast.makeText(getActivity(), " 打开蓝牙中...", Toast.LENGTH_LONG).show();
                    return;
                }
                if(_socket==null){
                    Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class); //跳转程序设置
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
                }
                else {
                    Toast.makeText(getActivity(),"socket连接出错，请重新连接或重启程序！",Toast.LENGTH_SHORT);
                }
                return;
            }
        });



        handler = new Handler();

//实现接口中的方法用来回调
        callback = new IPostListener() {
            @Override
            public void stateUpdate(ObdCommand obdCommand,String s) {
                message = s.toString();
                command = obdCommand;
                handler.post(update);
            }
        };
    }


    Runnable update = new Runnable() {
        @Override
        public void run() {
            TextView t;
            int index = command.getIndex();
            switch (index){
                case 0:
                    System.out.println("收到数据错误或未收到数据");
                case 1:
                    t = (TextView)view.findViewById(R.id.RPM);
                    t.setText("发动机转速："+message+" rpm");
                    float rpmDirection = (float)(Float.parseFloat(message)/1.5);//通过速度计算度数
                    RPMPointer.updateDirection(rpmDirection);
                    RPMshow.setText(message+"rpm");
                    RPMRoundProgressBar.setProgress(Integer.parseInt(message));
                    break;
                case 2:
                    t = (TextView)view.findViewById(R.id.airFlowTextView);
                    t.setText("质量空气流量： "+ message);
                    break;
                case 3:
                    t = (TextView)view.findViewById(R.id.MAPTextView);
                    t.setText("进气歧管压力： "+ message);
                    break;
                case 4:
                    t = (TextView)view.findViewById(R.id.ECTTextView);
                    t.setText("发动机冷却液温度： "+ message);
                    break;
                case 5:
                    t = (TextView)view.findViewById(R.id.speedTextView);
                    t.setText("速度： "+ message);
                    float speedDirection = (float)(Float.parseFloat(message)*1.5);//通过速度计算度数
                    speedPointer.updateDirection(speedDirection);
                    speed_show.setText(message+"km/h");
                    speedRoundProgressBar.setProgress(Integer.parseInt(message));
                    break;
            }
        }
    };


    Runnable addJobRunnable = new Runnable() {
        @Override
        public void run() {
            binder.addJob();
            addJobHandler.postDelayed(this,10);
        }
    };
}