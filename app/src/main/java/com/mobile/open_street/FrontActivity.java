package com.mobile.open_street;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class FrontActivity extends AppCompatActivity {

    private TextView tvLatLong;
    private Button btnShowOnMap ,readmsg;
    private Button btnShowInMaps;
    private String latitude = "0.0";
    private String longitude = "0.0";
    private static final int SMS_PERMISSION_REQUEST_CODE = 200;
    private static final int READ_SMS_PERMISSION_REQUEST_CODE = 201;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_front);

        tvLatLong = findViewById(R.id.tvLatLong);
        btnShowOnMap = findViewById(R.id.btnShowOnMap);
        btnShowInMaps = findViewById(R.id.showinmaps);
        readmsg=findViewById(R.id.readmsg);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }

        handleIncomingMessage(getIntent());

        btnShowOnMap.setOnClickListener(view -> {
            Intent intent = new Intent(FrontActivity.this, MainActivity.class);
            intent.putExtra("latitude", Double.parseDouble(latitude));
            intent.putExtra("longitude", Double.parseDouble(longitude));
            startActivity(intent);
        });
        readmsg.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                readLatestSms();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, READ_SMS_PERMISSION_REQUEST_CODE);
            }
        });


        btnShowInMaps.setOnClickListener(view -> {
            if (!latitude.equals("0.0") && !longitude.equals("0.0")) {
                // Create an Intent to open Google Maps
                Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } else {
                Log.e("FrontActivity", "Latitude or Longitude not set.");
                Toast.makeText(this, "Latitude or Longitude not set.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void readLatestSms() {
        // Query SMS inbox
        Uri inboxUri = Uri.parse("content://sms/inbox");
        String[] projection = new String[]{"address", "body", "date"};
        String sortOrder = "date DESC";

        try (Cursor cursor = getContentResolver().query(inboxUri, projection, null, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                // Retrieve the message details
                String messageBody = cursor.getString(cursor.getColumnIndexOrThrow("body"));

                // Log the message body for debugging
                Log.d("FrontActivity", "Message: " + messageBody);

                String lat = null;
                String log = null;

                if (messageBody.contains("lat:") && messageBody.contains("log:")) {
                    String[] parts = messageBody.split(", ");
                    for (String part : parts) {
                        if (part.startsWith("lat: ")) {
                            lat = part.substring(5);
                        } else if (part.startsWith("log: ")) {
                            log = part.substring(5);
                        }
                    }

                    if (lat != null && log != null) {
                        latitude = lat;
                        longitude = log;
                        tvLatLong.setText("Latitude: " + latitude + ", Longitude: " + longitude);


                        updateMapWithLocation(latitude, longitude);
                    } else {
                        tvLatLong.setText("Latitude or Longitude not found in the message.");
                    }
                } else {
                    tvLatLong.setText("No coordinates found in the latest message.");
                }
            } else {
                Log.e("FrontActivity", "No SMS messages found.");
                tvLatLong.setText("No recent message found.");
            }
        } catch (Exception e) {
            Log.e("FrontActivity", "Error reading SMS", e);
        }
    }


    private void updateMapWithLocation(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lng = Double.parseDouble(longitude);

        Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Google Maps not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingMessage(intent);
    }

    private void handleIncomingMessage(Intent intent) {
        if (intent != null && intent.hasExtra("message")) {
            String message = intent.getStringExtra("message");
            Log.d("FrontActivity", "Received message: " + message);

            if (message != null) {
                String[] parts = message.split(", ");
                for (String part : parts) {
                    if (part.startsWith("lat: ")) {
                        latitude = part.substring(5);
                    } else if (part.startsWith("log: ")) {
                        longitude = part.substring(5);
                    }
                }

                tvLatLong.setText("Latitude: " + latitude + ", Longitude: " + longitude);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("FrontActivity", "SMS permission granted");
            } else {
                Log.e("FrontActivity", "SMS permission denied");
            }
        }
    }
}
