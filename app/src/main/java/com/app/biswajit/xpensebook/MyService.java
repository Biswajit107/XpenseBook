package com.app.biswajit.xpensebook;

import static com.app.biswajit.xpensebook.constants.Constants.DEBIT_KEY_WORD;
import static com.app.biswajit.xpensebook.constants.Constants.HDFC;
import static com.app.biswajit.xpensebook.constants.Constants.HDFC_BANK;
import static com.app.biswajit.xpensebook.constants.Constants.ICICI;
import static com.app.biswajit.xpensebook.constants.Constants.ICICI_BANK;
import static com.app.biswajit.xpensebook.constants.Constants.OTHER_BANK;
import static com.app.biswajit.xpensebook.constants.Constants.PAYMENT_TYPE_KEY_WORD;
import static com.app.biswajit.xpensebook.constants.Constants.SBI;
import static com.app.biswajit.xpensebook.constants.Constants.SBI_BANK;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;
//import android.support.v4.app.NotificationCompat;NotificationCompat
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import androidx.annotation.Nullable;

import com.app.biswajit.xpensebook.dao.DateRangeDao;
import com.app.biswajit.xpensebook.dao.ExpenseDao;
import com.app.biswajit.xpensebook.dao.MessageDao;
import com.app.biswajit.xpensebook.database.AppDatabase;
import com.app.biswajit.xpensebook.entity.DateRange;
import com.app.biswajit.xpensebook.entity.Expense;
import com.app.biswajit.xpensebook.entity.Message;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;

public class MyService extends Service {


    MessageDao messageDao;
    ExpenseDao expenseDao;
    DateRangeDao dateRangeDao;
    AppDatabase appDb;

    public MyService(Object object) {

        if(object instanceof MessageDao) {
            this.messageDao = (MessageDao) object;
        }else if(object instanceof ExpenseDao){
            this.expenseDao = (ExpenseDao) object;
        }else if(object instanceof DateRangeDao){
            this.dateRangeDao = (DateRangeDao) object;
        }else if(object instanceof AppDatabase){
            this.appDb = (AppDatabase) object;
        }
    }

//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }

    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";

    public MyService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        startForeground();

//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void startForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service is running background")
                .setContentIntent(pendingIntent)
                .build());
    }

    public static long firstDayOfMonth(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        return cal.getTimeInMillis();
    }

    public static long lastDayOfMonth(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY,cal.getActualMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,cal.getActualMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,cal.getActualMaximum(Calendar.SECOND));
        return cal.getTimeInMillis();
    }

    public static Date convertToBeginningOfTheDay(Date fromDate) {
        Calendar calStart = new GregorianCalendar();
        calStart.setTime(fromDate);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        return calStart.getTime();
    }

    public static Date convertToEndOfTheDay(Date toDate) {
        Calendar calEnd = new GregorianCalendar();
        calEnd.setTime(toDate);
        calEnd.set(Calendar.HOUR_OF_DAY, calEnd.getActualMaximum(Calendar.HOUR_OF_DAY));
        calEnd.set(Calendar.MINUTE, calEnd.getActualMaximum(Calendar.MINUTE));
        calEnd.set(Calendar.SECOND, calEnd.getActualMaximum(Calendar.SECOND));
        return calEnd.getTime();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getAllSms(Context context, DateRange dateRange) {

        ContentResolver cr = context.getContentResolver();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        Calendar currentCal = Calendar.getInstance();
        String where = Telephony.Sms.DATE + " >= " + dateRange.fromDate.getTime() + " AND " + Telephony.Sms.DATE + " <= " + dateRange.toDate.getTime() ;
        String order = "date ";
        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, where, null, order);
        int totalSMS = 0;
        if (c != null) {
            totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    int msgId = c.getInt(c.getColumnIndexOrThrow(Telephony.Sms._ID));
                    Long smsDate = c.getLong(c.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    String sender = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    String type;
                    switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                        case Telephony.Sms.MESSAGE_TYPE_INBOX:
                            type = "inbox";
                            Message msg = new Message();
                            Message receivedSmsMessage = new Message();
                            msg.bankName = getBankName(sender);
                            msg.messageConent = body;
                            msg.messageRecivedAt = new Timestamp(smsDate);
                            msg.msgId = msg.hashCode();
                            Message alreadyPresentMsg = null;
                            try {

                                alreadyPresentMsg = getMessage(msg);
                                if (shouldConsider(body, sender) && (alreadyPresentMsg == null || alreadyPresentMsg.msgId != msg.msgId)) {
                                    insertMessage(msg);
                                }

                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            break;
                    }


                    c.moveToNext();
                }
            }

            c.close();

        } else {
            Toast.makeText(this, "No message to show!", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean shouldConsider(String message, String sender){

        return (sender.contains("9668529132")
                || sender.contains(HDFC)
                || sender.contains(ICICI)
                || sender.contains(SBI))
                && (message.contains("debit")
                || message.contains("credit")
                || message.contains("spent")
                || message.contains("Money Transferred")
                || (message.contains("paying") && message.contains("NetBanking"))
                || (message.contains("withdrawn") && message.contains("Debit Card"))
                || (message.contains("Payment") && message.contains("Debit Card")));
    }

    public DateRange getDateRange() throws ExecutionException, InterruptedException {

        Callable<DateRange> callable = () -> dateRangeDao.getDateRange();

        Future<DateRange> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }

    public Boolean insertDateRange(DateRange dateRange) throws ExecutionException, InterruptedException {

        Callable<Boolean> callable = () -> {
            dateRangeDao.deleteDateRange();
            dateRangeDao.insertAll(dateRange);
            return true;
        };

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }

    public Message getMessage(Message msg) throws ExecutionException, InterruptedException {

        Callable<Message> callable = () -> messageDao.findByMsgId(msg.msgId);

        Future<Message> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }

    public Message getMessageById(int mid) throws ExecutionException, InterruptedException {

        Callable<Message> callable = () -> messageDao.findBymId(mid);

        Future<Message> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }

    public Boolean insertMessage(Message msg) throws ExecutionException, InterruptedException {

        Callable<Boolean> callable = () -> {
            messageDao.insertAll(msg);
            return true;
        };

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }

    public static String getBankName(String sender) {

        return sender.contains(HDFC) ? HDFC_BANK
                : sender.contains(ICICI) ? ICICI_BANK
                : sender.contains(SBI) ? SBI_BANK : OTHER_BANK;
    }



    public static Double extractAmount(String messageConent) {

        String regEx1 = "Rs.(?:\\d+|\\d{1,2},(?:\\d{2},)*\\d{3})(?:\\.\\d{2})? ";
        String regEx2 = "Rs. (?:\\d+|\\d{1,2},(?:\\d{2},)*\\d{3})(?:\\.\\d{2})? ";
        String regEx3 = "Rs(?:\\d+|\\d{1,2},(?:\\d{2},)*\\d{3})(?:\\.\\d{2})? ";
        String regEx4 = "Rs (?:\\d+|\\d{1,2},(?:\\d{2},)*\\d{3})(?:\\.\\d{2})? ";
        String regEx5 = "INR.(?:\\d+|\\d{1,2},(?:\\d{2},)*\\d{3})(?:\\.\\d{2})? ";
        String regEx6 = "INR. (?:\\d+|\\d{1,2},(?:\\d{2},)*\\d{3})(?:\\.\\d{2})? ";

        String amountStr =  getAmount(messageConent,
                Arrays.asList(regEx1,regEx2,regEx3,regEx4, regEx5, regEx6),
                false,0);

        return !"notfound".equals(amountStr) ? Double.parseDouble(amountStr.trim().replaceAll(",", "")) : 0.0;
    }

    private static String getAmount(String msg, List<String> regExpList, boolean found, int next) {

        if(next >= regExpList.size()){
            return "notfound";
        }

        Pattern pattern = Pattern.compile(regExpList.get(next));
        Matcher matcher = pattern.matcher(msg);

        String regEx = "(?:\\d+|\\d{1,2},(?:\\d{2},)*\\d{3})(?:\\.\\d{2})? ";
        if(found){
            pattern = Pattern.compile(regEx);
            matcher = pattern.matcher(msg);
            if(matcher.find()){
                return matcher.group(0);
            }
        }
        else {
            if (matcher.find()) {
                return getAmount(matcher.group(0),regExpList, true, 0);
            }
            else{
                return getAmount(msg,regExpList, false, ++next);
            }
        }

        return  "notfound";
    }

    public void deleteCurrentExpenseRow(int id) {
        Callable<Boolean> callable = () -> {
            expenseDao.softDeleteExpense(id);
            return true;
        };

        Executors.newSingleThreadExecutor().submit(callable);
    }

    public void resetDeleteFlagInExpense() {
        Callable<Boolean> callable = () -> {
            expenseDao.resetDeleteFlag();
            return true;
        };

        Executors.newSingleThreadExecutor().submit(callable);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void recalculate(Context context, DateRange dateRange, ProgressDialog mProgressBar){
        getAllSms(context, dateRange);
        processMessage();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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
                    expense = parseMessageAndPrepareExpense(message, getApplicationContext());
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Expense parseMessageAndPrepareExpense(Message message, Context context) {
        Expense expense = new Expense();
        String messageContent = message.messageConent;

        expense.amount =  MyService.extractAmount(message.messageConent);
        expense.deleteFlag = 0;

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

        expense.paymentDestination = getVendor(messageContent, context);
        expense.paymentType = messageContent.contains(PAYMENT_TYPE_KEY_WORD) || messageContent.contains("spent") || messageContent.contains("paying") ? DEBIT_KEY_WORD : "";
        expense.paymentAt = message.messageRecivedAt;
        expense.messageId = message.mid;
        return expense;
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
                }

                //return null;
            }
            catch (Exception ex){
                Log.e("MainActivity ",ex.getMessage());
            }
            return null;
        }
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


    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getVendor(String messageContent, Context context) {
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] tokens = tokenizer
                .tokenize(messageContent);
        TokenNameFinderModel model = null;

        try(InputStream inputStreamNameFinder = context.getAssets().open("ner_custom_model.bin")){
            model = new TokenNameFinderModel(inputStreamNameFinder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TokenNameFinder nameFinderME = new NameFinderME(model);
        List<Span> spans = Arrays.asList(nameFinderME.find(tokens));

        return getVendor(spans , tokens);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static String getVendor (List<Span> spans, String[] tokens)
    {

        final String DESTINATION_NAME = "dname";
        AtomicReference<String> vendor = new AtomicReference<>("");

        spans.stream().filter(span -> span.toString().contains(DESTINATION_NAME)).forEach(span -> {

            String str = span.toString();
            //str = [[0..2) sname, [22..23) dname]
            int dotIndex = str.indexOf(".");
            int secondDotIndex = str.indexOf("..") + 2;
            int parenthesisIndex = str.indexOf(")");
            System.out.println(str.substring(secondDotIndex, parenthesisIndex));

            int firstIndex = Integer.parseInt(str.substring(1, dotIndex));
            int lastIndex = Integer.parseInt(str.substring(secondDotIndex, parenthesisIndex));

            for (int i = firstIndex; i < lastIndex; i++) {
                vendor.set(vendor.get() + tokens[i] + " ");
            }
        });
        return vendor.get();
    }
}
