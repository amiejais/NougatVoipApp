package com.amiejais.nougatvoipapp.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amiejais.nougatvoipapp.R;
import com.amiejais.nougatvoipapp.app.Engine;
import com.amiejais.nougatvoipapp.ui.activity.ScreenAV;
import com.amiejais.nougatvoipapp.utils.CustomDialog;
import com.amiejais.nougatvoipapp.utils.DateTimeUtils;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryAVCallEvent;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnUriUtils;

import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by KNPX0678 on 03-Mar-17.
 */

public class HistoryFragment extends Fragment {

    private static final String TAG = HistoryFragment.class.getCanonicalName();

    private INgnHistoryService mHistorytService;
    private INgnSipService mSipService;

    private NgnHistoryEvent mSelectedEvent;
    private ScreenTabHistoryAdapter mAdapter;
    private ListView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fargment_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mHistorytService = Engine.getInstance().getHistoryService();
        mSipService = Engine.getInstance().getSipService();

        mAdapter = new ScreenTabHistoryAdapter(getActivity());
        mListView = (ListView) view.findViewById(R.id.screen_tab_history_listView);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mOnItemListViewClickListener);
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mHistorytService.isLoading()) {
            Toast.makeText(getActivity(), "Loading history...", Toast.LENGTH_SHORT).show();
        }
    }

    //
    // ScreenTabHistoryAdapter
    //
    public class ScreenTabHistoryAdapter extends BaseAdapter implements Observer {
        private List<NgnHistoryEvent> mEvents;
        private final LayoutInflater mInflater;
        private final Handler mHandler;

        private final static int TYPE_ITEM_AV = 0;
        private final static int TYPE_ITEM_SMS = 1;
        private final static int TYPE_ITEM_FILE_TRANSFER = 2;
        private final static int TYPE_COUNT = 3;

        public ScreenTabHistoryAdapter(FragmentActivity activity) {
            mHandler = new Handler();
            mInflater = LayoutInflater.from(activity);
            mEvents = mHistorytService.getObservableEvents()
                    .filter(new NgnHistoryAVCallEvent.HistoryEventAVFilter());
            mHistorytService.getObservableEvents().addObserver(this);
        }

        @Override
        protected void finalize() throws Throwable {
            mHistorytService.getObservableEvents().deleteObserver(this);
            super.finalize();
        }

        @Override
        public int getViewTypeCount() {
            return TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            final NgnHistoryEvent event = (NgnHistoryEvent) getItem(position);
            if (event != null) {
                switch (event.getMediaType()) {
                    case Audio:
                    case AudioVideo:
                    default:
                        return TYPE_ITEM_AV;
                    case FileTransfer:
                        return TYPE_ITEM_FILE_TRANSFER;
                    case SMS:
                        return TYPE_ITEM_SMS;
                }
            }
            return TYPE_ITEM_AV;
        }

        @Override
        public int getCount() {
            return mEvents.size();
        }

        @Override
        public Object getItem(int position) {
            return mEvents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void update(Observable observable, Object data) {
            mEvents = mHistorytService.getObservableEvents().filter(new NgnHistoryAVCallEvent.HistoryEventAVFilter());
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                notifyDataSetChanged();
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            final NgnHistoryEvent event = (NgnHistoryEvent) getItem(position);
            if (event == null) {
                return null;
            }
            if (view == null) {
                switch (event.getMediaType()) {
                    case Audio:
                    case AudioVideo:
                        view = mInflater.inflate(R.layout.screen_tab_history_item_av, null);
                        break;
                    case FileTransfer:
                    case SMS:
                    default:
                        Log.e(TAG, "Invalid media type");
                        return null;
                }
            }

            String remoteParty = NgnUriUtils.getDisplayName(event.getRemoteParty());

            if (event != null) {
                switch (event.getMediaType()) {
                    case Audio:
                    case AudioVideo:
                        final ImageView ivType = (ImageView) view.findViewById(R.id.screen_tab_history_item_av_imageView_type);
                        final TextView tvRemote = (TextView) view.findViewById(R.id.screen_tab_history_item_av_textView_remote);
                        final TextView tvDate = (TextView) view.findViewById(R.id.screen_tab_history_item_av_textView_date);
                        final String date = DateTimeUtils.getFriendlyDateString(new Date(event.getStartTime()));
                        tvDate.setText(date);
                        tvRemote.setText(remoteParty);
                        switch (event.getStatus()) {
                            case Outgoing:
                                ivType.setImageResource(R.drawable.call_outgoing_45);
                                break;
                            case Incoming:
                                ivType.setImageResource(R.drawable.call_incoming_45);
                                break;
                            case Failed:
                            case Missed:
                                ivType.setImageResource(R.drawable.call_missed_45);
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }

            return view;
        }
    }

    private final AdapterView.OnItemClickListener mOnItemListViewClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!mSipService.isRegistered()) {
                CustomDialog.showDialog(getActivity(), "User not logged In");
                Log.e(TAG, "Not registered yet");
                return;
            }
            mSelectedEvent = (NgnHistoryEvent) parent.getItemAtPosition(position);
            if (mSelectedEvent != null) {
                ScreenAV.makeCall(mSelectedEvent.getRemoteParty(), NgnMediaType.Audio);
            }
        }
    };


}
