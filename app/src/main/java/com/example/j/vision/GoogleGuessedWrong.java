package com.example.j.vision;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Map;

/**
 * Created by J on 2/23/2018.
 * receives the key / value pairs from google vision
 */

public class GoogleGuessedWrong extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gg_wrong);

       Intent intent = getIntent();
       final Map<String, Float> map = (Map<String, Float>) intent.getSerializableExtra("map");

        final TextView result = (TextView) findViewById(R.id.result);
        final EditText input = (EditText) findViewById(R.id.et);
        Button submit = (Button) findViewById(R.id.submit);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (Map.Entry<String, Float> entry : map.entrySet()) {
                    if (entry.getKey().equals(input.getText().toString())) {
                        result.setText("There was a " + (entry.getValue() * 100) + "% chance of it being a " + input.getText().toString());
                        break;
                    }
                    else{
                        result.setText("No results were returned for " + input.getText().toString());
                    }
                }

            }
        });

    }
}
