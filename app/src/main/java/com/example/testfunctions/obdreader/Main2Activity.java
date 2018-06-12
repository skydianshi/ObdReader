package com.example.testfunctions.obdreader;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testfunctions.CompassView;
import com.example.testfunctions.R;
import com.example.testfunctions.RoundProgressBar;

public class Main2Activity extends AppCompatActivity {
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

                    Button btn = (Button) findViewById(R.id.connectButton);
                    Toast.makeText(this, "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
                    btn.setText("已连接");
                    btn.setEnabled(false);

                    //启动service,在这边启动时因为启动service需要时间，放在这里用户再按更新数据中间留有的时间足够初始化了，不然连续按会出错，service来不及初始化，binder对象未得到
                    startServiceIntent = new Intent(this,MyService.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("address",address);//这边一定要先连接蓝牙再传递数据，不然传过去的是空值
                    startServiceIntent.putExtras(bundle);
                    bindService(startServiceIntent,conn, Service.BIND_AUTO_CREATE);
//这边要对service进行判断，判断其是否正在运行，如果正在运行则不操作，这里可以借用demo中的方法，也可以用一个bool变量进行记录
                    startService(startServiceIntent);
                }
                else {
                    Toast.makeText(Main2Activity.this,"蓝牙连接失败，请重试！",Toast.LENGTH_SHORT);
                }
                break;
            default:break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        updateButton = (Button)findViewById(R.id.button2);
        connectBTButton = (Button)findViewById(R.id.connectButton);

        speedPointer = (CompassView)findViewById(R.id.speed_compass_pointer);
        speedRoundProgressBar = (RoundProgressBar)findViewById(R.id.speed_roundProgressBar1);
        speed_show = (TextView)findViewById(R.id.speedshow);
        speedRoundProgressBar.setRoundWidth(30);
        speedPointer.updateDirection(0);
        RPMPointer = (CompassView)findViewById(R.id.RPM_compass_pointer);
        RPMRoundProgressBar = (RoundProgressBar)findViewById(R.id.RPM_roundProgressBar1);
        RPMshow = (TextView)findViewById(R.id.RPMshow);
        RPMRoundProgressBar.setRoundWidth(30);
        RPMPointer.updateDirection(0);

        //如果打开本地蓝牙设备不成功，提示信息，结束程序
        if (_bluetooth == null){
            Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            finish();
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
                    Toast.makeText(Main2Activity.this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
                    return;
                }
                if(_socket==null){
                    Intent serverIntent = new Intent(Main2Activity.this, DeviceListActivity.class); //跳转程序设置
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
                }
                else {
                    Toast.makeText(Main2Activity.this,"socket连接出错，请重新连接或重启程序！",Toast.LENGTH_SHORT);
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
                    t = (TextView)findViewById(R.id.RPM);
                    t.setText("发动机转速："+message+" rpm");
                    float rpmDirection = (float)(Float.parseFloat(message)/1.5);//通过速度计算度数
                    RPMPointer.updateDirection(rpmDirection);
                    RPMshow.setText(message+"rpm");
                    RPMRoundProgressBar.setProgress(Integer.parseInt(message));
                    break;
                case 2:
                    t = (TextView)findViewById(R.id.airFlowTextView);
                    t.setText("质量空气流量： "+ message);
                    break;
                case 3:
                    t = (TextView)findViewById(R.id.MAPTextView);
                    t.setText("进气歧管压力： "+ message);
                    break;
                case 4:
                    t = (TextView)findViewById(R.id.ECTTextView);
                    t.setText("发动机冷却液温度： "+ message);
                    break;
                case 5:
                    t = (TextView)findViewById(R.id.speedTextView);
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
