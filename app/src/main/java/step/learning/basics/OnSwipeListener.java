package step.learning.basics;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class OnSwipeListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;

    public OnSwipeListener(Context context){
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }

    private final class GestureListener extends android.view.GestureDetector.SimpleOnGestureListener{
        private static final int MIN_SWIPE_DISTANCE = 100;
        private static final int MIN_SWIPE_VELOCITY = 100;

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            float dx = e2.getX() - e1.getX();
            float dy = e2.getY() - e1.getY();

            if(Math.abs(dx) > Math.abs(dy)){
                if(Math.abs(dx) >= MIN_SWIPE_DISTANCE
                && Math.abs(velocityX) >= MIN_SWIPE_VELOCITY){
                    if(dx > 0) OnSwipeRight();
                    else  OnSwipeLeft();
                    result = true;
                }
            }
            else {
                if(Math.abs(dy) >= MIN_SWIPE_DISTANCE
                && Math.abs(velocityY) >= MIN_SWIPE_VELOCITY){
                    if(dy > 0) OnSwipeBottom();
                    else OnSwipeTop();
                    result = true;
                }
            }
            return result;
        }
    }

    public void OnSwipeRight(){}
    public void OnSwipeLeft(){}
    public void OnSwipeTop(){}
    public void OnSwipeBottom(){}
}
