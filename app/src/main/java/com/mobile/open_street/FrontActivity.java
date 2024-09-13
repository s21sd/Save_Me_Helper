package com.mobile.open_street;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class FrontActivity extends AppCompatActivity {

    private TextView tvLatLong;
    private Button btnShowOnMap;
    private String latitude = "0.0";
    private String longitude = "0.0";
    private static final int SMS_PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_front);

        tvLatLong = findViewById(R.id.tvLatLong);
        btnShowOnMap = findViewById(R.id.btnShowOnMap);

        // Request SMS receive permission if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }

        // Handle incoming message if any
        handleIncomingMessage(getIntent());

        // Set onClickListener to navigate to MainActivity
        btnShowOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pass the latitude and longitude to MainActivity via Intent
                Intent intent = new Intent(FrontActivity.this, MainActivity.class);
                intent.putExtra("latitude", Double.parseDouble(latitude));  // Pass latitude as double
                intent.putExtra("longitude", Double.parseDouble(longitude)); // Pass longitude as double
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingMessage(intent); // Handle new incoming messages
    }

    private void handleIncomingMessage(Intent intent) {
        if (intent != null && intent.hasExtra("message")) {
            String message = intent.getStringExtra("message");
            Log.d("FrontActivity", "Received message: " + message);

            if (message != null) {
                String[] parts = message.split(", ");
                for (String part : parts) {
                    if (part.startsWith("lat: ")) {
                        latitude = part.substring(5); // Extract latitude value
                    } else if (part.startsWith("log: ")) {
                        longitude = part.substring(5); // Extract longitude value
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
