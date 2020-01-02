package com.ggh.video;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.videocall.R;
import com.ggh.video.net.udp.NettyClient;
import com.ggh.video.net.udp.NettyReceiverHandler;
import com.ggh.video.utils.MyConstants;
import com.ggh.video.utils.NetUtils;
import com.ggh.video.utils.PermissionManager;
import com.yanzhenjie.permission.Permission;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.ed_ip)
    EditText edIp;

    @BindView(R.id.tv_ip)
    TextView tvIp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initPermission();
        tvIp.setText("本地ip地址为："+NetUtils.getIPAddress(MainActivity.this));
        NettyClient.getIns().setCallCallback(new NettyReceiverHandler.CallCallback() {
            @Override
            public void call() {
                startActivity(new Intent(MainActivity.this,VideoTalkActivity.class));
            }
        });
    }

    /**
     * 初始化权限事件
     */
    private void initPermission() {
        //检查权限
        PermissionManager.requestPermission(MainActivity.this, new PermissionManager.Callback() {
            @Override
            public void permissionSuccess() {
                PermissionManager.requestPermission(MainActivity.this, new PermissionManager.Callback() {
                    @Override
                    public void permissionSuccess() {
                        PermissionManager.requestPermission(MainActivity.this, new PermissionManager.Callback() {
                            @Override
                            public void permissionSuccess() {

                            }

                            @Override
                            public void permissionFailed() {

                            }
                        }, Permission.Group.STORAGE);
                    }

                    @Override
                    public void permissionFailed() {

                    }
                }, Permission.Group.MICROPHONE);
            }

            @Override
            public void permissionFailed() {

            }
        }, Permission.Group.CAMERA);

    }

    @OnClick(R.id.start)
    public void onViewClicked() {
        String targetIp = edIp.getText().toString();
        if (TextUtils.isEmpty(targetIp)){
            Toast.makeText(MainActivity.this, "目标ip不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        NettyClient.getIns().setTargetIp(targetIp);
        NettyClient.getIns().sendData("call".getBytes(), MyConstants.MSG_TYPE_NORMAL);
    }

    @Override
    protected void onDestroy() {
        NettyClient.getIns().close();
        super.onDestroy();
    }
}
