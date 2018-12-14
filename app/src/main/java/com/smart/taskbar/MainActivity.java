package com.smart.taskbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.smart.taskbar.model.Droid;

public class MainActivity extends Activity {
    SharedPreferences sPref;
    SharedPreferences sp;
    final int DIALOG = 1;
    final int DIALOG_CONTINUE = 0;
    String UrlOfImage;
    Bitmap bug,image;
    NotificationManager nm;
    Droid droid;
    BroadcastReceiver mReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nm = (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);
        sPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor ed = sPref.edit();
        setContentView(R.layout.activity_main);

        registerReceiverMethod();
    }
    public void registerReceiverMethod(){
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }


    public void onClickSettings(View v){
        Intent i = new Intent(this,PrefActivity.class);
        startActivity(i);
    }
    public void onClickInfo(View v){
        showDialog(DIALOG);
    }
    public void startHUDService(){
        startService(new Intent(this, HUD.class));
    }
    public void stopHUDService(){
        stopService(new Intent(this, HUD.class));

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sp.getBoolean("screensaver",false)){
            startHUDService();
        }else{
            stopHUDService();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // дерегистрируем (выключаем) BroadcastReceiver
        Log.i("aibol","unregister receiver");
        unregisterReceiver(mReceiver);
    }
    protected Dialog onCreateDialog(int id) {

        if (id == DIALOG) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle("The value of viewsTotal is:");
            adb.setMessage(sPref.getInt("viewsTotal", 0) + "");
            return adb.create();
        }
            // кнопка нейтрального ответа
            // создаем диалог
        return super.onCreateDialog(id);
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        if (id == DIALOG) {
            ((AlertDialog)dialog).setMessage(sPref.getInt("viewsTotal", 0) + "");
        }if (id == DIALOG_CONTINUE) {
        }
    }

    void sendNotifToContinue() {
        // 1-я часть
        Notification notif = new Notification(R.drawable.ic_launcher, "Click to continue",
                System.currentTimeMillis());

        // 3-я часть
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // 2-я часть
        notif.setLatestEventInfo(this, "Yuksel Screensaver","Click to continue", pIntent);

        // ставим флаг, чтобы уведомление пропало после нажатия
        notif.flags |= Notification.FLAG_AUTO_CANCEL;

        // отправляем
        nm.notify(1, notif);
    }

    class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) ) {

                stopHUDService();


            }else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON) && sp.getBoolean("screensaver",false)){

                sendNotifToContinue();
            }
        }
    }
}

