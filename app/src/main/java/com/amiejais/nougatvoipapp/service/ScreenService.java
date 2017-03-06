package com.amiejais.nougatvoipapp.service;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.amiejais.nougatvoipapp.R;
import com.amiejais.nougatvoipapp.app.IMSDroid;
import com.amiejais.nougatvoipapp.ui.activity.Main;


/**
 * Created by KNPX0678 on 04-Mar-17.
 */

public class ScreenService implements IScreenService {
    @Override
    public boolean show(Class<? extends AppCompatActivity> cls, String id) {
        Intent intent = new Intent(IMSDroid.getContext(), cls);
        intent.putExtra("id", id);
        IMSDroid.getContext().startActivity(intent);
        return true;
    }

    @Override
    public boolean show(AppCompatActivity compatActivity, Fragment fragment, String id) {
        compatActivity.getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit();
        return true;
    }

    @Override
    public boolean show(Class<? extends AppCompatActivity> cls) {
        Intent intent = new Intent(IMSDroid.getContext(), cls);
        IMSDroid.getContext().startActivity(intent);

        return true;
    }

    @Override
    public boolean bringToFront(int action, String[]... args) {
        Intent intent = new Intent(IMSDroid.getContext(), Main.class);
        try{
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP  | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("action", action);
            for(String[] arg : args){
                if(arg.length != 2){
                    continue;
                }
                intent.putExtra(arg[0], arg[1]);
            }
            IMSDroid.getContext().startActivity(intent);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean start() {
        return false;
    }

    @Override
    public boolean stop() {
        return false;
    }
}
