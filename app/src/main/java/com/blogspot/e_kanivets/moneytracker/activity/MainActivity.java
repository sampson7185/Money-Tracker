package com.blogspot.e_kanivets.moneytracker.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.pm.FeatureInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.blogspot.e_kanivets.moneytracker.R;
import com.blogspot.e_kanivets.moneytracker.adapter.RecordAdapter;
import com.blogspot.e_kanivets.moneytracker.helper.DBHelper;
import com.blogspot.e_kanivets.moneytracker.helper.MTHelper;
import com.blogspot.e_kanivets.moneytracker.model.Record;
import com.blogspot.e_kanivets.moneytracker.ui.AddExpenseDialog;
import com.blogspot.e_kanivets.moneytracker.ui.AddIncomeDialog;
import com.blogspot.e_kanivets.moneytracker.ui.ChangeDateDialog;
import com.blogspot.e_kanivets.moneytracker.util.Constants;
import com.blogspot.e_kanivets.moneytracker.util.MTApp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class MainActivity extends Activity implements Observer{

    private Activity activity;

    private ListView listView;

    private Button btnAddIncome;
    private Button btnAddExpense;
    private Button btnReport;
    private TextView tvFromDate;
    private TextView tvToDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        activity = this;

        //Link views
        btnAddIncome = (Button) findViewById(R.id.btn_add_income);
        btnAddExpense = (Button) findViewById(R.id.btn_add_expense);
        btnReport = (Button) findViewById(R.id.btn_report);

        tvFromDate = (TextView) findViewById(R.id.tv_from_date);
        tvToDate = (TextView) findViewById(R.id.tv_to_date);

        listView = (ListView) findViewById(R.id.listView);

        //Set dates of current week
        tvFromDate.setText(MTHelper.getInstance().getFirstDayOfWeek());
        tvToDate.setText(MTHelper.getInstance().getLastDayOfWeek());

        //Set listeners
        btnAddIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddIncomeDialog dialog = new AddIncomeDialog(activity);
                dialog.show();
            }
        });

        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddExpenseDialog dialog = new AddExpenseDialog(activity);
                dialog.show();
            }
        });

        tvFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeDateDialog dialog = new ChangeDateDialog(activity,
                        MTHelper.getInstance().getPeriod().getFirst(), new ChangeDateDialog.OnDateChangedListener() {
                    @Override
                    public void OnDataChanged(Date date) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        tvFromDate.setText(dateFormat.format(date));

                        MTHelper.getInstance().getPeriod().setFirst(date);
                        MTHelper.getInstance().update();
                    }
                });
                dialog.show();
            }
        });

        tvToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeDateDialog dialog = new ChangeDateDialog(activity,
                        MTHelper.getInstance().getPeriod().getLast(), new ChangeDateDialog.OnDateChangedListener() {
                    @Override
                    public void OnDataChanged(Date date) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        tvToDate.setText(dateFormat.format(date));

                        MTHelper.getInstance().getPeriod().setLast(date);
                        MTHelper.getInstance().update();
                    }
                });
                dialog.show();
            }
        });

        listView.setAdapter(new RecordAdapter(activity, MTHelper.getInstance().getRecords()));
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

        //Subscribe to helper
        MTHelper.getInstance().addObserver(this);
    }

    @Override
    public void update(Observable observable, Object data) {
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
    }
}
