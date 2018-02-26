package com.example.j.vision;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Matrix;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * adapted from https://developer.android.com/things/training/doorbell/cloud-vision.html
 * Created by jwc374 on 2/21/2018.
 * displays the photo in ImageView
 * compresses the bitmap and converts it to byteArray
 * byte array gets passed to google
 * switches between threads
 *
 * steps for using Vision api:
 * 1) Construct the Vision API instance
 * 2) Create the image request
 * 3) Add the features we want
 * 4) Batch and execute the request
 * 5) Convert response into a readable collection of annotations
 */

public class PassPhoto extends AppCompatActivity {

    private static final String TAG = "PassPhoto.java";
    private static final String CLOUD_VISION_API_KEY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_photo);

        final ImageView IMG;
        IMG = (ImageView) findViewById(R.id.img);

        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        File imageFile = new File(path);
        Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath()); //retrieving file

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
        //when placing a bitmap in image view from gallery, its rotated on its side :(
        IMG.setImageBitmap(rotatedBitmap);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        myBitmap = Bitmap.createScaledBitmap(myBitmap, 160, 160, true);
        myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        final byte[] byteArray = stream.toByteArray();


        //start a new thread
        new Thread(new Runnable() {
            public void run() {
                onPictureTaken(byteArray);
            }
        }).start();
    }


    public static Map<String, Float> annotateImage(byte[] imageBytes) throws IOException {
        // Construct the Vision API instance
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        VisionRequestInitializer initializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY);
        Vision vision = new Vision.Builder(httpTransport, jsonFactory, null).setVisionRequestInitializer(initializer).setApplicationName("vision").build();

        // Create the image request
        AnnotateImageRequest imageRequest = new AnnotateImageRequest();
        Image image = new Image();
        image.encodeContent(imageBytes);
        imageRequest.setImage(image);

        // Add the features we want
        Feature labelDetection = new Feature();
        labelDetection.setType("LABEL_DETECTION");
        labelDetection.setMaxResults(10);
        imageRequest.setFeatures(Collections.singletonList(labelDetection));

        // Batch and execute the request
        BatchAnnotateImagesRequest requestBatch = new BatchAnnotateImagesRequest();
        requestBatch.setRequests(Collections.singletonList(imageRequest));
        BatchAnnotateImagesResponse response = vision.images().annotate(requestBatch).setDisableGZipContent(true).execute();

        return convertResponseToMap(response);
    }

    private static Map<String, Float> convertResponseToMap(BatchAnnotateImagesResponse response) {
        Map<String, Float> annotations = new HashMap<>();

        // Convert response into a readable collection of annotations
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                annotations.put(label.getDescription(), label.getScore());
            }
        }
        return annotations;
    }

        public void onPictureTaken(final byte[] imageBytes) {
            if (imageBytes != null) {
                try {
                    // Process the image using Cloud Vision
                    final Map<String, Float> annotations = annotateImage(imageBytes);
                    Log.d(TAG, "cloud vision annotations:" + annotations);

                    //switch back to the main thread to access the textview
                    runOnUiThread(new Runnable(){
                        public void run() {

                            //find the highest likelihood match
                            String maxKey = null;
                            Float maxValue = Float.MIN_VALUE;
                            for (Map.Entry<String, Float> entry : annotations.entrySet()) {
                                Float value = entry.getValue();
                                if (value > maxValue) {
                                    maxKey = entry.getKey();
                                    maxValue = value;
                                }
                            }
                            final TextView tv = (TextView) findViewById(R.id.resultTv);
                            Button yes = findViewById(R.id.yes);
                            Button no = findViewById(R.id.no);

                            //this will be the top percentage result
                            tv.setText("Is this a(n) " + maxKey + "?");

                            //this is a correct guess by google
                            yes.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(PassPhoto.this, ResultClass.class);
                                    startActivity(intent); // starting next activity
                                }
                            });

                            //not a correct guess
                            no.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(PassPhoto.this, GoogleGuessedWrong.class);
                                    intent.putExtra("map", (Serializable) annotations);
                                    startActivity(intent); // starting next activity
                                }
                            });
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "Cloud Vision API error: ", e);
                }
            }
        }
    }









