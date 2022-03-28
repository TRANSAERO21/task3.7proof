package com.elseboot3909.coolbirds;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import java.util.concurrent.TimeUnit;

public class GameView extends View {

    private int viewWidth;
    private int viewHeight;

    private int points = 2;
    private int level = 1;

    private final Sprite playerBird;
    private final Sprite enemyBird;
    private final Sprite enemyBirdTap;
    private final Sprite ctlButton;
    private final Sprite coin;

    private final Bitmap b_stop;
    private final Bitmap b_play;

    private boolean isFrozen = false;
    private boolean isIgnore = false;

    private int timerInterval = 30;

    private final Timer t;

    public GameView(Context context) {
        super(context);

        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.player);
        int w = b.getWidth() / 5;
        int h = b.getHeight() / 3;
        Rect firstFrame = new Rect(0, 0, w, h);
        playerBird = new Sprite(10, 0, 0, 150, firstFrame, b);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                if (i == 2 && j == 3) {
                    continue;
                }
                playerBird.addFrame(new Rect(j * w, i * h, j * w + w, i * w + w));
            }
        }

        b = BitmapFactory.decodeResource(getResources(), R.drawable.enemy);
        w = b.getWidth() / 5;
        h = b.getHeight() / 3;
        firstFrame = new Rect(4 * w, 0, 5 * w, h);

        enemyBird = new Sprite(2000, 250, -300, 0, firstFrame, b);

        for (int i = 0; i < 3; i++) {
            for (int j = 4; j >= 0; j--) {

                if (i == 0 && j == 4) {
                    continue;
                }

                if (i == 2 && j == 0) {
                    continue;
                }

                enemyBird.addFrame(new Rect(j * w, i * h, j * w + w, i * w + w));
            }
        }

        enemyBirdTap = (Sprite) enemyBird.clone();
        enemyBirdTap.setX(5000);
        enemyBirdTap.setVelocityX(-650);

        b = BitmapFactory.decodeResource(getResources(), R.drawable.coin);
        w = b.getWidth() / 6;
        h = b.getHeight();
        firstFrame = new Rect(0, 0, w, h);
        coin = new Sprite(4000, 250, -1000, 0, firstFrame, b);
        for (int i = 1; i < 6; i++) {
            coin.addFrame(new Rect(w * i, 0, w * i + w, h));
        }

        b_stop = BitmapFactory.decodeResource(getResources(), R.drawable.pause);
        ctlButton = new Sprite(2000, 0, 0, 0,  new Rect(0, 0, b_stop.getWidth(), b_stop.getHeight()), b_stop);
        b_play = BitmapFactory.decodeResource(getResources(), R.drawable.play);

        t = new Timer();
        t.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int old_w, int old_h) {
        super.onSizeChanged(w, h, old_w, old_h);

        viewWidth = w;
        viewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawARGB(255, 141, 198, 255);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(100);
        paint.setColor(Color.RED);
        canvas.drawText( "| Points = " + points + " | Level = " + level + " |", playerBird.getFrameWidth() + 300, viewHeight - 20, paint);
        if (isFrozen) {
            paint.setTextSize(170);
            canvas.drawText( "Paused!", viewWidth / 2 - 275, viewHeight / 2, paint);
        }
        playerBird.draw(canvas);
        enemyBird.draw(canvas);
        enemyBirdTap.draw(canvas);
        ctlButton.draw(canvas);
        coin.draw(canvas);
        if (points >= 100) {
            level++;
            points = 0;
            if (timerInterval > 10) {
                timerInterval--;
            }
            enemyBird.setVelocityX(enemyBird.getVelocityX() - 50);
            enemyBirdTap.setVelocityX(enemyBirdTap.getVelocityX() - 25);
            if (playerBird.getVelocityY() > 0) {
                playerBird.setVelocityY(playerBird.getVelocityY() + 25);
            } else {
                playerBird.setVelocityY(playerBird.getVelocityY() - 25);
            }
            invalidate();
        }
        if (points <= -100) {
            paint.setTextSize(170);
            canvas.drawText( "WASTED", viewWidth / 2 - 275, viewHeight / 2, paint);
            isIgnore = true;
            t.cancel();
            new Thread(() -> {
                waitTime();
                Activity activity = (Activity)getContext();
                activity.finish();
                activity.startActivity(activity.getIntent());
            }).start();
        }
    }

    protected void update() {
        playerBird.update(timerInterval);
        enemyBird.update(timerInterval);
        enemyBirdTap.update(timerInterval);
        coin.update(timerInterval);
        if (playerBird.getY() + playerBird.getFrameHeight() > viewHeight) {
            playerBird.setY(viewHeight - playerBird.getFrameHeight());
            playerBird.setVelocityY(-playerBird.getVelocityY());
            points--;
        } else if (playerBird.getY() < 0) {
            playerBird.setY(0);
            playerBird.setVelocityY(-playerBird.getVelocityY());
            points--;
        }
        if (enemyBird.getX() < -enemyBird.getFrameWidth()) {
            enemyBird.teleportEnemy(0, viewWidth, viewHeight);
            points += 15;
        }
        if (coin.intersect(playerBird)) {
            coin.teleportEnemy((int) Math.pow(10, 4), viewWidth, viewHeight);
            points += 30;
        }
        if (coin.getX() < - coin.getFrameWidth()) {
            coin.teleportEnemy((int) Math.pow(10, 4), viewWidth, viewHeight);
        }
        if (enemyBirdTap.getX() < -enemyBirdTap.getFrameWidth()) {
            enemyBirdTap.teleportEnemy(4000, viewWidth, viewHeight);
            points -= 20;
        }
        if (enemyBird.intersect(playerBird)) {
            enemyBird.teleportEnemy(0, viewWidth, viewHeight);
            points -= 35;
        }
        invalidate();
    }

    class Timer extends CountDownTimer {
        public Timer() {
            super(Integer.MAX_VALUE, timerInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (!isFrozen) {
                update();
            }
        }

        @Override
        public void onFinish() {
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isIgnore) return true;
        int eventAction = event.getAction();
        if (eventAction == MotionEvent.ACTION_DOWN) {
            if (event.getX() >= ctlButton.getX() && event.getX() <= (ctlButton.getX() + ctlButton.getFrameWidth())
                    && event.getY() >= ctlButton.getY() && event.getY() <= (ctlButton.getY() + ctlButton.getFrameHeight())) {
                if (isFrozen) {
                    ctlButton.setBitmap(b_stop);
                } else {
                    ctlButton.setBitmap(b_play);
                }
                isFrozen = !isFrozen;
                invalidate();
            } else if (event.getX() >= enemyBirdTap.getX() && event.getX() <= (enemyBirdTap.getX() + enemyBirdTap.getFrameWidth())
                    && event.getY() >= enemyBirdTap.getY() && event.getY() <= (enemyBirdTap.getY() + enemyBirdTap.getFrameHeight())) {
                enemyBirdTap.teleportEnemy(4000, viewWidth, viewHeight);
                points += 10;
            } else if (!isFrozen) {
                playerBird.setVelocityY(-playerBird.getVelocityY());
                points--;
            }
        }
        return true;
    }

    private void waitTime() {
        try
        {
            TimeUnit.MILLISECONDS.sleep(2000);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

}
