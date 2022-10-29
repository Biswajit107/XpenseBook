package com.app.biswajit.xpensebook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.biswajit.xpensebook.dao.ExpenseDao;
import com.app.biswajit.xpensebook.dao.MessageDao;
import com.app.biswajit.xpensebook.database.AppDatabase;
import com.app.biswajit.xpensebook.entity.Expense;
import com.app.biswajit.xpensebook.entity.Message;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;
import static com.app.biswajit.xpensebook.constants.Constants.AMOUNT_PARSING_KEYWORD_FROM;
import static com.app.biswajit.xpensebook.constants.Constants.AMOUNT_PARSING_KEYWORD_TO;
import static com.app.biswajit.xpensebook.constants.Constants.DEBIT_KEY_WORD;
import static com.app.biswajit.xpensebook.constants.Constants.PAYMENT_TYPE_KEY_WORD;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabFragment1#newInstance} factory method to
 * create an instance of this fragment.
 */

interface Updatable {
    public void update();
}

public class TabFragment1 extends Fragment implements Updatable{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public static final String MY_SHARED_PREFERENCES = "MySharedPrefs" ;
    View rootview;


    public TabFragment1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabFragment1.
     */
    // TODO: Rename and change types and number of parameters
    public static TabFragment1 newInstance(String param1, String param2) {
        TabFragment1 fragment = new TabFragment1();
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        rootview = inflater.inflate(R.layout.tab_fragment1, container, false);

        update();
        // Inflate the layout for this fragment
        return rootview;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void update() {
        // refresh your fragment, if needed
        Log.i("Fragment1","Inside Fragment1");
        processMessage();
        String expense = readExpenses();
        TextView textView = (TextView)rootview.findViewById(R.id.totalExpense);
        textView.setText(expense);

    }
    private static AppDatabase appDb;
    @RequiresApi(api = Build.VERSION_CODES.N)
    private String readExpenses() {
        GetAsyncTask asyncTask = new GetAsyncTask(appDb.expenseDao());
        List<Expense> expenses = new ArrayList<>();
        try {
            expenses = (List<Expense>)asyncTask.execute("",new Expense()).get();
            String totalExpense = expenses != null ? String.valueOf(expenses.stream().mapToDouble(i->i.amount).sum()) : "";
            return totalExpense;
            //binding.setTotalDebited(totalExpense);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }
    private void processMessage() {
        GetAsyncTask asyncTask = new GetAsyncTask(appDb.messageDao());
        List<Message> messages = new ArrayList<>();
        try {
            messages = (List<Message>) asyncTask.execute("", new Message()).get();
            messages = messages != null ? messages : new ArrayList<>();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<Expense> expenses = new ArrayList<>();
        List<Integer> messageIds = new ArrayList<>();
        for (Message message : messages) {
            Expense expense = new Expense();
            try {
                if (message != null
                        && !((message.messageConent.contains("credit") && !message.messageConent.contains("debit")
                        || ( (message.messageConent.contains("credit") && message.messageConent.contains("debit")
                        && message.messageConent.indexOf("debit") > message.messageConent.indexOf("credit")))
                        || message.messageConent.contains("will be debited")))) {
                    expense = parseMessageAndPrepareExpense(message);
                    expenses.add(expense);
                    messageIds.add(message.mid);
                }
            }
            catch (NumberFormatException ex){
                Log.i(getClass().getName(), ex.getLocalizedMessage());
            }
        }
        if (expenses != null && expenses.size() > 0) {
            new InsertAsyncTask(appDb.expenseDao()).execute(expenses, new Expense());
        }
        if (messageIds != null && messageIds.size() > 0) {
            new UpdateAsyncTask(appDb.messageDao()).execute(messageIds, new Message());
        }
    }

    private Expense parseMessageAndPrepareExpense(Message message) {
        Expense expense = new Expense();
        String messageContent = message.messageConent;

        expense.amount =  MyService.extractAmount(message.messageConent);

        List<String> items = Arrays.asList(message.messageConent.split("\\s* \\s*"));

        //Parsing for source
        int index = items.indexOf("from") > -1 ? items.indexOf("from"):items.indexOf("via") > -1?items.indexOf("via"):-1;
        int secondaryIndex = Math.min(items.indexOf("on") > -1 ? items.indexOf("on"):-1
                ,items.indexOf("at") > -1 ? items.indexOf("at"):-1);
        if(items.indexOf("on") > -1 && items.indexOf("at") > -1) {
            secondaryIndex = items.indexOf("on") < items.indexOf("at") ? items.indexOf("on") : items.indexOf("at");
        }
        else {
            secondaryIndex = items.indexOf("on") > -1 ? items.indexOf("on") : items.indexOf("at") > -1 ? items.indexOf("at") : -1;
        }
        secondaryIndex = items.indexOf("to") > -1 && secondaryIndex > -1 && secondaryIndex < items.indexOf("to")  ? secondaryIndex : secondaryIndex > -1? secondaryIndex :items.indexOf("to") > -1 ? items.indexOf("to") : -1;
        String parsedString = items.get(index+1);
        for(int i = index+ 1 ;i<secondaryIndex - 1 ;i++) {
            parsedString = parsedString + " " + items.get(i+1);
        }

        if((messageContent.contains("Payment") && messageContent.contains("Debit Card"))){
            expense.paymentSource = "HDFC Bank";
        }else {
            expense.paymentSource = parsedString;
        }

        //Parsing for destination
        index = items.indexOf("to") > -1 ? items.indexOf("to"):items.indexOf("at") > -1?items.indexOf("at"):items.indexOf("Info:")> -1 ? items.indexOf("Info:") : -1;
        secondaryIndex = Math.min(items.indexOf("on") > -1 ? items.indexOf("on"):-1
                ,items.indexOf("at") > -1 ? items.indexOf("at"):-1);
        if(items.indexOf("Not") > -1 && items.indexOf("Avl") > -1) {
            secondaryIndex = items.indexOf("Not") < items.indexOf("Avl") ? items.indexOf("Not") : items.indexOf("Avl");
        }
        else {
            secondaryIndex = items.indexOf("Not") > -1 ? items.indexOf("Not") : items.indexOf("Avl") > -1 ? items.indexOf("Avl") : -1;
        }
        secondaryIndex = items.indexOf("on") > -1 && secondaryIndex > -1 && secondaryIndex < items.indexOf("on")  ? secondaryIndex : secondaryIndex > -1? secondaryIndex :items.indexOf("on") > -1 ? items.indexOf("on") : -1;
        parsedString = items.get(index+1);
        for(int i = index+ 1 ;i<secondaryIndex - 1 ;i++) {
            parsedString = parsedString + " " + items.get(i+1);

        }

        expense.paymentDestination = parsedString;
        expense.paymentType = messageContent.contains(PAYMENT_TYPE_KEY_WORD) || messageContent.contains("spent") || messageContent.contains("paying") ? DEBIT_KEY_WORD : "";
        expense.paymentAt = message.messageRecivedAt;
        expense.messageId = message.mid;
        return expense;
    }

    private static class InsertAsyncTask extends AsyncTask<Object, Void, Void> {
        private MessageDao messageDao;
        private ExpenseDao expenseDao;

        InsertAsyncTask(Object dao) {
            if(dao instanceof  MessageDao) {
                messageDao = (MessageDao)dao;
            }
            else if(dao instanceof ExpenseDao){
                expenseDao = (ExpenseDao)dao;
            }
        }

        @Override
        protected Void doInBackground(final Object... params) {
            try {
                if(params[1] instanceof Message) {
                    messageDao.insertAll((Message) params[0]); // This line throws the exception
                }
                else if(params[1] instanceof Expense){
                    List<Expense> expenses = (List<Expense>)params[0];
                    expenseDao.insertAll(expenses);
                }
                Log.d(getClass().getSimpleName(), "do in background 1 ");
                //return null;
            }
            catch (Exception ex){
                Log.e("MainActivity ",ex.getMessage());
            }
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Object, Void, Void> {
        private MessageDao messageDao;
        private ExpenseDao expenseDao;

        UpdateAsyncTask(Object dao) {
            if(dao instanceof  MessageDao) {
                messageDao = (MessageDao)dao;
            }
            else if(dao instanceof ExpenseDao){
                expenseDao = (ExpenseDao)dao;
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(final Object... params) {
            try {
                if(params[1] instanceof Message) {
                    List<Integer> messageIdList = (List<Integer>) params[0];
                    //int []messageIds = messageIdList.stream().mapToInt(Integer::intValue).toArray();

                    messageDao.updateMessage(1,messageIdList); // This line throws the exception
                }
                else if(params[1] instanceof Expense){

                }
                Log.d(getClass().getSimpleName(), "do in background 1 ");
                //return null;
            }
            catch (Exception ex){
                Log.e("MainActivity ",ex.getMessage());
            }
            return null;
        }
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
