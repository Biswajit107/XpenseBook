package com.app.biswajit.xpensebook.dao;

import com.app.biswajit.xpensebook.entity.Expense;
import com.app.biswajit.xpensebook.entity.Message;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ExpenseDao {
    @Query("SELECT * FROM expense")
    List<Expense> getAllExpense();

    @Query("SELECT distinct * FROM expense where DELETE_FLAG = 0 and strftime('%m', date(PAYMENT_AT/1000,'unixepoch','localtime')) = (:month) and strftime('%Y', date(PAYMENT_AT/1000,'unixepoch','localtime')) = (:year) order by PAYMENT_AT")
    List<Expense> getAllExpenseByMonth(String month, String year);

    @Insert
    void insertAll(List<Expense> expenses);

    @Delete
    void delete(Message message);

    @Query("Update Expense set DELETE_FLAG = 1 where eid = :id")
    void softDeleteExpense(int id);

    @Query("Update Expense set DELETE_FLAG = 0")
    void resetDeleteFlag();

    @Query("SELECT distinct * FROM expense where DELETE_FLAG = 0 and PAYMENT_AT >= :fromDate and PAYMENT_AT <= :toDate order by PAYMENT_AT")
    List<Expense> getAllExpenseByDateRange(long fromDate, long toDate);
}
