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
import android.view.View.OnClickListener;
import android.widget.*;
import android.util.Log;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;
import android.app.Dialog;

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
        switch(id) {
            case R.id.action_save_image:
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/dla_tree");//TODO: to settings?
                myDir.mkdirs();
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String fname = "Image-" + n + ".jpg";
                File file = new File(myDir, fname);
                Log.i(TAG, "" + file);
                if (file.exists())
                    file.delete();//sorry!
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    BitmapDrawable drawable = (BitmapDrawable) iv.getDrawable();
                    bmp = drawable.getBitmap();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.file_is_here) + ": " + file,
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.i(TAG, e.toString());
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_wrong) + ": " + e.toString(),
                            Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_settings:
//                Toast.makeText(getApplicationContext(), "Not implemented yet",
//                        Toast.LENGTH_LONG).show();
                final Dialog dialog = new Dialog(this, R.style.DialogTheme);
                dialog.setContentView(R.layout.settings_dialog);
                dialog.setTitle(R.string.action_settings);

                SeekBar sbPaletteShift = (SeekBar) dialog.findViewById(R.id.paletteShiftValue);
                sbPaletteShift.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView tvPaletteShift = (TextView) dialog.findViewById(R.id.textPaletteShift);
                        tvPaletteShift.setText(String.valueOf(seekBar.getProgress()));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                SeekBar sbPaletteCoeff = (SeekBar) dialog.findViewById(R.id.paletteCoeffValue);
                sbPaletteCoeff.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView tvPaletteCoeff = (TextView) dialog.findViewById(R.id.textPaletteCoeff);
                        tvPaletteCoeff.setText(String.valueOf(seekBar.getProgress()));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                SeekBar sbIterations = (SeekBar) dialog.findViewById(R.id.iterationsNumberValue);
                sbIterations.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView tvIterations = (TextView) dialog.findViewById(R.id.textIterations);
                        tvIterations.setText(String.valueOf(seekBar.getProgress()));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                // if button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //here we are applying settings
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;
            default:
                return true;
        }
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
                bStart.setText(R.string.start);
                bStart.setBackgroundColor(Color.parseColor("#00FF55"));
                bReset.setText(R.string.reset);
                bReset.setEnabled(true);
                break;
            case STATE_PLAY:
                bStart.setText(R.string.pause);
                bStart.setBackgroundColor(Color.parseColor("#FF0077"));
                bReset.setText(R.string.reset);
                bReset.setEnabled(false);
                break;
            case STATE_PAUSE:
                bStart.setText(R.string.resume);
                bStart.setBackgroundColor(Color.parseColor("#00FFFF"));
                bReset.setText(R.string.reset);
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
            int maxIt = 500;//TODO:  make it mutable
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
                    float i_screen = Math.round(iv.getWidth()/2 + (x-ARRAY_SIZE/2) * scale);
                    float j_screen = Math.round(iv.getHeight()/2 + (y-ARRAY_SIZE/2) * scale);
                    paint.setColor(getColorByNumber(number));
                    c.drawCircle(i_screen, j_screen, 0.75f*scale, paint);
                    iv.setImageBitmap(bmp);
                    iv.invalidate();

                    TextView tw = (TextView) findViewById(R.id.smallTextView);
                    tw.setText(getResources().getString(R.string.dots) + ": " + number);
                    //Log.i(TAG, "updateScreen() end");
                }
            });
        }


        public int getColorByNumber(int number) {
            int red, green, blue;
            double freq = 3.14159265*2/3000;//TODO:  make it changable from settings
            red   = (int) Math.round(Math.sin(freq*number + 0) * 127 + 128);
            green = (int) Math.round(Math.sin(freq*number + 2) * 127 + 128);
            blue  = (int) Math.round(Math.sin(freq*number + 4) * 127 + 128);
            return Color.rgb(red, green, blue);
        }
    };

}
