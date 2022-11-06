package com.app.biswajit.xpensebook.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.app.biswajit.xpensebook.entity.DateRange;

@Dao
public interface DateRangeDao {
    @Query("SELECT * FROM daterange")
    DateRange getDateRange();

    @Insert
    void insertAll(DateRange... dateRanges);

    @Delete
    void delete(DateRange dateRange);

    @Query("Delete from daterange")
    void deleteDateRange();

}
