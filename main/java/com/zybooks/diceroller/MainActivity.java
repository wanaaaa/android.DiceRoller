package com.zybooks.diceroller;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

public class MainActivity extends AppCompatActivity
        implements RollLengthDialogFragment.OnRollLengthSelectedListener

{

    public static final int MAX_DICE = 3;

    private int mVisibleDice;
    private Dice[] mDice;
    private ImageView[] mDiceImageViews;
    //////////////////////////////////////
    private Menu mMenu;
    private CountDownTimer mTimer;
    private int sumInt;
    private TextView sumTview;
    private TextView winNotTview;
    ///////////////////////////
    private long mTimerLength = 2000;
    private int mCurrentDie;
    ////////////////////////////
    private int mInitX;
    ///////////////////
    private GestureDetectorCompat mDetector;


    @Override
    public void onRollLengthClick(int which) {
        mTimerLength = 1000 * (which + 1);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sumTview = findViewById(R.id.TotalSum);
        winNotTview = findViewById(R.id.WinOrNot);
        mDetector = new GestureDetectorCompat(this, new DiceGestureListener());

        // Create an array of Dice
        mDice = new Dice[MAX_DICE];
        for (int i = 0; i < MAX_DICE; i++) {
            mDice[i] = new Dice(i + 1);
            //Log.d(TAG, "onCreate: >>>>>>>>>>>>>>>>>>>> "+ mDice[i].getNumber());
        }
        // Create an array of ImageViews
        mDiceImageViews = new ImageView[MAX_DICE];
        mDiceImageViews[0] = findViewById(R.id.dice1);
        mDiceImageViews[1] = findViewById(R.id.dice2);
        mDiceImageViews[2] = findViewById(R.id.dice3);
        //////////////////////////////////////
        for (int i = 0; i < mDiceImageViews.length; i++) {
            registerForContextMenu(mDiceImageViews[i]);
            mDiceImageViews[i].setTag(i);
        }

        // All dice are initially visible
        mVisibleDice = MAX_DICE;
        ///////////////////////////////
        mDiceImageViews[0].setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mInitX = (int) event.getX();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int x = (int) event.getX();

                    // See if movement is at least 20 pixels
                    if (Math.abs(x - mInitX) >= 20) {
                        if (x > mInitX) {
                            mDice[0].addOne();
                        }
                        else {
                            mDice[0].subtractOne();
                        }
                        showDice();
                        mInitX = x;
                    }
                    return true;
            }
            return false;
        });
        ///////////////////////////////
        showDice();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    private void showDice() {
        // Display only the number of dice visible
        int fDice = -1;
        int sDice = -1;
        int tDice = -1;
        for (int i = 0; i < mVisibleDice; i++) {
            if (i == 0) {
                sumInt = 0;
            }
            Drawable diceDrawable = ContextCompat.getDrawable(this, mDice[i].getImageId());
            mDiceImageViews[i].setImageDrawable(diceDrawable);
            mDiceImageViews[i].setContentDescription(Integer.toString(mDice[i].getNumber()));
            sumInt += mDice[i].getNumber();
            if (i == 0) fDice = mDice[i].getNumber();
            if (i == 1) sDice = mDice[i].getNumber();
            if (i == 2) tDice = mDice[i].getNumber();
            //Log.d(TAG, "showDice: ====>>>>>>>>>>" + Integer.toString(mDice[i].getNumber()));
        }
        sumTview.setText("The sum is: " +String.valueOf(sumInt));
        int winNotInt = 0;
        if (mVisibleDice == 2) {
            if (sumInt == 2 || sumInt == 7) {
                winNotInt = 1;
            } else if (sumInt == 18) {
                winNotInt = -1;
            }
        } else if ( mVisibleDice == 3) {
            if (sumInt % 7 == 0 || sumInt % 11 == 0) {
                winNotInt = 1;
            }
            else if ((fDice == 6 && sDice == 6 && tDice == 6) ||
            (fDice == 3 && sDice == 3 && tDice == 3)) {
                winNotInt = -1;
            }
        }

        if (winNotInt == -1) {
            winNotTview.setText("You lose");
        } else if (winNotInt == 0) {
            winNotTview.setText("Tie");
        } else if (winNotInt == 1) {
            winNotTview.setText("You win");
        }
        Log.d(TAG, "showDice: ********************" + sumInt);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Determine which menu option was chosen
        if (item.getItemId() == R.id.action_one) {
            changeDiceVisibility(1);
            showDice();
            return true;
        }
        else if (item.getItemId() == R.id.action_two) {
            changeDiceVisibility(2);
            showDice();
            return true;
        }
        else if (item.getItemId() == R.id.action_three) {
            changeDiceVisibility(3);
            showDice();
            return true;
        }
        else if (item.getItemId() == R.id.action_stop) {
            mTimer.cancel();
            item.setVisible(false);
            return true;
        }
        else if (item.getItemId() == R.id.action_roll) {
            rollDice();
            //Log.d(TAG, "onOptionsItemSelected: ++++++"+ firstDice);
            return true;
        }
        else if (item.getItemId() == R.id.action_roll_length) {
            RollLengthDialogFragment dialog = new RollLengthDialogFragment();
            dialog.show(getSupportFragmentManager(), "rollLengthDialog");
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void rollDice() {
        mMenu.findItem(R.id.action_stop).setVisible(true);

        if (mTimer != null) {
            mTimer.cancel();
        }

        mTimer = new CountDownTimer(mTimerLength, 100) {
            public void onTick(long millisUntilFinished) {
                for (int i = 0; i < mVisibleDice; i++) {
                    mDice[i].roll();
//                    if(i == 0) {
//                        firstDice = mDice[i].getNumber();
//                    }
                    //finalNums[i] = mDice[i].getNumber();
                    //Log.d(TAG, "onTick: >>>>>>>>>" + firstDice);
                    //Log.d(TAG, "onTick: <<<<<<<<<<" + mDice[i].getNumber());
                }
                showDice();
                //Log.d(TAG, "onTick: <<<<<<<<<<<<" + firstDice);
//                for (int mInt: finalNums) {
//                    Log.d(TAG, "onOptionsItemSelected: =========<<<>>>>"+ mInt);
//                }
            }
            public void onFinish() {
                mMenu.findItem(R.id.action_stop).setVisible(false);
            }
        }.start();
    }


    private void changeDiceVisibility(int numVisible) {
        mVisibleDice = numVisible;

        // Make dice visible
        for (int i = 0; i < numVisible; i++) {
            mDiceImageViews[i].setVisibility(View.VISIBLE);
        }

        // Hide remaining dice
        for (int i = numVisible; i < MAX_DICE; i++) {
            mDiceImageViews[i].setVisibility(View.GONE);
        }
    }
    /////////////////////////////

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        mCurrentDie = (int) v.getTag();   // Which die is selected?
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_one) {
            //mDice[0].addOne();
            mDice[mCurrentDie].addOne();

            showDice();
            return true;
        }
        else if (item.getItemId() == R.id.subtract_one) {
            //mDice[0].subtractOne();
            mDice[mCurrentDie].subtractOne();
            showDice();
            return true;
        }
        else if (item.getItemId() == R.id.roll) {
            rollDice();
            return true;
        }

        return super.onContextItemSelected(item);
    }
    ////////////////////////
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private class DiceGestureListener extends GestureDetector.SimpleOnGestureListener implements com.zybooks.diceroller.DiceGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.d(TAG, "onFling: !!!!!!!!!!!!!!!!!!!!!!! x->"+velocityX + "  Y->"+ velocityY);
            if (velocityY > 0 ) {
                rollDice();
            }
            return true;
        }

        @Override
        public boolean onDoubleTap() {
            Log.d(TAG, "onFling: !!!!!!!!!!!!!!!!!!!!!!! x->");

            return true;
        }
    }

}