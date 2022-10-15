package com.app.biswajit.xpensebook.dao;

import com.app.biswajit.xpensebook.entity.Expense;
import com.app.biswajit.xpensebook.entity.Message;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ExpenseDao {
    @Query("SELECT * FROM expense")
    List<Expense> getAllExpense();

    @Query("SELECT * FROM expense where strftime('%m', date(PAYMENT_AT/1000,'unixepoch','localtime')) = (:month) and strftime('%Y', date(PAYMENT_AT/1000,'unixepoch','localtime')) = (:year)")
    List<Expense> getAllExpenseByMonth(String month, String year);

//    @Query("SELECT * FROM expense WHERE eid IN (:messageIds)")
//    List<Message> loadAllByIds(int[] messageIds);

//    @Query("SELECT * FROM message WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    Message findByName(String first, String last);

    @Insert
    void insertAll(List<Expense> expenses);

    @Delete
    void delete(Message message);
}
