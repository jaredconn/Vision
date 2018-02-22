package com.example.j.vision;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String CLOUD_VISION_API_KEY = "AIzaSyCPeO7TayOnyDMwaarp_HKV9g3guJN2qi4";
    Button button;

    int REQUEST_CODE = 1;
    ImageView IMG;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();
        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //saving the image to a file
                File imageDirectory = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/" ); //gallery directory
                String fileName = getFileName();
                File imageFile = new File(imageDirectory, fileName);
                Uri imageUri = Uri.fromFile(imageFile);
                galleryAddPic("storage/emulated/0/DCIM/Camera/image" + i + ".jpg");
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                if (cameraIntent.resolveActivity(getPackageManager()) != null) //check if camera application is available on device
                {
                    startActivityForResult(cameraIntent, REQUEST_CODE);
                }
            }
        });
    }
    private String getFileName() {
        i++;
        return "image" + i + ".jpg";
    }
    /**
     * Saves the taken photo to the gallery
     * @param mCurrentPhotoPath
     */
    protected void galleryAddPic(String mCurrentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE || requestCode == 0) {
            if (resultCode == RESULT_OK) //operation was successful
            {
                if (requestCode == 1) //then we're taking a new photo
                {
                    File imageFile = new File("storage/emulated/0/DCIM/Camera/image" + i + ".jpg");

                    if(imageFile.exists()){
                        Intent intent = new Intent(this, PassPhoto.class);
                        String path = "storage/emulated/0/DCIM/Camera/image" + i + ".jpg";
                        intent.putExtra("path",path);
                        startActivity(intent); // starting next activity
                    }
                }
            }
        }
    }

}

