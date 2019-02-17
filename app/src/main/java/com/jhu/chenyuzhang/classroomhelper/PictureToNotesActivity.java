package com.jhu.chenyuzhang.classroomhelper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureToNotesActivity extends AppCompatActivity {

    ImageView imageView;
    EditText display;
    TextView tv_display;

    public static final int REQUEST_PERM_WRITE_STORAGE = 102;
    static final int REQUEST_TAKE_PHOTO = 1;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;
    Button btn_gallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_to_notes);

        imageView = findViewById(R.id.photoImage);
        display = findViewById(R.id.display);
        tv_display = findViewById(R.id.tv_display);

        btn_gallery = findViewById(R.id.button_gallery);

        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivityForResult(intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PictureToNotesActivity.this, new String[]{Manifest.permission.CAMERA},1);
            }
        }
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(PictureToNotesActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERM_WRITE_STORAGE);

        }
        takePhoto();

        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

    }

    /*protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        imageView.setImageBitmap(bitmap);
    } */
    


    public void takePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        super.onActivityResult(requestCode, resultCode, returnIntent);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO:
                    Bitmap capturedBitmap = (Bitmap) returnIntent.getExtras().get("data");

                    //imageView.setImageBitmap(capturedBitmap);

                    saveImageToGallery(capturedBitmap);
                    break;

                case PICK_IMAGE:
                    imageUri = returnIntent.getData();
                    imageView.setImageURI(imageUri);
                    Bitmap bm=((BitmapDrawable)imageView.getDrawable()).getBitmap();
                    recognizeText(bm);

                default:
                    break;
            }
        }
    }

    private void saveImageToGallery(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root+"/saveImage");
        myDir.mkdirs();

        //Random generator = new Random();
        //int n = 10000;
        //n = generator.nextInt();
        //String imageName = "Image-" + n +".jpg";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "JPEG_" + timeStamp + "_";
        File file = new File(myDir, imageName);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG,90,out);


            //String resizeImagePath = file.getAbsolutePath();
            out.flush();
            out.close();

            Toast.makeText(PictureToNotesActivity.this, "Photo Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(PictureToNotesActivity.this, "Exception Throw", Toast.LENGTH_SHORT).show();
        }
    }


    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }


    private void recognizeText(Bitmap bitmap1) {
        //final Bitmap bitmap1 = BitmapFactory.decodeResource(
        //        getApplicationContext().getResources(),R.drawable.pic_to_notes
        //);

        //imageView.buildDrawingCache();
        //Bitmap bitmap1 = imageView.getDrawingCache();

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if(!textRecognizer.isOperational()) {
            Toast.makeText(getApplicationContext(),"Could not get the Text", Toast.LENGTH_SHORT).show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap1).build();

            SparseArray<TextBlock> items = textRecognizer.detect(frame);

            StringBuilder sb = new StringBuilder();

            for(int i=0; i<items.size(); ++i) {
                TextBlock myItem = items.valueAt(i);
                sb.append(myItem.getValue());
                sb.append("\n");
            }

            display.setEnabled(true);
            display.setText(sb.toString());
            display.setEnabled(false);

            //tv_display.setText(sb.toString());
        }

        textRecognizer.release();
    }

}
