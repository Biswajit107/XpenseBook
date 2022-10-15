package com.app.biswajit.xpensebook.repository;

import android.app.Application;

import com.app.biswajit.xpensebook.dao.MessageDao;
import com.app.biswajit.xpensebook.database.AppDatabase;
import com.app.biswajit.xpensebook.entity.Message;

import androidx.lifecycle.LiveData;

public class MessageRepository {

    private MessageDao messageDao;
    private LiveData<Message> message;

    public MessageRepository(Application application){
        AppDatabase db = AppDatabase.getDatabase(application);
        messageDao = db.messageDao();
        message = messageDao.getAll();
    }

    public LiveData<Message> getMessage(){
        message = messageDao.getAll();
        return message;
    }

    public void insert(Message message){
        AppDatabase.databaseWriteExecutor.execute(()->{
            messageDao.insertAll(message);
        });
    }
}
