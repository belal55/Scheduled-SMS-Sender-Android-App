package com.example.humayra.mysmsapplication;

        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.os.Bundle;
        import android.widget.Toast;

/**
 * Created by FROZEN WARRIOR on 11/18/2017.
 */

public class MyTask extends BroadcastReceiver {

    MainActivity main;

    @Override
    public void onReceive(Context context, Intent intent) {

            Intent intent1 = new Intent("mybroadcast");
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.sendBroadcast(intent1);



    }
}
