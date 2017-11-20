package com.pdf_plugin;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 项目根目录有个123.pdf的测试文件 拷到手机根目录 如下路径
     */
    private String path = "/storage/emulated/0/123.pdf";
    /**
     * TextView
     */
    private TextView mTvCpuInfo;
    /**
     * 下载pdf插件,并打开pdf文件
     */
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        String abi;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String[] supportedAbis = Build.SUPPORTED_ABIS;//Build.SUPPORTED_ABIS得到根据偏好排序的设备支持的ABI列表
            String[] supported32BitAbis = Build.SUPPORTED_32_BIT_ABIS;
            String[] supported64BitAbis = Build.SUPPORTED_64_BIT_ABIS;
            try {
                String cpuAbi = Build.CPU_ABI;
                String cpuAbi2 = Build.CPU_ABI2;
            } catch (Exception e) {//过时了也是可以获取到的 可以对比一下数据
                e.printStackTrace();
            }
            abi = supportedAbis[0];
        } else {
            String cpuAbi = Build.CPU_ABI;
            String cpuAbi2 = Build.CPU_ABI2;
            abi = cpuAbi;
        }
        mTvCpuInfo.setText(abi);
    }

    private void initView() {
        mTvCpuInfo = (TextView) findViewById(R.id.tv_cpu_info);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.button:
                PdfPlugin.getInstant(MainActivity.this, path).downLoadPlugin();
                break;
        }
    }
}
