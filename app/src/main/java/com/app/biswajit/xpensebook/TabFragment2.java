package com.app.biswajit.xpensebook;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.biswajit.xpensebook.dao.ExpenseDao;
import com.app.biswajit.xpensebook.dao.MessageDao;
import com.app.biswajit.xpensebook.database.AppDatabase;
import com.app.biswajit.xpensebook.entity.Expense;
import com.app.biswajit.xpensebook.entity.Message;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.tab_fragment2, container, false);
        String[] spaceProbeHeaders={"Source","Destination","Amount"};
        String[][] spaceProbes={{"Source","Destination","Amount"}};
        final TableView<String[]> tableView = (TableView<String[]>) rootView.findViewById(R.id.tableView);
        tableView.setColumnCount(3);
        tableView.setHeaderBackgroundColor(Color.parseColor("#2ecc71"));
        tableView.setHeaderAdapter(new SimpleTableHeaderAdapter(getActivity(),spaceProbeHeaders));
        tableView.setColumnCount(3);
        tableView.setDataAdapter(new SimpleTableDataAdapter(getActivity(), spaceProbes));

        // Inflate the layout for this fragment
//        TableView tableView = (TableView) findViewById(R.id.tableView);
        return rootView;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void update() {
        Log.i("Fragment2", "Inside Fagment2");
        String[] spaceProbeHeaders={"Source","Destination","Amount"};
        String[][] spaceProbes={{"Source","Destination","Amount"},{"Source","Destination","Amount"},{"Source","Destination","Amount"}};
        final TableView<Expense> tableView = rootView.findViewById(R.id.tableView);
        List<Expense> expenses = readExpenses();
        final ExpenseTableDataAdapter expenseTableDataAdapter = new ExpenseTableDataAdapter(getActivity(), expenses, tableView);
        tableView.setHeaderBackgroundColor(Color.parseColor("#2ecc71"));
        tableView.setHeaderAdapter(new SimpleTableHeaderAdapter(getActivity().getApplicationContext(),spaceProbeHeaders));
        tableView.setColumnCount(3);
        tableView.setDataAdapter(expenseTableDataAdapter);
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
}
