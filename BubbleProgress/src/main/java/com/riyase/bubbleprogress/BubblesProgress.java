package com.riyase.bubbleprogress;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by riyase on 14/5/15.
 */
public class BubblesProgress extends View {

    private static final String TAG = "ProgressIndefinite";

    int bubbleCount = 5;
    float bubbleSidePadding = 15;
    Paint bubblePaint;
    int bubbleColors[];

    float bubbleDiameter;
    boolean animRunning = false;
    private ObjectAnimator startAngleAnimator;
    private long animDuration =1000;
    private int animDirection = ANIM_L2R;
    private static final int ANIM_L2R = 0;
    private static final int ANIM_R2L = 1;
    private int animRepeatCount = Animation.INFINITE;

    ///////////////
    float  bubbleRadiusMax;
    float offset;
    float bubbleWidth;
    ///////////////

    Interpolator mInterpolator;

    public BubblesProgress(Context context) {
        super(context, null);
    }

    public BubblesProgress(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context,attrs,0);
    }

    public void init(Context context, AttributeSet attrs, int defStyleAttr) {//BubblesProgressIndefinite(Context context, AttributeSet attrs, int defStyleAttr) {
        //super(context, attrs, defStyleAttr);
        TypedArray a       = context.obtainStyledAttributes(attrs, R.styleable.BubblesProgressIndefinite, defStyleAttr, 0);
        bubbleCount        = a.getInt(R.styleable.BubblesProgressIndefinite_bpi_bubble_count, 5);
        bubbleSidePadding  = a.getDimension(R.styleable.BubblesProgressIndefinite_bpi_bubble_side_padding, 15);
        animDuration       = a.getInt(R.styleable.BubblesProgressIndefinite_bpi_anim_duration, 1000);
        animRepeatCount    = a.getInt(R.styleable.BubblesProgressIndefinite_bpi_anim_repeat_count, Animation.INFINITE);
        animDirection      = a.getInt(R.styleable.BubblesProgressIndefinite_bpi_anim_direction, ANIM_L2R);
        bubblePaint        = new Paint();
        bubblePaint.setAntiAlias(true);
        bubbleColors       = new int[] {Color.RED,Color.GREEN,Color.MAGENTA};
        mInterpolator      = new LinearInterpolator();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(animRunning == false && w > 0 && h > 0) {
            animateBubbles( w, h );
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawRect(offset,0,offset+bubbleWidth*bubbleCount-bubbleSidePadding,bubbleRadiusMax * 2,new Paint());
        for ( int i=0; i < bubbleCount; i++ ) {
            int x = (int) ( offset + ( i * bubbleWidth ) + bubbleRadiusMax );
            int y = getHeight() / 2;
            //canvas.drawLine(x-(getHeight()/2)-bubbleSidePadding,y-(getHeight()/2),x-(getHeight()/2)-bubbleSidePadding,y+(getHeight()/2),bubblePaint);
            bubblePaint.setColor( bubbleColors[ i % bubbleColors.length] );
            float drawRadius = getDrawRadius( i, bubbleDiameter, bubbleCount);
            canvas.drawCircle( x, y, drawRadius, bubblePaint );
        }
    }

    private float getDrawRadius( int whichBubble, float diameter, int bubbleCount ) {
        float diffRadius = (( bubbleRadiusMax * 2.0f ) / bubbleCount ) / 2.0f ;
        float radius = ( whichBubble * diffRadius ) + diameter;

        /* in this logic, for param 'diameter'
        '0 to radius'(zero to half) indiactes bubble is increasing its size
        'radius to diameter' (half to full) indiactes bubble is decreasing its size*/
        if( radius >= bubbleRadiusMax * 2.0f ) {
            radius = radius - ( bubbleRadiusMax * 2.0f );
        }

        if( radius > bubbleRadiusMax ) {
            radius = bubbleRadiusMax - ( radius % bubbleRadiusMax );
        }
        return radius;
    }


    private void animateBubbles() {
        animateBubbles(getWidth(), getHeight());
    }

    private void animateBubbles(int width, int height) {
        int bubbleWidthMax  = ( width - ( getPaddingLeft() + getPaddingRight())) / bubbleCount;
        int bubbleHeightMax = height - (getPaddingTop() + getPaddingBottom());
        Log.i(TAG,"bubbleWidthMax:"+bubbleWidthMax);
        Log.i(TAG,"bubbleHeightMax:"+bubbleHeightMax);

        bubbleRadiusMax  = Math.min( bubbleWidthMax / 2 , bubbleHeightMax / 2 );
        bubbleWidth = (int) (( bubbleRadiusMax * 2 ) + ( bubbleSidePadding * 2 ));
        offset = ( width - ( bubbleWidth * bubbleCount )) / 2 ;
        offset += bubbleSidePadding;

        if ( animDirection == ANIM_L2R ) {
            startAngleAnimator = ObjectAnimator.ofFloat( this, "bubbleDiameter", bubbleRadiusMax * 2.0f, 1.0f );
        } else {
            startAngleAnimator = ObjectAnimator.ofFloat( this, "bubbleDiameter", 1.0f, bubbleRadiusMax * 2.0f );
        }
        startAngleAnimator.setDuration(animDuration );
        startAngleAnimator.setRepeatMode(ObjectAnimator.RESTART );
        startAngleAnimator.setRepeatCount(animRepeatCount );
        startAngleAnimator.setInterpolator(mInterpolator );
        startAngleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        } );
        startAngleAnimator.start();
        animRunning = true;
    }

    @SuppressWarnings("used")
    private void setBubbleDiameter( float diameter ) {
        bubbleDiameter = diameter;
    }

    public void setInterpolator( Interpolator interpolator ) {
        mInterpolator = interpolator;
        startAngleAnimator.cancel();
        animateBubbles();
    }

    public void setAnimDuration( long duration ) {
         this.animDuration = duration;
    }

    public void setBubbleColors( int... colors ) {
        if(colors.length != 0) {
            bubbleColors = colors;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            if ( startAngleAnimator != null ) {
                startAngleAnimator.cancel();
            }
        } catch ( Exception e ) {
            Log.e( "Exception", "cancel Animation", e );
        }
    }

}
