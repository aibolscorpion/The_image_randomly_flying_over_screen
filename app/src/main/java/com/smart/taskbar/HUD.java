package com.smart.taskbar;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.smart.taskbar.model.Droid;

public class HUD extends Service{
    ImageButton mButton;
    Droid droid;
    NotificationManager nm;
    int x=0;
    WindowManager wm;
    MainGamePanel mainGamePanel;
    @Override
    public IBinder
    onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //mView = new HUDView(this);
        mainGamePanel  = new MainGamePanel(this);

         wm = (WindowManager) getSystemService(WINDOW_SERVICE);



        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        wm.addView(mainGamePanel, params);


    }
    public MainGamePanel getMainGamePanel(){
        return mainGamePanel;
    }
    @Override
    public void onDestroy() {

        mainGamePanel.getMainThread().setRunning(false);
        wm.removeView(mainGamePanel);
        super.onDestroy();


    }

}
