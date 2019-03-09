package com.kartikey.howchat;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

public class Message implements IMessage {

    public String id, message;
    public Arthur arthur;
    public Date date;

    public Message(String id, String message, Arthur arthur, Date date) {
        this.id = id;
        this.message = message;
        this.arthur = arthur;
        this.date = date;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return message;
    }

    @Override
    public IUser getUser() {
        return arthur;
    }

    @Override
    public Date getCreatedAt() {
        return date;
    }
}
