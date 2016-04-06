package com.blogspot.e_kanivets.moneytracker.activity;

import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;

import com.blogspot.e_kanivets.moneytracker.MtApp;
import com.blogspot.e_kanivets.moneytracker.R;
import com.blogspot.e_kanivets.moneytracker.activity.base.BaseBackActivity;
import com.blogspot.e_kanivets.moneytracker.adapter.ExpandableListReportAdapter;
import com.blogspot.e_kanivets.moneytracker.controller.AccountController;
import com.blogspot.e_kanivets.moneytracker.controller.ExchangeRateController;
import com.blogspot.e_kanivets.moneytracker.repo.DbHelper;
import com.blogspot.e_kanivets.moneytracker.entity.Account;
import com.blogspot.e_kanivets.moneytracker.model.Period;
import com.blogspot.e_kanivets.moneytracker.entity.Record;
import com.blogspot.e_kanivets.moneytracker.report.ReportConverter;
import com.blogspot.e_kanivets.moneytracker.report.ReportMaker;
import com.blogspot.e_kanivets.moneytracker.report.base.IReport;
import com.blogspot.e_kanivets.moneytracker.ui.presenter.ShortSummaryPresenter;
import com.blogspot.e_kanivets.moneytracker.util.CurrencyProvider;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;

public class ReportActivity extends BaseBackActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "ReportActivity";

    public static final String KEY_PERIOD = "key_period";
    public static final String KEY_RECORD_LIST = "key_record_list";

    @Inject
    ExchangeRateController rateController;
    @Inject
    AccountController accountController;

    private List<Record> recordList;
    private Period period;

    private ShortSummaryPresenter shortSummaryPresenter;

    @Bind(R.id.spinner_currency)
    AppCompatSpinner spinnerCurrency;
    @Bind(R.id.exp_list_view)
    ExpandableListView expandableListView;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_report;
    }

    @Override
    protected boolean initData() {
        super.initData();

        recordList = getIntent().getParcelableArrayListExtra(KEY_RECORD_LIST);
        if (recordList == null) return false;

        period = getIntent().getParcelableExtra(KEY_PERIOD);
        if (period == null) return false;

        MtApp.get().getAppComponent().inject(ReportActivity.this);

        return true;
    }

    @Override
    protected void initViews() {
        super.initViews();

        initSpinnerCurrency();

        shortSummaryPresenter = new ShortSummaryPresenter(ReportActivity.this);
        expandableListView.addHeaderView(shortSummaryPresenter.create(false));
    }

    private void update(String currency) {
        ReportMaker reportMaker = new ReportMaker(rateController);
        IReport report = reportMaker.getReport(currency, period, recordList);

        ExpandableListReportAdapter adapter = null;

        if (report != null) {
            ReportConverter reportConverter = new ReportConverter(report);
            adapter = new ExpandableListReportAdapter(ReportActivity.this, reportConverter);
        }

        expandableListView.setAdapter(adapter);
        shortSummaryPresenter.update(report, currency, reportMaker.currencyNeeded(currency, recordList));
    }

    private void initSpinnerCurrency() {
        List<String> currencyList = CurrencyProvider.getAllCurrencies();

        spinnerCurrency.setAdapter(new ArrayAdapter<>(ReportActivity.this,
                R.layout.view_spinner_item, currencyList));
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                update((String) spinnerCurrency.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String currency = DbHelper.DEFAULT_ACCOUNT_CURRENCY;
        Account defaultAccount = accountController.readDefaultAccount();
        if (defaultAccount != null) currency = defaultAccount.getCurrency();

        for (int i = 0; i < currencyList.size(); i++) {
            if (currency.equals(currencyList.get(i))) {
                spinnerCurrency.setSelection(i);
                break;
            }
        }
    }
}