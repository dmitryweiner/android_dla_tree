package com.example.dlatree.app;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.graphics.*;
import android.view.*;
import android.view.View;
import android.widget.*;
import android.util.Log;
import java.util.Random;


public class MainActivity
    extends ActionBarActivity
    implements View.OnClickListener {

    private volatile Bitmap bmp = null;
    private ImageView iv = null;
    private volatile boolean stopFlag = false;
    private int dots[][];
    private static String TAG = "dla_tree";
    private Handler myHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = (ImageView) findViewById(R.id.imageView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.i(TAG, "App started");
    }

    @Override
    public void onClick(View v) {
        Button b = (Button)v;
        switch(v.getId()) {
            case R.id.buttonStart:
                myHandler = new Handler();
                Thread myThread = new Thread(drawImage);
                myThread.start();
                bmp.eraseColor(Color.rgb(0, 0, 0));
                iv.invalidate();
                stopFlag = false;
                b.setEnabled(false);
                b.setText("Running");
                break;
            case R.id.buttonStop:
                stopFlag = true;
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
        bmp = Bitmap.createBitmap( iv.getWidth(), iv.getHeight(), Bitmap.Config.ARGB_8888);
    }

    Runnable drawImage = new Runnable() {
        byte nx[] = {-1, -1, 0, 1, 1, 1, 0, -1};
        byte ny[] = {0, 1, 1, 1, 0, -1, -1, -1};

        @Override
        public void run() {
            Log.i(TAG, "Thread started");
            if (bmp != null) {
                int x, y;
                int xc, yc;
                int xn, yn;
                int maxIt = 300;
                int rmax;
                int currentDotIndex = 0;
                double a;
                double rm = 1;

                bmp.eraseColor(Color.rgb(0, 0, 0));
                xc = (bmp.getWidth() - 1) / 2;
                yc = (bmp.getHeight() - 1) / 2;
                rmax = Math.min(xc, yc) - 1;
                bmp.setPixel(xc, yc, Color.rgb(255, 255, 255));
                Log.i(TAG, "Initial pixel at " + xc + "," + yc);
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
                                        Log.i(TAG, "currentDotIndex = " + currentDotIndex);
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
                updateScreen();
                //Canvas c = new Canvas(bmp);
                //Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                //paint.setColor(Color.rgb(0, 255, 0));
                //c.drawRect(10, 10, 20, 20, paint);
                //bmp.setPixel(10, 10, Color.rgb(0, 255, 0));
            }
            updateButtons();
            Log.i(TAG, "Thread finished");
        }

        public void updateButtons(){
            myHandler.post(new Runnable() {
                public void run() {
                    Button bStart = (Button) findViewById(R.id.buttonStart);
                    bStart.setText("Start");
                    bStart.setEnabled(true);
                }
            });
        }

        public void updateScreen(){
            myHandler.post(new Runnable() {
                public void run() {
                    Log.i(TAG, "runOnUiThread begin");
                    iv.setImageBitmap(bmp);
                    iv.invalidate();
                    Log.i(TAG, "runOnUiThread end");
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
