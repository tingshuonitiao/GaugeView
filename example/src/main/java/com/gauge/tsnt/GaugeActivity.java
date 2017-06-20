package com.gauge.tsnt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.gauge.tsnt.gaugeview.GaugeView;

public class GaugeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gauge);
        final TextView location = (TextView) findViewById(R.id.location);
        GaugeView gauge = (GaugeView) findViewById(R.id.gauge);
        gauge.setOnGaugeScrollChangeListener(new GaugeView.OnGaugeScrollChangeListener() {
            @Override
            public void onGaugeScrollChange(int currentMiddleNum) {
                location.setText("location=" + currentMiddleNum);
            }
        });
    }
}
