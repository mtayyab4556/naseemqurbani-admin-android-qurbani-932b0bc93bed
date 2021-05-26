package com.example.qurbaninotifications;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.qurbaninotifications.network.WSResponse;
import com.example.qurbaninotifications.network.WsManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import e.mirzashafique.lib.Storage;
import e.mirzashafique.lib.model.SelectedFiles;

public class ServiceActivity extends AppCompatActivity {

    private ImageView imageView;
    private EditText notificationTitle, notificationDescription;
    private Button selectImage, uploadImage, pushNotification;
    private Spinner spinner;
    private Uri selecteImageUri;
    private ProgressBar progressBar;
    private String path = "mm", myImageName="";

    private WsManager wsManager;
    private TransferUtility transferUtility;
    private AmazonS3Client s3Client;
    TransferObserver observer;

    String tokes[] = {"f-b2djKaRqY:APA91bFXnB7nhiRVGNux65KK14WvENwKtYKIpz755YooPAJJsBxi1rI-5t9Vw0fCmiQgKVnqXC7yh1FOeQ9YQMRudSut1wRGWnQ5oMclK63dwtBjT2_65LoFuJZbo61dFVIOyOgeoWiY",
            "dMUrQUztrw4:APA91bHNqN7WSdC7v_0xCLgRKp_aq03lRE9zUPytaq2DblbhPLI0mE8u8iQajycQxZyhPF0r2WZrH8-SkP0S2aF3LKxDlnx60mUk7ZEkLs1dzc6BucCfQ_liC6_sPojDdjEPsNMe-z0B"};
    JSONArray jsonArray = new JSONArray();
    private RequestQueue requestQueue;

    private String URL = "https://fcm.googleapis.com/fcm/send";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        credentialsProvider();
        transferUtility = new TransferUtility(s3Client, getApplicationContext());
        wsManager = new WsManager(getApplicationContext());
        requestQueue = Volley.newRequestQueue(this);

        jsonArray.put("f-b2djKaRqY:APA91bFXnB7nhiRVGNux65KK14WvENwKtYKIpz755YooPAJJsBxi1rI-5t9Vw0fCmiQgKVnqXC7yh1FOeQ9YQMRudSut1wRGWnQ5oMclK63dwtBjT2_65LoFuJZbo61dFVIOyOgeoWiY");
        jsonArray.put("dMUrQUztrw4:APA91bHNqN7WSdC7v_0xCLgRKp_aq03lRE9zUPytaq2DblbhPLI0mE8u8iQajycQxZyhPF0r2WZrH8-SkP0S2aF3LKxDlnx60mUk7ZEkLs1dzc6BucCfQ_liC6_sPojDdjEPsNMe-z0B");


        imageView = findViewById(R.id.noti_im);
        notificationTitle = findViewById(R.id.noti_title_et);
        notificationDescription = findViewById(R.id.noti_description_et);
        selectImage = findViewById(R.id.select_im_btn);
        uploadImage = findViewById(R.id.upload_im);
        pushNotification = findViewById(R.id.push_noti_btn);
        spinner = findViewById(R.id.spinner);
        progressBar = findViewById(R.id.progress_bar);

        List<String> list = new ArrayList<String>();
        list.add("Bara_Service");
        list.add("Our_Official_Stock");
        list.add("Featured_Category");
        list.add("Informative_Videos");
        list.add("Sadqa_Service");
        list.add("Upload_Image");
        list.add("Upload_Video");
        list.add("Conversations");
        list.add("Home_Page");
        list.add("User_Profile");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        spinner.setAdapter(dataAdapter);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Storage.create(ServiceActivity.this).showImages(1).start();
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        pushNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String title = notificationTitle.getText().toString();
                String noti_descrip = notificationDescription.getText().toString();
                String action = spinner.getSelectedItem().toString();

                if ((!title.isEmpty()) && (!noti_descrip.isEmpty()) && (!action.isEmpty())) {

                    if (myImageName != null && !myImageName.isEmpty()) {

                        sendNotifications(title, noti_descrip, action);

                    } else {
                        T.message(getApplicationContext(), "Upload Image first!");
                    }
                } else {
                    T.message(getApplicationContext(), "Please fill all the fields");
                }
            }
        });
    }

    private void sendNotifications(String title, String notif, String action) {

        JSONObject mainobj = new JSONObject();

        try {

            mainobj.put("to", "/topics/" + "03235400786");

            JSONObject notificationImage = new JSONObject();
            notificationImage.put("id", "1");
            notificationImage.put("title", title);
            notificationImage.put("body", notif);
            notificationImage.put("image", myImageName);
            notificationImage.put("click_action", action);
            notificationImage.put("notify", "service_notification");
            notificationImage.put("token", jsonArray.toString());
            mainobj.put("data", notificationImage);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    mainobj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    if (response.toString().equals("-1")) {
                        T.message(getApplicationContext(), "Notification sending failed");
                    } else {
                        T.message(getApplicationContext(), "Notification send successfully");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ServiceActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> header = new HashMap<>();
                    try {
                        header.put("content-type", "application/json");
                        header.put("authorization", "key=AIzaSyD_3jEsH5st6HryYJyRvk6iw5QTS7aGFtc");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return header;
                }
            };
            requestQueue.add(request);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void credentialsProvider() {

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-2:75494b62-6d89-40d4-b191-eed112e31eb2", // Identity Pool ID
                Regions.US_EAST_2 // Region
        );

        setAmazonS3Client(credentialsProvider);
    }

    public void setAmazonS3Client(CognitoCachingCredentialsProvider credentialsProvider) {
        s3Client = new AmazonS3Client(credentialsProvider);
        s3Client.setRegion(com.amazonaws.regions.Region.getRegion(Regions.US_EAST_1));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Storage.handlePermissions(requestCode, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Storage.shouldHandle(requestCode, resultCode, data)) {
            List<SelectedFiles> selectedFiles = Storage.getResults();
            Toast.makeText(getApplicationContext(), selectedFiles.size() + "", Toast.LENGTH_LONG).show();
            Log.d("www", "" + selectedFiles.size());
            imageView.setImageURI(selectedFiles.get(0).getFileUri());
            selecteImageUri = selectedFiles.get(0).getFileUri();
            path = selectedFiles.get(0).getFilePath();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void uploadImage() {


        File file1 = new File(path);
        String imageKey = generateUniqueFileName();
        myImageName = imageKey;
        Log.d("my_image_key", imageKey);
        observer = transferUtility.upload(
                "qurbaniimages",     /* The bucket to upload to */
                imageKey,    /* The key for the uploaded object */
                file1        /* The file where the data to upload exists */
        );
        uploadImageTransferObserverListener(observer);
        Log.d("my_image_key", "calling");

    }

    public void uploadImageTransferObserverListener(final TransferObserver transferObserver) {

        transferObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.e("statechange", state + "");
                if (state.toString() == "COMPLETED") {
                    Toast.makeText(getApplicationContext(), "Image is uploaded", Toast.LENGTH_LONG).show();
                    Log.d("my_image_key", "Completed");
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent / bytesTotal * 100);
                Toast.makeText(getApplicationContext(), "Progress in %"
                        + percentage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("statechange", ex.getMessage());
            }

        });

    }

    public String generateUniqueFileName() {
        String filename = "";
        long millis = System.currentTimeMillis();
        String datetime = new Date().toString();
        datetime = datetime.replace(" ", "");
        datetime = datetime.replace(":", "");
        Random r = new Random();
        int range = r.nextInt(21 - 15) + 15;
        filename = datetime + "_" + millis;
        return filename;
    }
}
