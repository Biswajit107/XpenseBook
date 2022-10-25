package com.app.biswajit.xpensebook;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.app.biswajit.xpensebook.dao.ExpenseDao;
import com.app.biswajit.xpensebook.dao.MessageDao;
import com.app.biswajit.xpensebook.database.AppDatabase;
import com.app.biswajit.xpensebook.entity.Expense;
import com.app.biswajit.xpensebook.entity.Message;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabFragment2#newInstance} factory method to
 * create an instance of this fragment.
 */

public class TabFragment2 extends Fragment implements Updatable{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    View rootView;
    private static AppDatabase appDb;

    private TableLayout mTableLayout;
    private TableLayout mTableHeaderLayout;
    ProgressDialog mProgressBar;

    private static final NumberFormat PRICE_FORMATTER = NumberFormat.getNumberInstance();

    public TabFragment2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabFragment2.
     */
    // TODO: Rename and change types and number of parameters
    public static TabFragment2 newInstance(String param1, String param2) {
        TabFragment2 fragment = new TabFragment2();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDb = AppDatabase.getDatabase(getContext());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mProgressBar = new ProgressDialog(this.getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.tab_fragment2, container, false);
        mTableLayout = (TableLayout) rootView.findViewById(R.id.expenseTableView);
        mTableLayout.setStretchAllColumns(true);
        mTableHeaderLayout = (TableLayout) rootView.findViewById(R.id.headerTableView);
        mTableHeaderLayout.setStretchAllColumns(true);
        return rootView;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void update() {
        Log.i("Fragment2", "Inside Fragment2");

        startLoadData();
        int leftRowMargin=0;
        int topRowMargin=0;
        int rightRowMargin=0;
        int bottomRowMargin = 0;
        int textSize = 0, smallTextSize =0, mediumTextSize = 0;

        textSize = (int) getResources().getDimension(R.dimen.font_size_very_small);
        smallTextSize = (int) getResources().getDimension(R.dimen.font_size_small);
        mediumTextSize = (int)
                getResources().getDimension(R.dimen.font_size_medium);
        List<Expense> data = readExpenses();

        int rows = data.size();

        TextView textSpacer = null;
        mTableLayout.removeAllViews();

        setHeader(smallTextSize);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");

        // -1 means heading row
        for(int i = 0; i < rows; i ++) {

            Expense row = null;

            if (i > -1)
                row = data.get(i);
            else {
                textSpacer = new TextView(this.getContext());
                textSpacer.setText("");
            }

            // data columns
            //1st column
            final TextView tv = new TextView(this.getContext());
            tv.setLayoutParams(new
                    TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT,1f));
            tv.setGravity(Gravity.LEFT);

            tv.setPadding(5, 15, 0, 15);
            tv.setBackgroundColor(Color.parseColor("#f8f8f8"));
            tv.setText(dateFormat.format(row.paymentAt));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mediumTextSize);


            //2nd column
            final TextView tv2 = new TextView(this.getContext());
            //cell style
            tv2.setLayoutParams(new
                    TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT,1f));
            tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, mediumTextSize);
            tv2.setGravity(Gravity.LEFT);
            tv2.setPadding(5, 15, 0, 15);
            tv2.setBackgroundColor(Color.parseColor("#ffffff"));
            tv2.setTextColor(Color.parseColor("#000000"));
            tv2.setText(row.paymentDestination);


            //4th column
            final TextView tv4 = new TextView(this.getContext());
            //cell style
            tv4.setLayoutParams(new
                    TableRow.LayoutParams(0,
                    TableRow.LayoutParams.MATCH_PARENT,1f));
            tv4.setTextSize(TypedValue.COMPLEX_UNIT_PX, mediumTextSize);
            tv4.setGravity(Gravity.CENTER);
            tv4.setPadding(5, 15, 0, 15);
            //cell value style
            tv4.setBackgroundColor(Color.parseColor("#ffffff"));
            tv4.setTextColor(Color.parseColor("#000000"));
            String priceString = "₹" + PRICE_FORMATTER.format(row.amount);
            tv4.setText(priceString);


            // add table row to Table Layout
            final TableRow tr = new TableRow(this.getContext());
            tr.setId(i + 1);
            TableLayout.LayoutParams trParams = new
                    TableLayout.LayoutParams(0,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin,
                    bottomRowMargin);
            tr.setPadding(0,0,0,0);
            tr.setLayoutParams(trParams);
            tr.addView(tv);
            tr.addView(tv2);
            tr.addView(tv4);
            if (i > -1) {
                tr.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        TableRow tr = (TableRow) v;
                    }
                });
            }

            mTableLayout.addView(tr, trParams);
            if (i > -1) {
                // add separator row
                final TableRow trSep = new TableRow(this.getContext());
                TableLayout.LayoutParams trParamsSep = new
                        TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT);
                trParamsSep.setMargins(leftRowMargin, topRowMargin,
                        rightRowMargin, bottomRowMargin);
                trSep.setLayoutParams(trParamsSep);
                TextView tvSep = new TextView(this.getContext());
                TableRow.LayoutParams tvSepLay = new
                        TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
                tvSepLay.span = 4;
                tvSep.setLayoutParams(tvSepLay);
                tvSep.setBackgroundColor(Color.parseColor("#d9d9d9"));
                tvSep.setHeight(1);
                trSep.addView(tvSep);
                mTableLayout.addView(trSep, trParamsSep);
            }

        }
        mProgressBar.hide();

    }

    private void setHeader(int smallTextSize) {

        mTableHeaderLayout.removeAllViews();

        final TextView tv = new TextView(this.getContext());
        tv.setLayoutParams(new
                TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT,1f));
        tv.setGravity(Gravity.CENTER);

        tv.setPadding(5, 15, 0, 15);
        tv.setText("Date");
        tv.setBackgroundColor(Color.parseColor("#f0f0f0"));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);


        final TextView tv1 = new TextView(this.getContext());
        tv1.setLayoutParams(new
                TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT,1f));
        tv1.setGravity(Gravity.CENTER);

        tv1.setPadding(5, 15, 0, 15);
        tv1.setText("TO");
        tv1.setBackgroundColor(Color.parseColor("#f7f7f7"));
        tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);

        final TextView tv2 = new TextView(this.getContext());
        tv2.setLayoutParams(new
                TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT,1f));
        tv2.setGravity(Gravity.CENTER);

        tv2.setPadding(5, 15, 0, 15);
        tv2.setText("Amount");
        tv2.setBackgroundColor(Color.parseColor("#f0f0f0"));
        tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);

        final TableRow tr = new TableRow(this.getContext());
        TableLayout.LayoutParams trParams = new
                TableLayout.LayoutParams(0,
                TableLayout.LayoutParams.WRAP_CONTENT);
        trParams.setMargins(0, 0, 0,
                0);
        tr.setPadding(0,0,0,0);
        tr.setLayoutParams(trParams);
        tr.addView(tv);
        tr.addView(tv1);
        tr.addView(tv2);

        mTableHeaderLayout.addView(tr, trParams);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<Expense> readExpenses() {
        GetAsyncTask asyncTask = new GetAsyncTask(appDb.expenseDao());
        List<Expense> expenses = new ArrayList<>();
        String [][]expenseArray;
        try {
            expenses = (List<Expense>)asyncTask.execute("",new Expense()).get();
            expenses = expenses != null ? expenses : new ArrayList<>();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return expenses;
    }

    private static class GetAsyncTask extends AsyncTask<Object, Void, Object> {
        private MessageDao messageDao;
        private ExpenseDao expenseDao;

        GetAsyncTask(Object dao) {
            if(dao instanceof  MessageDao) {
                messageDao = (MessageDao)dao;
            }
            else if(dao instanceof ExpenseDao){
                expenseDao = (ExpenseDao)dao;
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Object doInBackground(final Object... params) {
            try {
                if(params[1] instanceof Message) {
                    Log.d(getClass().getSimpleName(), "Get Message in background ");
                    return messageDao.findByMessageProcessed(); // This line throws the exception
                }
                else if(params[1] instanceof Expense){
                    LocalDate currentdate = LocalDate.now();
                    //String currentMonth = currentdate.getMonthValue() < 10 ? "0" + String.valueOf(currentdate.getMonthValue()) : String.valueOf(currentdate.getMonthValue()) ;
                    String yearMonth = YearMonth.now().toString();
                    String currentMonth = yearMonth.substring(5);
                    String currentYear = Year.now().toString();
                    return expenseDao.getAllExpenseByMonth(currentMonth,currentYear);
                }

                //return null;
            }
            catch (Exception ex){
                Log.e("MainActivity ",ex.getMessage());
            }
            return null;
        }
    }

    public void startLoadData() {
        mProgressBar.setCancelable(false);
        mProgressBar.setMessage("Fetching Expenses..");
        mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressBar.show();
        //new LoadDataTask().execute(0);
    }
}
