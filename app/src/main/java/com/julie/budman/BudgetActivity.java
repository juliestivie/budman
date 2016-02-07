package com.julie.budman;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BudgetActivity extends AppCompatActivity {

    protected float todayExpense = 0;
    protected int dayBudget = 20;
    protected float weekExpense = 0;
    protected int weekBudget = 140;
    protected float monthExpense = 0;
    protected int monthBudget = 600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
            // reset day expense
            this.todayExpense = 0;
            TextView text = (TextView) findViewById(R.id.today_expense_sum);
            ProgressBar bar = (ProgressBar) findViewById(R.id.day_progress_bar);
            this.updateExpense(0, 0, this.dayBudget, text, bar);
            // reset week expense
            this.weekExpense = 0;
            text = (TextView) findViewById(R.id.week_expense_sum);
            bar = (ProgressBar) findViewById(R.id.week_progress_bar);
            this.updateExpense(0, 0, this.weekBudget, text, bar);
            // reset month expense
            this.monthExpense = 0;
            text = (TextView) findViewById(R.id.month_expense_sum);
            bar = (ProgressBar) findViewById(R.id.month_progress_bar);
            this.updateExpense(0, 0, this.monthBudget, text, bar);
        }

        return super.onOptionsItemSelected(item);
    }

    protected float updateExpense(float newExpense, float oldExpense, int budget, TextView text, ProgressBar bar) {
        float expense = oldExpense + newExpense;

        // update text view
        if (text != null)
            text.setText(Float.toString(expense));

        // update status bar
        if (budget == 0)
            return expense;
        if (bar != null) {
            int status = Math.round(expense * 100 / budget);
            if (status > 100)
                status = 100;
            bar.setProgress(status);
        }

        return expense;
    }

    public void addExpense(View view) {
        float _expense = 0;
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
        editText.setText(null);

        // hide keyboard
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        // update day view
        TextView text = (TextView) findViewById(R.id.today_expense_sum);
        ProgressBar bar = (ProgressBar) findViewById(R.id.day_progress_bar);
        this.todayExpense = this.updateExpense(_expense, this.todayExpense, this.dayBudget, text, bar);

        // update week view
        text = (TextView) findViewById(R.id.week_expense_sum);
        bar = (ProgressBar) findViewById(R.id.week_progress_bar);
        this.weekExpense = this.updateExpense(_expense, this.weekExpense, this.weekBudget, text, bar);

        // update month view
        text = (TextView) findViewById(R.id.month_expense_sum);
        bar = (ProgressBar) findViewById(R.id.month_progress_bar);
        this.monthExpense = this.updateExpense(_expense, this.monthExpense, this.monthBudget, text, bar);


    }
}
