package com.example.j.vision;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


/**
 * Created by J on 2/23/2018.
 */

public class ResultClass  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_class);
        TextView tv = (TextView) findViewById(R.id.tv);
        Intent intent = getIntent();
    }
}
