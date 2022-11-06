package com.app.biswajit.xpensebook.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.app.biswajit.xpensebook.TimestampConverter;

import java.util.Date;

@Entity
public class DateRange {
    @PrimaryKey(autoGenerate = true)
    public int did;
    @TypeConverters({TimestampConverter.class})
    public Date fromDate;
    @TypeConverters({TimestampConverter.class})
    public Date toDate;

}
