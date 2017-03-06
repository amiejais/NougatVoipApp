package com.amiejais.nougatvoipapp.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amiejais.nougatvoipapp.R;
import com.amiejais.nougatvoipapp.ui.activity.DashBoardActivity;

import org.doubango.ngn.model.NgnContact;

/**
 * Created by KNPX0678 on 03-Mar-17.
 */

public class ContactDetailFragment extends Fragment {
    public NgnContact mSelectedContact;
    private LinearLayout mPhoneNumbersLayout;

    public ContactDetailFragment(){

    }
    public ContactDetailFragment(NgnContact pSelectedContact) {
        mSelectedContact = pSelectedContact;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fargment_contact_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPhoneNumbersLayout = (LinearLayout) view.findViewById(R.id.user_numbers);
        ImageView mContactImage = (ImageView) view.findViewById(R.id.iv_user_img);
        TextView mUserName = (TextView) view.findViewById(R.id.tv_username);

        mContactImage.setImageBitmap(mSelectedContact.getPhoto());
        mUserName.setText(mSelectedContact.getDisplayName());
        mUserName.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.color_black_light));
        displayAllNumbers();

    }

    private void displayAllNumbers() {
        for (int i = 0; i <= mSelectedContact.getPhoneNumbers().size() - 1; i++) {

            final TextView phoneNumer = new TextView(getActivity());
            phoneNumer.setId(i);
            phoneNumer.setTag(mSelectedContact.getPhoneNumbers().get(i).getNumber());
            phoneNumer.setPadding(10, 50, 50, 10);
            phoneNumer.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            phoneNumer.setTextSize(26f);
            phoneNumer.setTextColor(Color.BLACK);
            phoneNumer.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorTransparent));
            phoneNumer.setText(mSelectedContact.getPhoneNumbers().get(i).getNumber());
            phoneNumer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String number = (String) phoneNumer.getTag();
                    changeToCall(number);
//                    ScreenAV.makeCall(number, NgnMediaType.Audio);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 10, 10, 10);
            phoneNumer.setLayoutParams(layoutParams);
            mPhoneNumbersLayout.addView(phoneNumer, layoutParams);
            final TextView divider = new TextView(getActivity());
            divider.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.color_black));
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            divider.setLayoutParams(layoutParams);
            mPhoneNumbersLayout.addView(divider);
        }
    }

    private void changeToCall(String number) {
        ((DashBoardActivity) getActivity()).getMenuAndUpdate(2, new DialorFragment(number));
    }


}
