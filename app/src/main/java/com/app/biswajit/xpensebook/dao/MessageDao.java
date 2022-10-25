package com.app.biswajit.xpensebook.dao;

import com.app.biswajit.xpensebook.entity.Message;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.lifecycle.LiveData;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM message limit 1")
    LiveData<Message> getAll();

    @Query("SELECT * FROM message where message_processed = 0")
    List<Message> findByMessageProcessed();

    @Query("SELECT * FROM message WHERE mid IN (:messageIds)")
    List<Message> loadAllByIds(int[] messageIds);

//    @Query("SELECT * FROM message WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    Message findByName(String first, String last);

    @Insert
    void insertAll(Message... messages);

    @Query("UPDATE message SET message_processed=:processed WHERE mid IN (:messageIds)")
    void updateMessage(int processed, List<Integer> messageIds);

    @Delete
    void delete(Message message);

    @Query("SELECT * FROM message WHERE message_id IN (:msgId)")
    Message findByMsgId(int msgId);
}
