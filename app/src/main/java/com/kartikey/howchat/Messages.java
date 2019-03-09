package com.kartikey.howchat;

public class Messages {
    public String message, type, from;
    public long time;
    public Boolean seen;

    public Messages(String message, Boolean seen, long time, String type, String from) {
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
        this.from = from;

    }

    public Messages(){

    }
}