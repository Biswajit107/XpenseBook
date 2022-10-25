package com.app.biswajit.xpensebook.entity;

import com.app.biswajit.xpensebook.TimestampConverter;

import java.util.Date;
import java.sql.Timestamp;
import java.util.Objects;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity
public class Message {
    @PrimaryKey(autoGenerate = true)
    public int mid;

    @ColumnInfo(name = "MESSAGE_ID")
    public int msgId;

    @ColumnInfo(name = "BANK_NAME")
    public String bankName;

    @ColumnInfo(name = "MESSAGE_CONTENT")
    public String messageConent;

    @ColumnInfo(name = "MESSAGE_PROCESSED")
    public int messageProcessed;

    @ColumnInfo(name = "MESSAGE_RECEIVED_AT")
    @TypeConverters({TimestampConverter.class})
    public Date messageRecivedAt;

//    public Message(int mid, String bankName, String messageConent, Date messageRecivedAt) {
//        this.mid = mid;
//        this.bankName = bankName;
//        this.messageConent = messageConent;
//        this.messageRecivedAt = messageRecivedAt;
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(bankName, message.bankName) && Objects.equals(messageConent, message.messageConent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bankName, messageConent);
    }
}
