package com.example.dlatree.app;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.view.View;
import android.widget.*;
import android.util.Log;


public class MainActivity
    extends ActionBarActivity
    implements View.OnClickListener {

    static final int STATE_STOP = 0;
    static final int STATE_PLAY = 1;
    static final int STATE_PAUSE = 2;

    private volatile Bitmap bmp = null;
    private ImageView iv = null;
    private volatile boolean stopFlag = false;
    private volatile boolean isRunning = false;
    private volatile boolean isFinishedNormally = true;
    private static String TAG = "dla_tree";
    private Handler myHandler = null;

    private volatile double rm = 1;
    private volatile int currentDotIndex = 0;
    private volatile int currentState = STATE_STOP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myHandler = new Handler();
        setContentView(R.layout.activity_main);
        iv = (ImageView) findViewById(R.id.imageView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.i(TAG, "App started");
    }
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.i(TAG, "onResume() fired");
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        stopFlag = true;
        Log.i(TAG, "onPause() fired");
    }

    @Override
    public void onStop() {
        super.onStop();  // Always call the superclass method first
        Log.i(TAG, "onStop() fired");
    }

    @Override
    public void onStart() {
        super.onStart();  // Always call the superclass method first
        Log.i(TAG, "onStart() fired");
    }

    @Override
    public void onRestart() {
        super.onRestart();  // Always call the superclass method first
        Log.i(TAG, "onRestart() fired");
    }

    @Override
    public void onClick(View v) {
        Button b = (Button)v;
        Button bReset = (Button) findViewById(R.id.buttonReset);
        Button bStart = (Button) findViewById(R.id.buttonStart);
        switch(v.getId()) {
            case R.id.buttonStart:
                if (isRunning) { //pressed "Pause"
                    stopFlag = true;
                    currentState = STATE_PAUSE;
                    updateButtonsState();
                } else { //pressed "Start" or "Resume"
                    if (isFinishedNormally) {
                        initState();
                    }
                    stopFlag = false;
                    Thread myThread = new Thread(drawImage);
                    myThread.start();
                    currentState = STATE_PLAY;
                    updateButtonsState();
                }
                break;
            case R.id.buttonReset:
                initState();
                currentState = STATE_STOP;
                updateButtonsState();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            BitmapDrawable drawable = (BitmapDrawable) iv.getDrawable();
            if (drawable != null) {
                bmp = drawable.getBitmap();
            } else {
                bmp = Bitmap.createBitmap(iv.getWidth(), iv.getHeight(), Bitmap.Config.ARGB_8888);
            }
            if (currentState == STATE_PLAY) {
                stopFlag = false;
                Thread myThread = new Thread(drawImage);
                myThread.start();
            }
        }
        Log.i(TAG, "onWindowFocusChanged() fired");
    }

    public void initState() {
        bmp.eraseColor(Color.rgb(0, 0, 0));
        iv.invalidate();
        rm = 1;
        currentDotIndex = 0;
    }

    public void updateButtonsState() {
        Button bReset = (Button) findViewById(R.id.buttonReset);
        Button bStart = (Button) findViewById(R.id.buttonStart);
        switch (currentState) {
            case STATE_STOP:
                bStart.setText("Start");
                bReset.setText("Reset");
                bReset.setEnabled(true);
                break;
            case STATE_PLAY:
                bStart.setText("Pause");
                bReset.setText("Reset");
                bReset.setEnabled(false);
                break;
            case STATE_PAUSE:
                bStart.setText("Resume");
                bReset.setText("Reset");
                bReset.setEnabled(true);
                break;
        }
    }

    Runnable drawImage = new Runnable() {
        byte nx[] = {-1, -1, 0, 1, 1, 1, 0, -1};
        byte ny[] = {0, 1, 1, 1, 0, -1, -1, -1};

        @Override
        public void run() {
            Log.i(TAG, "Thread started");
            isRunning = true;
            if (bmp != null) {
                int x, y;
                int xc, yc;
                int xn, yn;
                int maxIt = 400;
                int rmax;
                double a;

                xc = (bmp.getWidth() - 1) / 2;
                yc = (bmp.getHeight() - 1) / 2;
                rmax = Math.min(xc, yc) - 1;
                if (bmp.getPixel(xc, yc) == Color.rgb(0, 0, 0)) {
                    bmp.setPixel(xc, yc, Color.rgb(255, 255, 255));
                    Log.i(TAG, "Initial pixel at " + xc + "," + yc);
                }
                isFinishedNormally = false;
                while (rm < rmax && !stopFlag) {
                    boolean flag = false;
                    a = 3.14159265 * 2 * Math.random();
                    x = (int) (xc + rm * Math.cos(a));
                    y = (int) (yc + rm * Math.sin(a));
                    for (int i = 0; i < maxIt; i++){
                        byte rand = (byte) (Math.random() * 8);
                        x = x + nx[rand];
                        y = y + ny[rand];
                        if (x < 0 || x > (bmp.getWidth() - 1) ||
                            y < 0 || y > (bmp.getHeight() - 1)) {
                            break;
                        }
                        if (bmp.getPixel(x, y) == Color.rgb(0, 0, 0)) {
                            //check the neighbors
                            for (byte k = 0; k < 8; k++){
                                xn = x + nx[k];
                                yn = y + ny[k];
                                if (    xn >= 0 && yn >= 0 &&
                                        xn < bmp.getWidth() && yn < bmp.getHeight() &&
                                        (bmp.getPixel(xn, yn) != Color.rgb(0, 0, 0))) {

                                    int color = getColorByNumber(currentDotIndex);
                                    bmp.setPixel(x, y, color);
                                    if (currentDotIndex % 50 == 0) {
                                        //Log.i(TAG, "currentDotIndex = " + currentDotIndex);
                                        updateScreen();
                                    }
                                    currentDotIndex++;

                                    double r;
                                    r = Math.sqrt((x - xc) * (x - xc) + (y - yc) * (y - yc));
                                    if (r > rm) {
                                        rm = r;
                                    }
                                    flag = true;
                                    break;
                                }
                            }
                        }//if not on screen
                        if (flag) {
                            break;
                        }
                    }//for
                }//while(r<rmax)
                isFinishedNormally = !stopFlag;
                updateScreen();
            }
            if (isFinishedNormally) {
                updateButtons();
                currentState = STATE_STOP;
            }
            isRunning = false;
            Log.i(TAG, "Thread finished");
        }

        public void updateButtons(){
            myHandler.post(new Runnable() {
                public void run() {
                    updateButtonsState();
                }
            });
        }

        public void updateScreen(){
            myHandler.post(new Runnable() {
                public void run() {
                    //Log.i(TAG, "updateScreen() begin");
                    iv.setImageBitmap(bmp);
                    iv.invalidate();
                    //Log.i(TAG, "updateScreen() end");
                }
            });
        }

        public int getColorByNumber(int number) {
            int red, green, blue;
            double freq = 3.14159265*2/1400;
            red   = (int) Math.round(Math.sin(freq*number + 0) * 127 + 128);
            green = (int) Math.round(Math.sin(freq*number + 2) * 127 + 128);
            blue  = (int) Math.round(Math.sin(freq*number + 4) * 127 + 128);
            return Color.rgb(red, green, blue);
        }
    };

}
