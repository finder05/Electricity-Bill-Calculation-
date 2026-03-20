package com.example.myapplication;



import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private EditText unitsEditText, phoneEditText;
    private Button calculateBtn;

    private static final int SMS_PERMISSION_CODE = 100;

    private String messageToSend = "";
    private String phoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unitsEditText = findViewById(R.id.unitsEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        calculateBtn = findViewById(R.id.calculateBtn);

        calculateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String unitsStr = unitsEditText.getText().toString().trim();
                phoneNumber = phoneEditText.getText().toString().trim();

                if (unitsStr.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter units consumed", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (phoneNumber.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }

                double unitsConsumed = Double.parseDouble(unitsStr);
                double billAmount = calculateBill(unitsConsumed);

                messageToSend = "Your electricity bill is: ₹" + String.format("%.2f", billAmount);

                showAlert(messageToSend);
            }
        });
    }

    private double calculateBill(double units) {
        // Sample tariff (You can change rates as per requirement)
        // For example:
        // First 100 units: ₹1.5 per unit
        // Next 100 units: ₹2 per unit
        // Above 200 units: ₹3 per unit

        double bill = 0;

        if (units <= 100) {
            bill = units * 1.5;
        } else if (units <= 200) {
            bill = 100 * 1.5 + (units - 100) * 2;
        } else {
            bill = 100 * 1.5 + 100 * 2 + (units - 200) * 3;
        }

        return bill;
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Electricity Bill");
        builder.setMessage(message);
        builder.setCancelable(false);

        builder.setPositiveButton("Send SMS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendSMSMessage();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendSMSMessage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            sendSMS();
        }
    }

    private void sendSMS() {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, messageToSend, null, null);
            Toast.makeText(getApplicationContext(), "SMS Sent!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // Handle permission request response
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSMS();
            } else {
                Toast.makeText(this,
                        "Permission denied to send SMS", Toast.LENGTH_SHORT).show();
            }
        }
    }
}