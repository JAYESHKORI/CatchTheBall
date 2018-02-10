package com.example.jayesh.catchtheball;

import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private TextView scoreLabel;
    private TextView startLabel;
    private ImageView box;
    private ImageView black;
    private ImageView orange;
    private ImageView pink;

    //Size
    private int frameHeight;
    private int boxSize;

    private int screenWidth;
    private int screenHeight;

    //position
    private int boxY;
    private int orangeX;
    private int orangeY;
    private int blackX;
    private int blackY;
    private int pinkX;
    private int pinkY;

    //Speed
    private int boxSpeed;
    private int orangeSpeed;
    private int pinkSpeed;
    private int blackSpeed;


    //Score
    private int score = 0;

    //Initialize class
    private Handler handler = new Handler();
    private Timer timer =new Timer();
    private SoundPlayer soundPlayer;

    //Status check
    private boolean action_flag = false;
    private boolean start_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundPlayer = new SoundPlayer(this);

        scoreLabel = (TextView) findViewById(R.id.scoreLabel);
        startLabel = (TextView)findViewById(R.id.startLabel);
        box = (ImageView) findViewById(R.id.box);
        black = (ImageView)findViewById(R.id.black);
        orange = (ImageView)findViewById(R.id.orange);
        pink = (ImageView) findViewById(R.id.pink);

        //Get Screen Size
        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        screenWidth = size.x;
        screenHeight = size.y;

        // Now
        // Nexus4 Width: 768 Height:1280
        // Speed Box:20 Orange:12 Pink:20 Black:16

        boxSpeed = Math.round(screenHeight / 60);  // 1280 / 60 = 21.333... => 21
        orangeSpeed = Math.round(screenWidth / 60); // 768 / 60 = 12.8 => 13
        pinkSpeed = Math.round(screenWidth / 36);   // 768 / 36 = 21.333... => 21
        blackSpeed = Math.round(screenWidth / 45); // 768 / 45 = 17.06... => 17

        //Log.v("SPEED_BOX", boxSpeed + "");
        //Log.v("SPEED_ORANGE", orangeSpeed + "");
        //Log.v("SPEED_PINK", pinkSpeed + "");
        //Log.v("SPEED_BLACK", blackSpeed + "");

        //Move to out of Screen
        orange.setX(-80);
        orange.setY(-80);
        black.setX(-80);
        black.setY(-80);
        pink.setX(-80);
        pink.setY(-80);

        scoreLabel.setText("Score : 0");
    }

    private void changePos()
    {
        hitCheck();

        //Orange
        orangeX -= orangeSpeed;
        if (orangeX < 0)
        {
            orangeX = screenWidth + 20;
            orangeY = (int)Math.floor(Math.random()*(frameHeight-orange.getHeight()));
        }
        orange.setX(orangeX);
        orange.setY(orangeY);

        //Black
        blackX -= blackSpeed;
        if (blackX < 0)
        {
            blackX = screenWidth + 10;
            blackY = (int)Math.floor(Math.random()*(frameHeight-black.getHeight()));
        }
        black.setX(blackX);
        black.setY(blackY);

        //Pink
        pinkX -= pinkSpeed;
        if (pinkX < 0)
        {
            pinkX = screenWidth + 5000;
            pinkY = (int)Math.floor(Math.random()*(frameHeight-pink.getHeight()));
        }
        pink.setX(pinkX);
        pink.setY(pinkY);



        //Move box
        if (action_flag == true)
        {
            //Touching
            boxY -= boxSpeed;
        }
        else
        {
            //Releasing
            boxY += boxSpeed;
        }

        //Check box position
        if (boxY < 0) boxY = 0;
        if (boxY > frameHeight - boxSize) boxY = frameHeight - boxSize;
        box.setY(boxY);

        scoreLabel.setText("Score : "+score);
    }

    private void hitCheck()
    {
        //If the center of the ball is in the box, it counts as a hit.

        //Orange
        int orangeCenterX = orangeX + orange.getWidth()/2;
        int orangeCenterY = orangeY + orange.getHeight()/2;

        //0 <= orangeCenterX <= boxWidth
        //boxY <= orangeCenterY <= boxY + boxHeight
        if (0 <= orangeCenterX && orangeCenterX <= boxSize &&
                boxY <= orangeCenterY && orangeCenterY <= boxY + boxSize)
        {
            score += 10;
            orangeX = -10;
            soundPlayer.playHitSound();
        }

        //Pink
        int pinkCenterX = pinkX + pink.getWidth()/2;
        int pinkCenterY = pinkY + pink.getHeight()/2;
        if (0 <= pinkCenterX && pinkCenterX <= boxSize &&
                boxY <= pinkCenterY && pinkCenterY <= boxY + boxSize)
        {
            score += 30;
            pinkX = -10;
            soundPlayer.playHitSound();
        }

        //Black
        int blackCenterX = blackX + black.getWidth()/2;
        int blackCenterY = blackY + black.getHeight()/2;
        if (0 <= blackCenterX && blackCenterX <= boxSize &&
                boxY <= blackCenterY && blackCenterY <= boxY + boxSize)
        {
            //Stop timer!!
            timer.cancel();
            timer = null;
            soundPlayer.playOverSound();

            //Show Result
            Intent intent = new Intent(getApplicationContext(),Result.class);
            intent.putExtra("score",score);
            startActivity(intent);
        }
    }

    public boolean onTouchEvent(MotionEvent me)
    {
        if(start_flag == false)
        {
            start_flag = true;
            startLabel.setVisibility(View.GONE);

            //Why get frame height and box height here?
            //Because the UI has not been set on the screen in onCreate()!
            FrameLayout frame = (FrameLayout)findViewById(R.id.frame);
            frameHeight = frame.getHeight();

            boxY = (int)box.getY();
            boxSize = box.getHeight();//box is square height and width are same

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            changePos();
                        }
                    });
                }
            },0,20);          // calling change position every 20 millisecond
        }
        else
        {
            if(me.getAction() == MotionEvent.ACTION_DOWN)
            {
                action_flag = true;
            }
            else if(me.getAction() == MotionEvent.ACTION_UP)
            {
                action_flag = false;
            }
        }

        return true;
    }

    //Disable Return Button
    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            switch (event.getKeyCode())
            {
                case KeyEvent.KEYCODE_BACK:
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
