package com.ccl.view.radarview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RadarView radarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        radarView = (RadarView) findViewById(R.id.radarView);

        ArrayList<RadarView.RadarModel> radarModels = new ArrayList<>();
        RadarView.RadarModel radarModel = new RadarView.RadarModel(40, "张三");
        radarModels.add(radarModel);
        radarModel = new RadarView.RadarModel(80, "张三as");
        radarModels.add(radarModel);
        radarModel = new RadarView.RadarModel(90, "张三sdsad");
        radarModels.add(radarModel);
        radarModel = new RadarView.RadarModel(20, "张三aaa");
        radarModels.add(radarModel);
        radarModel = new RadarView.RadarModel(100, "张三1");
        radarModels.add(radarModel);

        radarView.setData(radarModels);
    }
}
