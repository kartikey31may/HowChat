package com.kartikey.howchat;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionPagerAdapter extends FragmentPagerAdapter {

    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position){
            case 0:
                FragmentRequest fragmentRequest = new FragmentRequest();
                return fragmentRequest;
            case 1:
                FragmentChats fragmentChats = new FragmentChats();
                return fragmentChats;
            case 2:
                FragmentFriends fragmentFriends = new FragmentFriends();
                return fragmentFriends;
                default :
                    return null;


        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0:
                return "Requests";
            case 1:
                return "Chats";
            case 2:
                return "Friends";
                default:
                    return null;
        }
    }
}
