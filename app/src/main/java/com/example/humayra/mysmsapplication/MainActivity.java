package com.example.humayra.mysmsapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText recipient,messagebody;
    private Button sendButton,timebtn;
    String msg, theNumber;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PICK_CONTACT = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recipient = (EditText) findViewById(R.id.recipientTextbox);
        messagebody = (EditText) findViewById(R.id.messagebox);
        timebtn = (Button) findViewById(R.id.setTimebtn);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                String[] permissions = {Manifest.permission.SEND_SMS};
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);

            }
            if (checkSelfPermission(Manifest.permission.WAKE_LOCK)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to WAKE_LOCK - requesting it");
                String[] permissions = {Manifest.permission.WAKE_LOCK};
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);

            }
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to READ_CONTACTS - requesting it");
                String[] permissions = {Manifest.permission.READ_CONTACTS};
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);

            }
        }


    }

    public void pickAContactNumber(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (reqCode) {
                case PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Toast.makeText(this, "Failed to pick contact!", Toast.LENGTH_SHORT).show();
        }
    }

    private void contactPicked(Intent data) {
        Cursor cursor = null;
        try {
            String phoneNo = null ;
            Uri uri = data.getData();
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            phoneNo = cursor.getString(phoneIndex);
            recipient.setText(phoneNo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter("mybroadcast"));

    }

    @Override
    public void onPause() {
        super.onPause();
        registerReceiver(broadcastReceiver, new IntentFilter("mybroadcast"));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    BroadcastReceiver broadcastReceiver = new MyTask() {
        @Override
        public void onReceive(Context context, Intent intent) {

            sendmsg(theNumber,msg);

        }
    };




    public void setTime(View view){
        theNumber=recipient.getText().toString();
        msg=messagebody.getText().toString();
        final Calendar calender = Calendar.getInstance();
        calender.setFirstDayOfWeek(Calendar.SATURDAY);
        final int hour = calender.get(Calendar.HOUR);
        final int minute = calender.get(Calendar.MINUTE);


        TimePickerDialog timePickerDialog;
        timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                timebtn.setText(i+":"+i1);
                final Calendar c = Calendar.getInstance();
                c.setFirstDayOfWeek(Calendar.SATURDAY);
                c.set(Calendar.HOUR_OF_DAY, i);
                c.set(Calendar.MINUTE, i1);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);

                long millis = c.getTimeInMillis();
               setAlarm(millis);

            }
        },hour,minute,false);
        timePickerDialog.show();
    }

    private void setAlarm(long timeInMillis) {
        AlarmManager alarmManger = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(MainActivity.this,MyTask.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,0,intent,0);
        alarmManger.set(AlarmManager.RTC_WAKEUP,timeInMillis,pendingIntent);
        Toast.makeText(this, "SMS Scheduled!!", Toast.LENGTH_SHORT).show();
    }




    public void sendmsg(String theNumber, String msg) {
        String sent = "Message sent!!";
        String deliverd = "Message delivered!!";
        PendingIntent sentPI = PendingIntent.getBroadcast(MainActivity.this,0,new Intent(sent),0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(MainActivity.this,0,new Intent(deliverd),0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "SMS sent!!", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(MainActivity.this, "Generic Failure!!", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(MainActivity.this, "No service!!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(sent));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "SMS delivered!!", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(MainActivity.this, "SMS not delivered!!", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        }, new IntentFilter(deliverd));


        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(theNumber,null,msg,sentPI,deliveredPI);



    }






}
