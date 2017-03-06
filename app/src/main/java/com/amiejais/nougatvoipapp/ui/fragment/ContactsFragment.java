package com.amiejais.nougatvoipapp.ui.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amiejais.nougatvoipapp.R;
import com.amiejais.nougatvoipapp.adapter.SeparatedListAdapter;
import com.amiejais.nougatvoipapp.app.Engine;

import org.doubango.ngn.model.NgnContact;
import org.doubango.ngn.utils.NgnGraphicsUtils;
import org.doubango.ngn.utils.NgnObservableList;
import org.doubango.ngn.utils.NgnStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by KNPX0678 on 03-Mar-17.
 */

public class ContactsFragment extends Fragment {

    private MySeparatedListAdapter mAdapter;
    private ListView mListView;
    private NgnContact mSelectedContact;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fargment_address_book, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = (ListView)view. findViewById(R.id.screen_tab_contacts_listView);
        mAdapter = new MySeparatedListAdapter(getActivity());

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mOnItemListViewClickListener);
//        mListView.setOnItemLongClickListener(mOnItemListViewLongClickListener);
    }



    /**
     * MySeparatedListAdapter
     */
    static class MySeparatedListAdapter extends SeparatedListAdapter implements Observer {
        private final LayoutInflater mInflater;
        private final Context mContext;
        private final Handler mHandler;
        private final NgnObservableList<NgnContact> mContacts;


        private MySeparatedListAdapter(Context context){
            super(context);
            mContext = context;
            mHandler = new Handler();
            mInflater = LayoutInflater.from(mContext);
            mContacts = Engine.getInstance().getContactService().getObservableContacts();
            mContacts.addObserver(this);

            updateSections();
            notifyDataSetChanged();
        }

        @Override
        protected void finalize() throws Throwable {
            Engine.getInstance().getContactService().getObservableContacts().deleteObserver(this);
            super.finalize();
        }

        private void updateSections(){
            clearSections();
            synchronized(mContacts){
                List<NgnContact> contacts = mContacts.getList();
                String lastGroup = "$", displayName;
                ScreenTabContactsAdapter lastAdapter = null;

                for(NgnContact contact : contacts){
                    displayName = contact.getDisplayName();
                    if(NgnStringUtils.isNullOrEmpty(displayName)){
                        continue;
                    }
                    final String group = displayName.substring(0, 1).toUpperCase();
                    if(!group.equalsIgnoreCase(lastGroup)){
                        lastGroup = group;
                        lastAdapter = new ScreenTabContactsAdapter(mContext, lastGroup);
                        addSection(lastGroup, lastAdapter);
                    }

                    if(lastAdapter != null){
                        lastAdapter.addContact(contact);
                    }
                }
            }
        }

        @Override
        protected View getHeaderView(int position, View convertView, ViewGroup parent, final Adapter adapter) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.view_list_header, parent, false);
            }
            TextView tvDisplayName = (TextView)convertView.findViewById(R.id.view_list_header_title);
            tvDisplayName.setText(((ScreenTabContactsAdapter)adapter).getSectionText());
            return convertView;
        }

        @Override
        public void update(Observable observable, Object data) {
            //if(Thread.currentThread() == Looper.getMainLooper().getThread()){
            //	updateSections();
            //	notifyDataSetChanged();
            //}
            //else{
            mHandler.post(new Runnable(){
                @Override
                public void run() {
                    updateSections();
                    notifyDataSetChanged();
                }
            });
            //}
        }
    }

    /**
     * ScreenTabContactsAdapter
     */
    static class ScreenTabContactsAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;

        private final Context mContext;
        private List<NgnContact> mContacts;
        private final String mSectionText;

        private ScreenTabContactsAdapter(Context context, String sectionText) {
            mContext = context;
            mSectionText = sectionText;
            mInflater = LayoutInflater.from(mContext);
        }

        public String getSectionText(){
            return mSectionText;
        }

        public void addContact(NgnContact contact){
            if(mContacts == null){
                mContacts = new ArrayList<NgnContact>();
            }
            mContacts.add(contact);
        }

        @Override
        public int getCount() {
            return mContacts==null ? 0: mContacts.size();
        }

        @Override
        public Object getItem(int position) {
            if(mContacts != null && mContacts.size()>position){
                return mContacts.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                view = mInflater.inflate(R.layout.fragment_contact_item, null);
            }
            final NgnContact contact = (NgnContact)getItem(position);

            if(contact != null){
                final ImageView ivAvatar = (ImageView) view.findViewById(R.id.screen_tab_contacts_item_imageView_avatar);
                if(ivAvatar != null){
                    final TextView tvDisplayName = (TextView) view.findViewById(R.id.screen_tab_contacts_item_textView_displayname);
                    tvDisplayName.setText(contact.getDisplayName());
                    final Bitmap avatar = contact.getPhoto();
                    if(avatar == null){
                        ivAvatar.setImageResource(R.drawable.avatar_48);
                    }
                    else{
                        ivAvatar.setImageBitmap(NgnGraphicsUtils.getResizedBitmap(avatar, NgnGraphicsUtils.getSizeInPixel(48), NgnGraphicsUtils.getSizeInPixel(48)));
                    }
                }
            }

            return view;
        }
    }

    private final AdapterView.OnItemClickListener mOnItemListViewClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mSelectedContact = (NgnContact)parent.getItemAtPosition(position);
            if(mSelectedContact != null){
                ContactDetailFragment contactDetailFragment=new ContactDetailFragment(mSelectedContact);
               getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, contactDetailFragment).addToBackStack(null).commit();
            }
        }
    };
}
