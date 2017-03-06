/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package com.amiejais.nougatvoipapp.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import com.amiejais.nougatvoipapp.R;
import com.amiejais.nougatvoipapp.app.Engine;
import com.amiejais.nougatvoipapp.service.IScreenService;

import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnPredicate;

import java.util.Date;

public class Main extends AppCompatActivity {
    private static String TAG = Main.class.getCanonicalName();
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String Time = "currentTime";
    public static final String isTimeSaved = "isTimeSaved";


    public static final int ACTION_NONE = 0;
    public static final int ACTION_RESTORE_LAST_STATE = 1;
    public static final int ACTION_SHOW_AVSCREEN = 2;
    public static final int ACTION_SHOW_CONTSHARE_SCREEN = 3;
    public static final int ACTION_SHOW_SMS = 4;
    public static final int ACTION_SHOW_CHAT_SCREEN = 5;

    private static final int RC_SPLASH = 0;

    private final Engine mEngine;
    private final IScreenService mScreenService;

    public Main() {
        super();
        // Sets main activity (should be done before starting services)
        mEngine = (Engine) Engine.getInstance();
        mEngine.setMainActivity(this);
        mScreenService = ((Engine) Engine.getInstance()).getScreenService();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        if (!sharedpreferences.getBoolean(isTimeSaved, false)) {
            setTime();
        }

        if (!getTime()) {
            new AlertDialog.Builder(this).setMessage("POC time expire. Please contact developer").setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).create().show();
            return;
        }

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        if (!Engine.getInstance().isStarted()) {
            startActivityForResult(new Intent(this, ScreenSplash.class), Main.RC_SPLASH);
            return;
        }

        Bundle bundle = savedInstanceState;
        if (bundle == null) {
            Intent intent = getIntent();
            bundle = intent == null ? null : intent.getExtras();
        }
        if (bundle != null && bundle.getInt("action", Main.ACTION_NONE) != Main.ACTION_NONE) {
            handleAction(bundle);
        } else if (mScreenService != null) {
            mScreenService.show(DashBoardActivity.class);
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            handleAction(bundle);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + ")");
        if (resultCode == RESULT_OK) {
            if (requestCode == Main.RC_SPLASH) {
                Log.d(TAG, "Result from splash screen");
            }
        }
    }

    private void handleAction(Bundle bundle) {
        final String id;
        switch (bundle.getInt("action", Main.ACTION_NONE)) {
            // Default or ACTION_RESTORE_LAST_STATE
            default:
            case ACTION_RESTORE_LAST_STATE:

                break;

            // Show Audio/Video Calls
            case ACTION_SHOW_AVSCREEN:
                Log.d(TAG, "Main.ACTION_SHOW_AVSCREEN");

                final int activeSessionsCount = NgnAVSession.getSize(new NgnPredicate<NgnAVSession>() {
                    @Override
                    public boolean apply(NgnAVSession session) {
                        return session != null && session.isActive();
                    }
                });

                NgnAVSession avSession = NgnAVSession.getSession(new NgnPredicate<NgnAVSession>() {
                    @Override
                    public boolean apply(NgnAVSession session) {
                        return session != null && session.isActive() && !session.isLocalHeld() && !session.isRemoteHeld();
                    }
                });
                if (avSession == null) {
                    avSession = NgnAVSession.getSession(new NgnPredicate<NgnAVSession>() {
                        @Override
                        public boolean apply(NgnAVSession session) {
                            return session != null && session.isActive();
                        }
                    });
                }
                if (avSession != null) {
                    if (!mScreenService.show(ScreenAV.class, Long.toString(avSession.getId()))) {
                        mScreenService.show(DashBoardActivity.class);
                    }
                } else {
                    Log.e(TAG, "Failed to find associated audio/video session");
                    mScreenService.show(DashBoardActivity.class);
                    mEngine.refreshAVCallNotif(R.drawable.phone_call_25);
                }

                break;
        }
    }

    public void setTime() {
        Date date = new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24));
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putLong(Time, date.getTime());
        editor.putBoolean(isTimeSaved, true);
        editor.commit();
    }

    private boolean getTime() {
        long appstartTime = sharedpreferences.getLong(Time, 0);
        if (appstartTime > System.currentTimeMillis()) {
            return true;
        }
        return false;
    }
}