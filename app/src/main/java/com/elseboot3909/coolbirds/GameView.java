package com.elseboot3909.coolbirds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends View {

    private int viewWidth;
    private int viewHeight;

    private int points = 0;

    private Sprite playerBird;
    private Sprite enemyBird;


    private final int timerInterval = 85;

    public GameView(Context context) {
        super(context);

        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.good_bird);
        int w = b.getWidth()/4;
        int h = b.getHeight();

        Rect firstFrame = new Rect(0, 0, w, h);

        playerBird = new Sprite(0, 0, 0, 350, firstFrame, b);

        for (int j = 0; j < 3; j++) {
            playerBird.addFrame(new Rect(j * w, 0, j * w + w, w));
        }

        b = BitmapFactory.decodeResource(getResources(), R.drawable.bad_birds);
        w = b.getWidth()/4;
        h = b.getHeight();

        firstFrame = new Rect(0, 0, w, h);

        enemyBird = new Sprite(2000, 250, -800, 0, firstFrame, b);

        for (int j = 0; j < 3; j++) {
            enemyBird.addFrame(new Rect(j * w, 0, j * w + w, w));
        }

        Timer t = new Timer();
        t.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewWidth = w;
        viewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawARGB(255, 141, 198, 255);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(55.0f);
        paint.setColor(Color.WHITE);
        canvas.drawText(points + " ", viewWidth - 100, 70, paint);
        playerBird.draw(canvas);
        enemyBird.draw(canvas);
    }

    protected void update() {
        playerBird.update(timerInterval);
        enemyBird.update(timerInterval);
        if (playerBird.getY() + playerBird.getFrameHeight() > viewHeight) {
            playerBird.setY(viewHeight - playerBird.getFrameHeight());
            playerBird.setVelocityY(-playerBird.getVelocityY());
            points--;
        } else if (playerBird.getY() < 0) {
            playerBird.setY(0);
            playerBird.setVelocityY(-playerBird.getVelocityY());
            points--;
        }
        if (enemyBird.getX() < - enemyBird.getFrameWidth()) {
            teleportEnemy();
            points +=10;
        }
        if (enemyBird.intersect(playerBird)) {
            teleportEnemy();
            points -= 40;
        }
        invalidate();
    }

    class Timer extends CountDownTimer {
        public Timer() {
            super(Integer.MAX_VALUE, timerInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            update();
        }

        @Override
        public void onFinish() {
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();
        if (eventAction == MotionEvent.ACTION_DOWN)  {
            playerBird.setVelocityY(playerBird.getVelocityY() * -1);
            points--;
        }
        return true;
    }

    private void teleportEnemy() {
        enemyBird.setX(viewWidth + Math.random() * 500);
        enemyBird.setY(Math.random() * (viewHeight - enemyBird.getFrameHeight()));
    }

}
