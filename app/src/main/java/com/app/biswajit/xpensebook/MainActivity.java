package com.app.biswajit.xpensebook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.TextView;
import android.widget.Toast;

import com.app.biswajit.xpensebook.dao.ExpenseDao;
import com.app.biswajit.xpensebook.dao.MessageDao;
import com.app.biswajit.xpensebook.database.AppDatabase;
//import com.app.biswajit.xpensebook.databinding.ActivityMainBinding;
import com.app.biswajit.xpensebook.entity.Expense;
import com.app.biswajit.xpensebook.entity.Message;
import com.app.biswajit.xpensebook.view.MessageViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Room;
import androidx.viewpager.widget.ViewPager;

import static com.app.biswajit.xpensebook.constants.Constants.AMOUNT_PARSING_KEYWORD_FROM;
import static com.app.biswajit.xpensebook.constants.Constants.AMOUNT_PARSING_KEYWORD_TO;
import static com.app.biswajit.xpensebook.constants.Constants.DEBIT_KEY_WORD;
import static com.app.biswajit.xpensebook.constants.Constants.HDFC;
import static com.app.biswajit.xpensebook.constants.Constants.HDFC_BANK;
import static com.app.biswajit.xpensebook.constants.Constants.ICICI;
import static com.app.biswajit.xpensebook.constants.Constants.ICICI_BANK;
import static com.app.biswajit.xpensebook.constants.Constants.OTHER_BANK;
import static com.app.biswajit.xpensebook.constants.Constants.PAYMENT_DESTINATION_PARSING_KEYWORD_FROM;
import static com.app.biswajit.xpensebook.constants.Constants.PAYMENT_DESTINATION_PARSING_KEYWORD_TO;
import static com.app.biswajit.xpensebook.constants.Constants.PAYMENT_SOURCE_PARSING_KEYWORD_FROM;
import static com.app.biswajit.xpensebook.constants.Constants.PAYMENT_SOURCE_PARSING_KEYWORD_TO;
import static com.app.biswajit.xpensebook.constants.Constants.PAYMENT_TYPE_KEY_WORD;
import static com.app.biswajit.xpensebook.constants.Constants.SBI;
import static com.app.biswajit.xpensebook.constants.Constants.SBI_BANK;
import static com.app.biswajit.xpensebook.constants.Constants.SENDER_BANK_HDFC_0;
import static com.app.biswajit.xpensebook.constants.Constants.SENDER_BANK_HDFC_1;

public class MainActivity extends AppCompatActivity implements MessageListener{

    //private static ActivityMainBinding binding = null;
    private static AppDatabase appDb;
    private MessageViewModel messageViewModel;
    SharedPreferences preferences;
    public static final String MY_SHARED_PREFERENCES = "MySharedPrefs" ;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create an instance of the tab layout from the view.
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        // Using PagerAdapter to manage page views in fragments.
        // Each page is represented by its own fragment.
        // This is another example of the adapter pattern.
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);

        // Setting a listener for clicks.
        viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                refreshTab(tab.getPosition(),viewPager);// create a method
                viewPager.setCurrentItem(tab.getPosition());
//                if (tab.getPosition() == 0) {
//
//                    viewPager.getAdapter().notifyDataSetChanged();
//
//                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.i("MainActivity","Inside onTabUnselected");
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.i("MainActivity","Inside onTabReselected");
                refreshTab(tab.getPosition(),viewPager);// create a method
            }
        });

        //Register sms listener
        MessageReceiver.bindListener(this);
        appDb = AppDatabase.getDatabase(getApplicationContext());

        MyService myService = new MyService(appDb.messageDao());
        myService.getAllSms(this.getApplicationContext());

        processMessage();
//        String totalExpense = readExpenses();
//        viewPager.setOffscreenPageLimit(1);
//        FragmentManager fragmentManager=getSupportFragmentManager();
//        viewPager.setAdapter(new PagerAdapter(fragmentManager,3));

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void refreshTab(int position, ViewPager viewPager) {
        switch (position) {
            case 0:
                TabFragment1 comP1 = (TabFragment1) viewPager.getAdapter().instantiateItem(viewPager, position);
                comP1.update();
                break;
            case 1:
                TabFragment2 comP2 = (TabFragment2) viewPager.getAdapter().instantiateItem(viewPager, position);
                comP2.update();
                break;
            case 2:
                TabFragment3 comP3 = (TabFragment3) viewPager.getAdapter().instantiateItem(viewPager, position);
                comP3.update();
                break;
            default:

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String readExpenses() {
        GetAsyncTask asyncTask = new GetAsyncTask(appDb.expenseDao());
        List<Expense> expenses = new ArrayList<>();
        try {
            expenses = (List<Expense>)asyncTask.execute("",new Expense()).get();
            return expenses != null ? String.valueOf(expenses.stream().mapToDouble(i->i.amount).sum()) : "";
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
            messages = (List<Message>)asyncTask.execute("",new Message()).get() ;
            messages = messages != null ? messages : new ArrayList<>();
        } catch (ExecutionException e) {
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


//        expense.amount = Double.parseDouble(messageContent.substring(beginIndex,offset).trim());
//
//        beginIndex = messageContent.indexOf(PAYMENT_SOURCE_PARSING_KEYWORD_FROM) + PAYMENT_SOURCE_PARSING_KEYWORD_FROM.length();
//        offset = messageContent.indexOf(PAYMENT_SOURCE_PARSING_KEYWORD_TO) - 1 ;
//        expense.paymentSource = messageContent.substring(beginIndex,offset).trim();
//
//        beginIndex = messageContent.indexOf(PAYMENT_DESTINATION_PARSING_KEYWORD_FROM) + PAYMENT_DESTINATION_PARSING_KEYWORD_FROM.length();
//        offset = messageContent.indexOf(PAYMENT_DESTINATION_PARSING_KEYWORD_TO) - 1 ;
//        expense.paymentDestination = message.bankName + " : " + messageContent.substring(beginIndex,offset).trim();

        expense.paymentType = messageContent.contains(PAYMENT_TYPE_KEY_WORD) || messageContent.contains("spent") || messageContent.contains("paying") ? DEBIT_KEY_WORD : "";
        expense.paymentAt = message.messageRecivedAt;
        expense.messageId = message.mid;
        return expense;
    }



    @Override
    public void messageReceived(SmsMessage smsMessage) {
        String sender = smsMessage.getOriginatingAddress();
        String message = smsMessage.getMessageBody();
        if(MyService.shouldConsider(message, sender)){

            Message receivedSmsMessage = new Message();

            receivedSmsMessage.bankName = MyService.getBankName(sender);
            receivedSmsMessage.messageConent = message;
            receivedSmsMessage.messageRecivedAt = new Timestamp(System.currentTimeMillis());
            new InsertAsyncTask(appDb.messageDao()).execute(receivedSmsMessage,new Message());

            Toast.makeText(this, "New Message Received and stored in DB", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onRefresh(View view) {
        processMessage();
        readExpenses();
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
                    //LocalDate currentdate = LocalDate.now();
//                    String currentMonth = currentdate.getMonthValue() < 10 ? "0" + String.valueOf(currentdate.getMonthValue()) : String.valueOf(currentdate.getMonthValue()) ;
//                    String currentMonth = YearMonth.now().toString();
//                    String currentYear = Year.now().toString();
//                    return expenseDao.getAllExpenseByMonth(currentMonth,currentYear);
                }

                //return null;
            }
            catch (Exception ex){
                Log.e("MainActivity ",ex.getMessage());
            }
            return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
//        if(binding != null && appDb != null) {
//            Message message = appDb.messageDao().getAll().getValue();
//            binding.setMessage(message);
//        }
        Log.d(getClass().getSimpleName(), "onResume()");
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(getClass().getSimpleName(), "onStart()");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRestart() {
        super.onRestart();

//        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
//        messageViewModel.getMessage().observe(this, message -> {
//            binding.setMessage(message);
//        });
        //messageViewModel.getAllMessage();
        Log.d(getClass().getSimpleName(), "onRestart()");
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(getClass().getSimpleName(), "onPause()");
    }
}
