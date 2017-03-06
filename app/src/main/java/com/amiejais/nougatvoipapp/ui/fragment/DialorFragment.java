package com.amiejais.nougatvoipapp.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.amiejais.nougatvoipapp.R;
import com.amiejais.nougatvoipapp.app.Engine;
import com.amiejais.nougatvoipapp.ui.activity.ScreenAV;
import com.amiejais.nougatvoipapp.utils.CustomDialog;
import com.amiejais.nougatvoipapp.utils.DialerUtils;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnStringUtils;

/**
 * Created by KNPX0678 on 03-Mar-17.
 */

public class DialorFragment extends Fragment {

    private static String TAG = DialorFragment.class.getCanonicalName();

    private EditText mEtNumber;
    private ImageButton mIbInputType;
    private  INgnSipService mSipService;
    private String mNumber="";

    public DialorFragment(){
        mSipService = Engine.getInstance().getSipService();
    }
    public DialorFragment(String number) {
        mSipService = Engine.getInstance().getSipService();
        mNumber=number;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fargment_dialer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEtNumber = (EditText) view.findViewById(R.id.screen_tab_dialer_editText_number);
        mEtNumber.setText(mNumber);
        DialerUtils.setDialerTextButton(getActivity(), R.id.screen_tab_dialer_button_0, "0", "+", DialerUtils.TAG_0, mOnDialerClick);
        DialerUtils.setDialerTextButton(getActivity(), R.id.screen_tab_dialer_button_1, "1", "", DialerUtils.TAG_1, mOnDialerClick);
        DialerUtils.setDialerTextButton(getActivity(), R.id.screen_tab_dialer_button_2, "2", "ABC", DialerUtils.TAG_2, mOnDialerClick);
        DialerUtils.setDialerTextButton(getActivity(), R.id.screen_tab_dialer_button_3, "3", "DEF", DialerUtils.TAG_3, mOnDialerClick);
        DialerUtils.setDialerTextButton(getActivity(), R.id.screen_tab_dialer_button_4, "4", "GHI", DialerUtils.TAG_4, mOnDialerClick);
        DialerUtils.setDialerTextButton(getActivity(), R.id.screen_tab_dialer_button_5, "5", "JKL", DialerUtils.TAG_5, mOnDialerClick);
        DialerUtils.setDialerTextButton(getActivity(), R.id.screen_tab_dialer_button_6, "6", "MNO", DialerUtils.TAG_6, mOnDialerClick);
        DialerUtils.setDialerTextButton(getActivity(), R.id.screen_tab_dialer_button_7, "7", "PQRS", DialerUtils.TAG_7, mOnDialerClick);
        DialerUtils.setDialerTextButton(getActivity(), R.id.screen_tab_dialer_button_8, "8", "TUV", DialerUtils.TAG_8, mOnDialerClick);
        DialerUtils.setDialerTextButton(getActivity(), R.id.screen_tab_dialer_button_9, "9", "WXYZ", DialerUtils.TAG_9, mOnDialerClick);
        DialerUtils.setDialerTextButton(getActivity(), R.id.screen_tab_dialer_button_star, "*", "", DialerUtils.TAG_STAR, mOnDialerClick);
        DialerUtils.setDialerTextButton(getActivity(), R.id.screen_tab_dialer_button_sharp, "#", "", DialerUtils.TAG_SHARP, mOnDialerClick);

        ImageView delete = (ImageView) view.findViewById(R.id.screen_tab_dialer_imageButton_input_type);
        delete.setTag(DialerUtils.TAG_DELETE);
        delete.setOnClickListener(mOnDialerClick);

        ImageView makeCall = (ImageView) view.findViewById(R.id.screen_tab_dialer_button_audio);
        makeCall.setTag(DialerUtils.TAG_AUDIO_CALL);
        makeCall.setOnClickListener(mOnDialerClick);
        mEtNumber.setFocusable(false);
        mEtNumber.setFocusableInTouchMode(false);


        view.findViewById(R.id.screen_tab_dialer_button_0).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });


    }

    private final View.OnClickListener mOnDialerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int tag = Integer.parseInt(v.getTag().toString());
            final String number = mEtNumber.getText().toString();

            if (tag == DialerUtils.TAG_DELETE) {
                final int selStart = mEtNumber.getSelectionStart();
                if (selStart > 0) {
                    final StringBuffer sb = new StringBuffer(number);
                    sb.delete(selStart - 1, selStart);
                    mEtNumber.setText(sb.toString());
                    mEtNumber.setSelection(selStart - 1);
                }
            } else if (tag == DialerUtils.TAG_AUDIO_CALL) {
                if (NgnStringUtils.isNullOrEmpty(number)) {
                    CustomDialog.showDialog(getActivity(), "Please enter the number");
                } else if (!mSipService.isRegistered()) {
                    CustomDialog.showDialog(getActivity(), "Please login to make call");
                } else {
                    ScreenAV.makeCall(number, NgnMediaType.Audio);
//                    mEtNumber.setText(NgnStringUtils.emptyValue());
                }
            } else {
                final String textToAppend = tag == DialerUtils.TAG_STAR ? "*" : (tag == DialerUtils.TAG_SHARP ? "#" : Integer.toString(tag));
                appendText(textToAppend);
            }
        }
    };


    private void appendText(String textToAppend) {
        final int selStart = mEtNumber.getSelectionStart();
        final StringBuffer sb = new StringBuffer(mEtNumber.getText().toString());
        sb.insert(selStart, textToAppend);
        mEtNumber.setText(sb.toString());
        mEtNumber.setSelection(selStart + 1);
    }

}
