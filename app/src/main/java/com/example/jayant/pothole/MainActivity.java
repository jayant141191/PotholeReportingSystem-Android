package com.example.jayant.pothole;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int RESULT_LOAD_IMAGE=1;
    private static final String SERVER_ADDRESS = "http://10.130.96.98/";
    private String mImageFileLocation="";
    ImageView imageToUpload;
    Button bUploadImage,btToTakePhoto;
    EditText uploadImageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageToUpload = (ImageView)findViewById(R.id.imageToUpload);
        bUploadImage = (Button)findViewById(R.id.bUploadImage);
        uploadImageName = (EditText)findViewById(R.id.etUploadName);
        btToTakePhoto = (Button)findViewById(R.id.bToTakePhoto);


        //imageToUpload.setOnClickListener(this);
        btToTakePhoto.setOnClickListener(this);
        bUploadImage.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.bToTakePhoto:

                //Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(galleryIntent,RESULT_LOAD_IMAGE);
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                File photoFile = null;
                try{
                     photoFile = createImageFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent, RESULT_LOAD_IMAGE);
                break;

            case R.id.bUploadImage:
                Bitmap image = ((BitmapDrawable) imageToUpload.getDrawable()).getBitmap();
                new UploadImage(image,uploadImageName.getText().toString()).execute();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null){
            //Uri selectedImage = data.getData();
            //imageToUpload.setImageURI(selectedImage);
            //Bitmap capturedImage = (Bitmap)data.getExtras().get("data");
            Bitmap capturedImage = BitmapFactory.decodeFile(mImageFileLocation);
            /*ExifInterface exifInterface = null;
            try {
                   exifInterface = new ExifInterface(data.getData().getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();
            switch(orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                default:
            }

            Bitmap rotatedBitmap = Bitmap.createBitmap(capturedImage,0,0,capturedImage.getWidth(),capturedImage.getHeight(),matrix,true);
            */
            imageToUpload.setImageBitmap(capturedImage);


        }

    }

    File createImageFile() throws IOException{

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);
        mImageFileLocation = image.getAbsolutePath();
        return image;
    }

    private class UploadImage extends AsyncTask<Void,Void,Void> {

        Bitmap image;
        String name;
        public UploadImage(Bitmap image, String name){
            this.image = image;
            this.name = name;
        }
        @Override
        protected Void doInBackground(Void... params) {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);

            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("image",encodedImage));
            dataToSend.add(new BasicNameValuePair("name",name));

            HttpParams httpRequestParams = getHttpRequestParams();

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "SavePicture.php");
            try{
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            }catch (Exception e){
               e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(),"Image Uploaded",Toast.LENGTH_SHORT).show();
        }
    }

    private HttpParams getHttpRequestParams(){
        HttpParams httpRequestParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpRequestParams, 100 * 30);
        HttpConnectionParams.setSoTimeout(httpRequestParams,100*30);
        return httpRequestParams;
    }
}
