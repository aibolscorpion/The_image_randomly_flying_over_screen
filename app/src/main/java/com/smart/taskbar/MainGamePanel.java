/**
 * 
 */
package com.smart.taskbar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.smart.taskbar.model.Droid;
import com.smart.taskbar.model.components.Speed;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * @author impaler
 * This is the main surface that handles the ontouch events and draws
 * the image to the screen.
 */
public class MainGamePanel extends SurfaceView implements
		SurfaceHolder.Callback {
	private static final String TAG = "aibol";
	private String text;
	public MainThread thread;
	private Droid droid;
	private int viewsTotal = 0;
	Context context ;
	private NotificationManager nm;
	SharedPreferences sPref;
	Bitmap image;
	SharedPreferences sp;
	String UrlOfImage;
	Bitmap bug;
	int widthAndHeightOfImage = 70;
	Intent svc;
	@SuppressLint("NewApi")
	public MainGamePanel(Context context) {
		super(context);
		// adding the callback (this) to the surface holder to intercept events
		this.context = context;
		sp = PreferenceManager.getDefaultSharedPreferences(context);
		nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

		setBackgroundColor(Color.TRANSPARENT);
		setZOrderOnTop(true);
		svc = new Intent(getContext(),HUD.class);
		getHolder().addCallback(this);
		// create droid and load bitmap
		droid = new Droid(getBitmap(),0,200);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		// create the game loop thread
		thread = new MainThread(getHolder(), this);
		
		// make the GamePanel focusable so it can handle events
		setFocusable(true);


	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public Bitmap getBitmap(){
		UrlOfImage = sp.getString("urlOfImage","");
		if (UrlOfImage.isEmpty() || UrlOfImage == ""){
			bug = BitmapFactory.decodeResource(getResources(), R.drawable.basketball);
		}else {
			bug = getBitmapFromURL(UrlOfImage);
			if (bug == null){
				Toast.makeText(getContext(),"Wrong Url of image",Toast.LENGTH_LONG).show();
				bug = BitmapFactory.decodeResource(getResources(), R.drawable.basketball);
			}
		}
		image = Bitmap.createScaledBitmap(bug,
				widthAndHeightOfImage,widthAndHeightOfImage, false);
		return image;
	}
	public  Bitmap getBitmapFromURL(String src) {
		try {

			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			// Log exception
			return null;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// at this point the surface is created and
		// we can safely start the game loop

		thread.setRunning(true);
		thread.start();
	}
	public MainThread getMainThread(){
		return thread;
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// tell the thread to shut down and wait for it to finish
		// this is a clean shutdown
		boolean retry = true;
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}
	}

	public Droid getDroid(){
		return droid;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// delegating event handling to the droid
			droid.handleActionDown((int)event.getX(), (int)event.getY(),getContext());

		}
		return true;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void render(Canvas canvas) {

		text = sp.getString("textOnTopOfTheImage", "text");
		if(text.isEmpty() || text == ""){
			text = "text";
		}

		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		droid.draw(canvas, text);


	}

	/**
	 * This is the game update method. It iterates through all the objects
	 * and calls their update method if they have one or calls specific
	 * engine's update method.
	 */

	public void update() {
		// check collision with right wall if heading right
		if (droid.getSpeed().getxDirection() == Speed.DIRECTION_RIGHT
				&& droid.getX()-droid.getBitmap().getWidth() > getWidth() ) {
			sPref = getContext().getSharedPreferences("MyPrefs",Context.MODE_PRIVATE);
			SharedPreferences.Editor ed = sPref.edit();
			viewsTotal = sPref.getInt("viewsTotal",1)+1;
			if(sp.getBoolean("viewsTotal",false)){
				sendNotif(viewsTotal);
			}

			ed.putInt("viewsTotal",viewsTotal);
			ed.commit();
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			droid.setY(getRandomHeight());
			getRandomDirection();


		}else if(droid.getSpeed().getxDirection() == Speed.DIRECTION_LEFT
				&& droid.getX()<=0) 	{
			sPref = getContext().getSharedPreferences("MyPrefs",Context.MODE_PRIVATE);
			SharedPreferences.Editor ed = sPref.edit();
			viewsTotal = sPref.getInt("viewsTotal",1)+1;
			if(sp.getBoolean("viewsTotal",false)){
				sendNotif(viewsTotal);
			}

			ed.putInt("viewsTotal",viewsTotal);
			ed.commit();

			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Random rand = new Random();
			droid.setY(getRandomHeight());
			getRandomDirection();
		}
		// check collision with left wall if heading left

		// Update the lone droid
		droid.update();
	}
	public int getRandomHeight(){
		Random rand = new Random();
		return rand.nextInt(getHeight()-droid.getBitmap().getHeight());
	}
	protected void getRandomDirection(){
			int x;
			Random random = new Random();
			boolean direction = random.nextBoolean();
			if(direction){
				droid.getSpeed().setxDirection(Speed.DIRECTION_RIGHT);
				droid.setX(0);
			}else{
				droid.getSpeed().setxDirection(Speed.DIRECTION_LEFT);
				droid.setX(getWidth()+droid.getBitmap().getWidth());
			}
	}
	void sendNotif(int viewsTotals) {
		// 1-я часть
		Notification notif = new Notification(R.drawable.ic_launcher, "The value of totalView is:",
				System.currentTimeMillis());

		// 3-я часть
		Intent intent = new Intent(getContext(), MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);

		// 2-я часть
		notif.setLatestEventInfo(getContext(), "The value of ViewsTotal is: ",viewsTotals+"", pIntent);

		// ставим флаг, чтобы уведомление пропало после нажатия
		notif.flags |= Notification.FLAG_AUTO_CANCEL;

		// отправляем
		nm.notify(1, notif);
	}


}


