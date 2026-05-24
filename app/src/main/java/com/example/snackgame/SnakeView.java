package com.example.snackgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SnakeView extends View {
    private static final int COLS = 24;
    private static final int ROWS = 32;
    private static final int START_LENGTH = 5;
    private static final long FRAME_MS = 120L;
    private static final float SWIPE_THRESHOLD = 40f;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Deque<Point> snake = new ArrayDeque<>();
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            update();
            invalidate();
            if (running) {
                handler.postDelayed(this, FRAME_MS);
            }
        }
    };

    private Point food = new Point();
    private int direction = Direction.RIGHT;
    private int nextDirection = Direction.RIGHT;
    private int score = 0;
    private boolean running = false;
    private boolean gameOver = false;
    private float downX;
    private float downY;

    public SnakeView(Context context) {
        super(context);
        setFocusable(true);
        resetGame();
    }

    public void pause() {
        running = false;
        handler.removeCallbacks(tick);
    }

    public void resume() {
        if (!gameOver && !running) {
            running = true;
            handler.postDelayed(tick, FRAME_MS);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        float cell = Math.min(width / (float) COLS, (height - 140f) / ROWS);
        float boardWidth = cell * COLS;
        float boardHeight = cell * ROWS;
        float left = (width - boardWidth) / 2f;
        float top = 110f;

        canvas.drawColor(Color.rgb(12, 22, 18));

        paint.setColor(Color.rgb(232, 245, 233));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(42f);
        canvas.drawText("Score " + score, left, 64f, paint);

        paint.setColor(Color.rgb(24, 43, 34));
        canvas.drawRoundRect(left, top, left + boardWidth, top + boardHeight, 18f, 18f, paint);

        paint.setColor(Color.rgb(255, 203, 71));
        drawCell(canvas, left, top, cell, food.x, food.y, 0.5f);

        paint.setColor(Color.rgb(76, 209, 122));
        for (Point part : snake) {
            drawCell(canvas, left, top, cell, part.x, part.y, 0.18f);
        }

        Point head = snake.peekFirst();
        if (head != null) {
            paint.setColor(Color.rgb(178, 255, 191));
            drawCell(canvas, left, top, cell, head.x, head.y, 0.25f);
        }

        if (gameOver) {
            paint.setColor(Color.argb(190, 0, 0, 0));
            canvas.drawRect(0, 0, width, height, paint);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.WHITE);
            paint.setTextSize(54f);
            canvas.drawText("Game Over", width / 2f, height / 2f - 24f, paint);
            paint.setTextSize(30f);
            canvas.drawText("Tap to restart", width / 2f, height / 2f + 34f, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                if (gameOver) {
                    resetGame();
                    resume();
                    invalidate();
                    return true;
                }
                handleSwipe(event.getX() - downX, event.getY() - downY);
                return true;
            default:
                return true;
        }
    }

    private void update() {
        if (gameOver) {
            return;
        }

        direction = nextDirection;
        Point head = snake.peekFirst();
        if (head == null) {
            return;
        }

        Point next = new Point(head.x, head.y);
        if (direction == Direction.UP) {
            next.y--;
        } else if (direction == Direction.DOWN) {
            next.y++;
        } else if (direction == Direction.LEFT) {
            next.x--;
        } else if (direction == Direction.RIGHT) {
            next.x++;
        }

        if (next.x < 0 || next.x >= COLS || next.y < 0 || next.y >= ROWS || hitsSnake(next)) {
            gameOver = true;
            running = false;
            return;
        }

        snake.addFirst(next);
        if (next.equals(food)) {
            score++;
            placeFood();
        } else {
            snake.removeLast();
        }
    }

    private void resetGame() {
        handler.removeCallbacks(tick);
        snake.clear();
        int startX = COLS / 2;
        int startY = ROWS / 2;
        for (int i = 0; i < START_LENGTH; i++) {
            snake.addLast(new Point(startX - i, startY));
        }
        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        score = 0;
        gameOver = false;
        placeFood();
    }

    private void placeFood() {
        Set<Point> occupied = new HashSet<>(snake);
        Point candidate;
        do {
            candidate = new Point(random.nextInt(COLS), random.nextInt(ROWS));
        } while (occupied.contains(candidate));
        food = candidate;
    }

    private boolean hitsSnake(Point point) {
        for (Point part : snake) {
            if (part.equals(point)) {
                return true;
            }
        }
        return false;
    }

    private void handleSwipe(float dx, float dy) {
        if (Math.abs(dx) < SWIPE_THRESHOLD && Math.abs(dy) < SWIPE_THRESHOLD) {
            return;
        }
        if (Math.abs(dx) > Math.abs(dy)) {
            setDirection(dx > 0 ? Direction.RIGHT : Direction.LEFT);
        } else {
            setDirection(dy > 0 ? Direction.DOWN : Direction.UP);
        }
    }

    private void setDirection(int requestedDirection) {
        if ((direction == Direction.UP && requestedDirection == Direction.DOWN)
                || (direction == Direction.DOWN && requestedDirection == Direction.UP)
                || (direction == Direction.LEFT && requestedDirection == Direction.RIGHT)
                || (direction == Direction.RIGHT && requestedDirection == Direction.LEFT)) {
            return;
        }
        nextDirection = requestedDirection;
    }

    private void drawCell(Canvas canvas, float left, float top, float cell, int x, int y, float insetRatio) {
        float inset = cell * insetRatio;
        float cellLeft = left + x * cell + inset;
        float cellTop = top + y * cell + inset;
        float cellRight = left + (x + 1) * cell - inset;
        float cellBottom = top + (y + 1) * cell - inset;
        canvas.drawRoundRect(cellLeft, cellTop, cellRight, cellBottom, 7f, 7f, paint);
    }

    private static final class Direction {
        static final int UP = 0;
        static final int RIGHT = 1;
        static final int DOWN = 2;
        static final int LEFT = 3;
    }
}
