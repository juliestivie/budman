package com.julie.budman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Calendar;

public class BudgetActivity extends AppCompatActivity {

    private static final String TAG = "budMan";
    protected float todayExpense = 0;
    protected int dayBudget = 20;
    protected float weekExpense = 0;
    protected int weekBudget = 140;
    protected float monthExpense = 0;
    protected int monthBudget = 600;
    protected float balance = 0;
    protected int currentDay;
    protected int currentWeek;
    protected int currentMonth;
    protected int currentYear;
    private final DateChangeReceiver dateReceiver = new DateChangeReceiver();

    public class DateChangeReceiver extends BroadcastReceiver {
        private BudgetActivity budget;

        @Override
        public void onReceive(final Context context, Intent intent) {
            Log.d("DateChangeReceiver", "Time changed");
            if (this.budget == null) {
                Log.d("DateChangeReceiver", "cannot update the date");
                return;
            }
            try {
                this.budget.checkCalendar();
                this.budget.updateView();
                this.budget.savePreferences();
            } catch (Exception e) {
                Log.e("DateChangeReceiver", "error with the date change");
            }
        }

        public void setBudgetActivity(BudgetActivity budget)  {
            this.budget = budget;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.dateReceiver.setBudgetActivity(this);

        // get the saved data
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.budman_pref_file),
                this.MODE_PRIVATE);


        EditText text = (EditText) findViewById(R.id.add_expense);
        // add a filter to avoid the user to enter a amount with more than 2 digits after the coma
        text.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,2)});
        // add a listener to the edit view
        text.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                Log.d(TAG, "key event");
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    // Perform action on key press
                    Log.d(TAG, "calling on click method");
                    addExpense(v);
                    return true;
                }
                return false;
            }
        });

        this.todayExpense = sharedPref.getFloat(getString(R.string.day_expense), 0);
        this.weekExpense = sharedPref.getFloat(getString(R.string.week_expense), 0);
        this.monthExpense = sharedPref.getFloat(getString(R.string.month_expense), 0);
        this.currentDay = sharedPref.getInt(getString(R.string.date_day), 0);
        this.currentWeek = sharedPref.getInt(getString(R.string.date_week), 0);
        this.currentMonth = sharedPref.getInt(getString(R.string.date_month), 0);
        this.currentYear = sharedPref.getInt(getString(R.string.date_year), 0);

        Log.v(TAG, "today " + this.todayExpense + " week " + this.weekExpense + " month " + this.monthExpense);
        Log.v(TAG, "day " + this.currentDay + " week " + this.currentWeek + " month " + this.currentMonth + " year " + this.currentYear);

        // check if the date changed (in this case, reset the values)
        this.checkCalendar();

        // display
        this.updateView();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // register the date receiver
        registerReceiver(this.dateReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // try to unregister the date receiver
        try {
            unregisterReceiver(this.dateReceiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. It's a known bug and is exactly what is desired.
                //Log.w(TAG,"Tried to unregister the receiver when it's not registered");
            } else {
                // unexpected, re-throw
                throw e;
            }
        }
    }

    public void checkCalendar() {
        Calendar rightNow = Calendar.getInstance();
        int _currentDay = rightNow.get(Calendar.DAY_OF_YEAR);
        int _currentWeek = rightNow.get(Calendar.WEEK_OF_YEAR);
        int _currentMonth = rightNow.get(Calendar.MONTH);
        int _currentYear = rightNow.get(Calendar.YEAR);

        if (_currentYear != this.currentYear || _currentMonth != this.currentMonth) {
            // reset everything
            this.todayExpense = 0;
            this.weekExpense = 0;
            this.monthExpense = 0;
        } else if (_currentWeek != this.currentWeek) {
            // reset day and week
            this.todayExpense = 0;
            this.weekExpense = 0;
        } else if (_currentDay != this.currentDay) {
            // reset day
            this.todayExpense = 0;
        }

        Log.v(TAG, "current date " + _currentDay + _currentWeek + _currentMonth + _currentYear);

        this.currentDay = _currentDay;
        this.currentWeek = _currentWeek;
        this.currentMonth = _currentMonth;
        this.currentYear = _currentYear;

        this.savePreferences();
    }

    public void savePreferences() {
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.budman_pref_file),
                this.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        // save the new expense values
        editor.putFloat(getString(R.string.day_expense), this.todayExpense);
        editor.putFloat(getString(R.string.week_expense), this.weekExpense);
        editor.putFloat(getString(R.string.month_expense), this.monthExpense);
        editor.putInt(getString(R.string.date_day), this.currentDay);
        editor.putInt(getString(R.string.date_week), this.currentWeek);
        editor.putInt(getString(R.string.date_month), this.currentMonth);
        editor.putInt(getString(R.string.date_year), this.currentYear);
        editor.commit();
    }

    public void updateView() {
        TextView text;
        ProgressBar bar;
        int status = 0;

        // day view
        text = (TextView) findViewById(R.id.today_expense_sum);
        text.setText(Float.toString(this.todayExpense));

        bar = (ProgressBar) findViewById(R.id.day_progress_bar);
        status = Math.round(100 * this.todayExpense / this.dayBudget);
        if (status > 100)
            status = 100;
        bar.setProgress(status);


        // update week view
        text = (TextView) findViewById(R.id.week_expense_sum);
        text.setText(Float.toString(this.weekExpense));

        bar = (ProgressBar) findViewById(R.id.week_progress_bar);
        status = Math.round(100 * this.weekExpense / this.weekBudget);
        if (status > 100)
            status = 100;
        bar.setProgress(status);

        // update month view
        text = (TextView) findViewById(R.id.month_expense_sum);
        text.setText(Float.toString(this.monthExpense));

        bar = (ProgressBar) findViewById(R.id.month_progress_bar);
        status = Math.round(100 * this.monthExpense / this.monthBudget);
        if (status > 100)
            status = 100;
        bar.setProgress(status);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_budget, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_reset) {
            // reset expenses
            this.todayExpense = 0;
            this.weekExpense = 0;
            this.monthExpense = 0;

            this.updateView();

            this.savePreferences();
        }

        return super.onOptionsItemSelected(item);
    }


    public void addExpense(View view) {
        float _expense = 0;

        this.checkCalendar();

        // get the expense
        EditText editText = (EditText) findViewById(R.id.add_expense);
        Editable edit = editText.getText();
        if (edit == null)
            return;

        String expense_string = edit.toString();
        try {
            _expense = Float.valueOf(expense_string);
        } catch (Exception e) {
            editText.setText(null);
            // hide keyboard
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            return;
        }

        // reset the edit text
        editText.setText(null);

        // hide keyboard
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        // update expenses
        this.todayExpense += _expense;
        this.weekExpense += _expense;
        this.monthExpense += _expense;

        // update view
        this.updateView();

        // save new data
        this.savePreferences();
    }
}
