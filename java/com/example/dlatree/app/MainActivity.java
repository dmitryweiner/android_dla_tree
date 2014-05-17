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
    static final int ARRAY_SIZE = 300;

    private volatile Bitmap bmp = null;
    private volatile boolean stopFlag = false;
    private volatile boolean isRunning = false;
    private volatile boolean isFinishedNormally = true;
    private volatile int[][] dots = new int[ARRAY_SIZE][ARRAY_SIZE];
    private static String TAG = "dla_tree";
    private Handler myHandler = null;
    private float scale = 0;

    private ImageView iv = null;
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
                currentState = STATE_STOP;
                initState();
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
            if (currentState == STATE_PLAY) {
                stopFlag = false;
                Thread myThread = new Thread(drawImage);
                myThread.start();
                Log.i(TAG, "myThread.start();");
            }
            scale = (float) Math.min(iv.getHeight(), iv.getWidth()) / ARRAY_SIZE;
            Log.i(TAG, "scale = " + scale);
        }
        Log.i(TAG, "onWindowFocusChanged() fired");
    }

    public void initState() {
        bmp = Bitmap.createBitmap(iv.getWidth(), iv.getHeight(), Bitmap.Config.ARGB_8888);
        bmp.eraseColor(Color.rgb(0, 0, 0));
        iv.setImageBitmap(bmp);
        iv.invalidate();
        rm = 1;
        currentDotIndex = 0;
        dots = new int[ARRAY_SIZE][ARRAY_SIZE];
    }

    public void updateButtonsState() {
        Button bReset = (Button) findViewById(R.id.buttonReset);
        Button bStart = (Button) findViewById(R.id.buttonStart);
        switch (currentState) {
            case STATE_STOP:
                bStart.setText("Start");
                bStart.setBackgroundColor(Color.parseColor("#00FF55"));
                bReset.setText("Reset");
                bReset.setEnabled(true);
                break;
            case STATE_PLAY:
                bStart.setText("Pause");
                bStart.setBackgroundColor(Color.parseColor("#FF0077"));
                bReset.setText("Reset");
                bReset.setEnabled(false);
                break;
            case STATE_PAUSE:
                bStart.setText("Resume");
                bStart.setBackgroundColor(Color.parseColor("#00FFFF"));
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
            int x, y;
            int xc, yc;
            int xn, yn;
            int maxIt = 500;
            int rmax;
            double a;

            xc = (ARRAY_SIZE - 1) / 2;
            yc = (ARRAY_SIZE - 1) / 2;
            rmax = Math.min(xc, yc) - 1;
            if (dots[xc][yc] == 0) {
                dots[xc][yc] = 1;
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
                    if (x < 0 || x > (ARRAY_SIZE - 1) ||
                        y < 0 || y > (ARRAY_SIZE - 1)) {
                        break;
                    }
                    if (dots[x][y] == 0) {
                        //check the neighbors
                        for (byte k = 0; k < 8; k++){
                            xn = x + nx[k];
                            yn = y + ny[k];
                            if (    xn >= 0 && yn >= 0 &&
                                    xn < ARRAY_SIZE && yn < ARRAY_SIZE &&
                                    (dots[xn][yn] != 0)) {

                                dots[x][y] = currentDotIndex;
                                drawDot(x, y, currentDotIndex);
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

        public void drawDot(final int x, final int y, final int number){
            myHandler.post(new Runnable() {
                public void run() {
                    //Log.i(TAG, "updateScreen() begin");
                    BitmapDrawable drawable = (BitmapDrawable) iv.getDrawable();
                    if (drawable != null) {
                        bmp = drawable.getBitmap();
                    } else {
                        bmp = Bitmap.createBitmap(iv.getWidth(), iv.getHeight(), Bitmap.Config.ARGB_8888);
                    }
                    Canvas c = new Canvas(bmp);
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    int i_screen = Math.round(iv.getWidth()/2 + (x-ARRAY_SIZE/2) * scale);
                    int j_screen = Math.round(iv.getHeight()/2 + (y-ARRAY_SIZE/2) * scale);
                    paint.setColor(getColorByNumber(number));
                    c.drawCircle(i_screen, j_screen, 0.75f*scale, paint);
                    iv.setImageBitmap(bmp);
                    iv.invalidate();

                    TextView tw = (TextView) findViewById(R.id.smallTextView);
                    tw.setText("Dots: " + number);
                    //Log.i(TAG, "updateScreen() end");
                }
            });
        }


        public int getColorByNumber(int number) {
            int red, green, blue;
            double freq = 3.14159265*2/3000;
            red   = (int) Math.round(Math.sin(freq*number + 0) * 127 + 128);
            green = (int) Math.round(Math.sin(freq*number + 2) * 127 + 128);
            blue  = (int) Math.round(Math.sin(freq*number + 4) * 127 + 128);
            return Color.rgb(red, green, blue);
        }
    };

}
