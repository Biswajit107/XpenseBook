package com.app.biswajit.xpensebook.entity;

import com.app.biswajit.xpensebook.TimestampConverter;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity
public class Expense {
    @PrimaryKey(autoGenerate = true)
    public int eid;
    @ColumnInfo(name = "MESSAGE_ID")
    public int messageId;
    @ColumnInfo(name = "PAYMENT_TYPE")
    public String paymentType;
    @ColumnInfo(name = "PAYMENT_SOURCE")
    public String paymentSource;
    @ColumnInfo(name = "PAYMENT_DESTINATION")
    public String paymentDestination;
    @ColumnInfo(name = "AMOUNT")
    public double amount;
    @ColumnInfo(name = "PAYMENT_AT")
    @TypeConverters({TimestampConverter.class})
    public Date paymentAt;

}
