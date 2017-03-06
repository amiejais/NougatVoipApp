package com.amiejais.nougatvoipapp.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amiejais.nougatvoipapp.R;
import com.amiejais.nougatvoipapp.app.Engine;
import com.amiejais.nougatvoipapp.utils.CustomDialog;

import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnSipSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;


public class HomeFragment extends Fragment {


    private static final String TAG = "HomeFragment";
    private EditText mUserName;
    private EditText mPassword;
    private TextView mRegisterText;
    private EditText mCallerId;
    private INgnSipService mSipService;
    private INgnConfigurationService mConfigurationService;
    private BroadcastReceiver mSipBroadCastRecv;
    private Button mConnect;
    private Button mReset;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fargment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSipService = Engine.getInstance().getSipService();
        mConfigurationService = Engine.getInstance().getConfigurationService();
        initView(view);
    }

    private void initView(View view) {
        mRegisterText = (TextView) view.findViewById(R.id.tv_register_text);
        mUserName = (EditText) view.findViewById(R.id.edt_username);
        mPassword = (EditText) view.findViewById(R.id.edt_password);
        mCallerId = (EditText) view.findViewById(R.id.edt_caller_id);

        mUserName.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI));
        mPassword.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_PASSWORD, NgnConfigurationEntry.DEFAULT_IDENTITY_IMPU));
        mCallerId.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI));

        mUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCallerId.setText(mUserName.getText());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mConnect = (Button) view.findViewById(R.id.btn_connect);
        mReset = (Button) view.findViewById(R.id.btn_reset);

        mConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(true);
            }
        });
        mReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(false);
            }
        });
        displayStatus();
    }


    private void displayStatus() {

        String tempState = "";
        if (mSipService.getRegistrationState() == NgnSipSession.ConnectionState.CONNECTING || mSipService.getRegistrationState() == NgnSipSession.ConnectionState.TERMINATING) {
            tempState = "Status: Connecting... ";
        } else if (mSipService.isRegistered()) {
            tempState = "Status: Connected";
        } else {
            tempState = "Status: Not Connected";
        }
        mRegisterText.setText("" + tempState);
    }

    private void register(boolean connect) {
        if (mSipService.getRegistrationState() == NgnSipSession.ConnectionState.CONNECTING || mSipService.getRegistrationState() == NgnSipSession.ConnectionState.TERMINATING) {
            mSipService.stopStack();
        } else if (mSipService.isRegistered()) {
            mSipService.unRegister();
        } else if (connect) {
            if (TextUtils.isEmpty(mUserName.getText())) {
                CustomDialog.showDialog(getActivity(), "Please enter the username");
            } else if (TextUtils.isEmpty(mPassword.getText())) {
                CustomDialog.showDialog(getActivity(), "Please enter the password");
            } else if (!TextUtils.isEmpty(mUserName.getText()) && !TextUtils.isEmpty(mPassword.getText())) {
                if (mUserName.getText().toString().equalsIgnoreCase("9910508758") || mUserName.getText().toString().equalsIgnoreCase("8076035240")) {
                    mRegisterText.setText("Status: Connecting...");
                    saveLoginDetails();
                    mSipService.register(getActivity());
                } else {
                    CustomDialog.showDialog(getActivity(), "Please enter the given number");
                }

            }

        }

        mSipBroadCastRecv = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                // Registration Event
                if (NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)) {
                    String tempStatus = "";
                    NgnRegistrationEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);

                    if (args == null) {
                        Log.e(TAG, "Invalid event args");
                        return;
                    }
                    switch (args.getEventType()) {
                        case REGISTRATION_NOK:
                            tempStatus = "Not Connected";
                            break;
                        case UNREGISTRATION_OK:
                            tempStatus = "Not Connected";
                            break;
                        case REGISTRATION_OK:
                            tempStatus = "Connected";
                            break;
                        case REGISTRATION_INPROGRESS:
                            tempStatus = "Connecting...";
                            break;
                        case UNREGISTRATION_INPROGRESS:
                            tempStatus = "Connected";
                            break;
                        case UNREGISTRATION_NOK:
                            tempStatus = "Not Connected";
                            break;
                        default:
                            break;
                    }

                    mRegisterText.setText("Status: " + tempStatus);
                    mConnect.setEnabled(true);
                }
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
        getActivity().registerReceiver(mSipBroadCastRecv, intentFilter);
    }

    private void saveLoginDetails() {
        mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU,
                "sip:" + mUserName.getText().toString() + "@sip2sip.info");
        mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI,
                mUserName.getText().toString().trim());
        mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD,
                mPassword.getText().toString().trim());

        // Compute
        if (!mConfigurationService.commit()) {
            Log.e(TAG, "Failed to Commit() configuration");
        }
    }


}
