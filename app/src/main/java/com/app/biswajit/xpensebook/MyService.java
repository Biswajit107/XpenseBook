package com.app.biswajit.xpensebook;

import static com.app.biswajit.xpensebook.constants.Constants.HDFC;
import static com.app.biswajit.xpensebook.constants.Constants.HDFC_BANK;
import static com.app.biswajit.xpensebook.constants.Constants.ICICI;
import static com.app.biswajit.xpensebook.constants.Constants.ICICI_BANK;
import static com.app.biswajit.xpensebook.constants.Constants.OTHER_BANK;
import static com.app.biswajit.xpensebook.constants.Constants.SBI;
import static com.app.biswajit.xpensebook.constants.Constants.SBI_BANK;
import static com.app.biswajit.xpensebook.constants.Constants.SENDER_BANK_HDFC_0;
import static com.app.biswajit.xpensebook.constants.Constants.SENDER_BANK_HDFC_1;
import static com.app.biswajit.xpensebook.constants.Constants.SENDER_BANK_HDFC_3;
import static com.app.biswajit.xpensebook.constants.Constants.SENDER_BANK_HDFC_4;
import static com.app.biswajit.xpensebook.constants.Constants.SENDER_BANK_HDFC_5;
import static com.app.biswajit.xpensebook.constants.Constants.SENDER_BANK_HDFC_6;
import static com.app.biswajit.xpensebook.constants.Constants.SENDER_BANK_HDFC_7;

import android.app.PendingIntent;
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
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import androidx.annotation.Nullable;

import com.app.biswajit.xpensebook.dao.ExpenseDao;
import com.app.biswajit.xpensebook.dao.MessageDao;
import com.app.biswajit.xpensebook.entity.Expense;
import com.app.biswajit.xpensebook.entity.Message;

public class MyService extends Service {


    MessageDao messageDao;

    public MyService(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }

    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getAllSms(Context context) {

        ContentResolver cr = context.getContentResolver();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        Calendar currentCal = Calendar.getInstance();
        String where = Telephony.Sms.DATE + " >= " + cal.getTimeInMillis() + " AND " + Telephony.Sms.DATE + " <= " + currentCal.getTimeInMillis() ;
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

    public Message getMessage(Message msg) throws ExecutionException, InterruptedException {

        Callable<Message> callable = () -> messageDao.findByMsgId(msg.msgId);

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

}
