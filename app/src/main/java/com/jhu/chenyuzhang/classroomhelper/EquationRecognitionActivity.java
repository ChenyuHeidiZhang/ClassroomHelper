package com.jhu.chenyuzhang.classroomhelper;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.github.kexanie.library.MathView;

public class EquationRecognitionActivity extends AppCompatActivity {

    ImageView imageView;

    public static final int REQUEST_PERM_WRITE_STORAGE = 102;
    static final int REQUEST_TAKE_PHOTO = 100;

    private TextView mTextViewResult;
    private MathView formula_one;

    public volatile static Bitmap capturedBitmap;
    private static final String KEY_BITMAP = "keyBitmap";
    public SharedPreferences prefBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equation_recognition);

        imageView = findViewById(R.id.photoImage_eq);
        mTextViewResult = findViewById(R.id.text_view_result);
        formula_one = findViewById(R.id.formula_one);

        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivityForResult(intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(EquationRecognitionActivity.this, new String[]{Manifest.permission.CAMERA},1);
            }
        }
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(EquationRecognitionActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERM_WRITE_STORAGE);
        }

        takePhoto();

        new Call().execute();

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
                    capturedBitmap = (Bitmap) returnIntent.getExtras().get("data");

                    imageView.setImageBitmap(capturedBitmap);

                    //saveImageToGallery(capturedBitmap);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream .toByteArray();
                    String data = Base64.encodeToString(byteArray, Base64.DEFAULT);

                    prefBitmap = getSharedPreferences("bitmap", MODE_PRIVATE);
                    prefBitmap.edit().putString(KEY_BITMAP, data).apply();

                    break;

                default:
                    break;
            }
        }
    }

    /*private void saveImageToGallery(Bitmap finalBitmap) {

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

            Toast.makeText(EquationRecognitionActivity.this, "Photo Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(EquationRecognitionActivity.this, "Exception Throw", Toast.LENGTH_SHORT).show();
        }
    }*/


    private class Call extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");

            prefBitmap = getSharedPreferences("bitmap", MODE_PRIVATE);
            String data = prefBitmap.getString(KEY_BITMAP,"");

            Log.d("data",data);

//            System.out.println(data.substring(0, 40));
            String bseString = "{ \"src\" : \" " + "data:image/jpg;base64," + data + " \"  }";
            //String bseString = "{ \"src\" : \" " + data + " \"  }";
            RequestBody body = RequestBody.create(mediaType, bseString);

            Request request = new Request.Builder()
                    .url("https://api.mathpix.com/v3/latex")
                    .addHeader("content-type", "application/json")
                    .addHeader("app_id", "garycloudyang_gmail_com")
                    .addHeader("app_key", "43d5eeba93314d645135")
                    .post(body)
                    .build();

            try {
                System.out.print("Executed!");
                Response response = client.newCall(request).execute();
                String myResponse = response.body().string();

                try {
                    JSONObject Jobject = new JSONObject(myResponse);
                    final String myLatex = Jobject.getString("latex");
                    EquationRecognitionActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextViewResult.setText(myLatex);
                            formula_one.setText(myLatex);
                        }
                    });
                } catch (JSONException e) {
                }

            } catch (IOException e) {
                System.out.print("IO Error!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }
    }

}
