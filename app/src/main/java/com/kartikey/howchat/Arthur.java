package com.kartikey.howchat;

import com.stfalcon.chatkit.commons.models.IUser;

public class Arthur implements IUser {

    public String id, name, avatar;

    public Arthur(){

    }

    public Arthur(String id, String name, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }
}
