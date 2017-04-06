package com.pdf_plugin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    /**
     * 项目根目录有个123.pdf的测试文件 拷到手机根目录 如下路径
     */
    private String path = "/storage/emulated/0/123.pdf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PdfPlugin.getInstant(MainActivity.this, path).downLoadPlugin();
            }
        });
    }
}
