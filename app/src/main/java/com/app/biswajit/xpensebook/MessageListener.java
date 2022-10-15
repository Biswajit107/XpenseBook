package com.app.biswajit.xpensebook;

import android.telephony.SmsMessage;

public interface MessageListener {

    /**
     * To call this method when new message received and send back
     * @param smsMessage Message
     */
    void messageReceived(SmsMessage smsMessage);
}
