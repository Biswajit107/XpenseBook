package com.app.biswajit.xpensebook.view;

import android.app.Application;

import com.app.biswajit.xpensebook.entity.Message;
import com.app.biswajit.xpensebook.repository.MessageRepository;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class MessageViewModel extends AndroidViewModel {

    private MessageRepository messageRepository;
    private LiveData<Message> message;

    public MessageViewModel(Application application){
        super(application);
        messageRepository = new MessageRepository(application);
        message = messageRepository.getMessage();
    }

    public LiveData<Message> getMessage(){
        return message;
    }

    public void getAllMessage(){
        message = messageRepository.getMessage();
    }

    public void insert(Message message){
        messageRepository.insert(message);
    }
}
