package com.example.xiao.guaguaka;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.xiao.guaguaka.view.GuaGuaKaView;

public class MainActivity extends AppCompatActivity {

    private GuaGuaKaView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mView = (GuaGuaKaView) findViewById(R.id.guaguaka);
        mView.setOnGuaGuaKaCompleteListenner(new GuaGuaKaView.OnGuaGuaKaCompleteListenner() {

            @Override
            public void onComplete() {
                Toast.makeText(MainActivity.this, "完成", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
