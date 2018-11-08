package net.frostedbytes.android.countdown.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import net.frostedbytes.android.countdown.BaseActivity;
import net.frostedbytes.android.countdown.R;
import net.frostedbytes.android.countdown.models.EventSummary;
import net.frostedbytes.android.countdown.utils.DateUtils;
import net.frostedbytes.android.countdown.utils.LogUtils;
import net.frostedbytes.android.countdown.views.TouchableImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.frostedbytes.android.countdown.BaseActivity.BASE_TAG;

public class EventListFragment extends Fragment {

    private static final String TAG = BASE_TAG + EventListFragment.class.getSimpleName();

    public interface OnEventListListener {

        void onCreateEvent();

        void onDeleteEvent(EventSummary eventSummary);

        void onSelected(EventSummary eventSummary);
    }

    private OnEventListListener mCallback;

    private RecyclerView mRecyclerView;

    private ArrayList<EventSummary> mEventSummaries;

    public static EventListFragment newInstance(ArrayList<EventSummary> eventSummaries) {

        LogUtils.debug(TAG, "++newInstance(ArrayList<EventSummary>)");
        EventListFragment fragment = new EventListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(BaseActivity.ARG_EVENT_SUMMARIES, eventSummaries);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        LogUtils.debug(TAG, "++onAttach(Context)");
        try {
            mCallback = (OnEventListListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                // TODO: update with list of events
                String.format(Locale.ENGLISH, "%s must implement TBD.", context.toString()));
        }

        Bundle arguments = getArguments();
        if (arguments != null) {
            mEventSummaries = arguments.getParcelableArrayList(BaseActivity.ARG_EVENT_SUMMARIES);
        } else {
            LogUtils.error(TAG, "Arguments were null.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LogUtils.debug(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
        final View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        mRecyclerView = view.findViewById(R.id.event_list_view_events);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        FloatingActionButton createButton = view.findViewById(R.id.event_list_button_create);
        if (mEventSummaries.size() > 0) {
            createButton.show();
            createButton.setOnClickListener(buttonView -> {

                if (mCallback != null) {
                    mCallback.onCreateEvent();
                }
            });
        } else {
            createButton.hide();
        }

        updateUI();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LogUtils.debug(TAG, "++onDestroy()");
        mCallback = null;
        mEventSummaries = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        LogUtils.debug(TAG, "++onResume()");
        updateUI();
    }

    private void updateUI() {

        LogUtils.debug(TAG, "++updateUI()");
        if (mEventSummaries != null && mEventSummaries.size() > 0) {
            EventSummaryAdapter eventAdapter = new EventSummaryAdapter(mEventSummaries);
            mRecyclerView.setAdapter(eventAdapter);
            eventAdapter.notifyDataSetChanged();
        }
    }

    private class EventSummaryAdapter extends RecyclerView.Adapter<EventSummaryHolder> {

        private final List<EventSummary> mEventSummaries;

        EventSummaryAdapter(List<EventSummary> eventSummaries) {

            mEventSummaries = eventSummaries;
        }

        @NonNull
        @Override
        public EventSummaryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new EventSummaryHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull EventSummaryHolder holder, int position) {

            EventSummary eventSummary = mEventSummaries.get(position);
            holder.bind(eventSummary);
        }

        @Override
        public int getItemCount() {
            return mEventSummaries.size();
        }
    }

    private class EventSummaryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mEventTitleTextView;
        private final TextView mEventDateTextView;
        private final Switch mEventActiveSwitch;
        private final TouchableImageView mDeleteEventImageView;

        private EventSummary mEventSummary;

        EventSummaryHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.event_item, parent, false));

            itemView.setOnClickListener(this);
            mEventTitleTextView = itemView.findViewById(R.id.event_item_title);
            mEventDateTextView = itemView.findViewById(R.id.event_item_date);
            mEventActiveSwitch = itemView.findViewById(R.id.event_item_active);
            mDeleteEventImageView = itemView.findViewById(R.id.event_item_delete);
            mDeleteEventImageView.setOnTouchListener((view, motionEvent) -> {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mCallback.onDeleteEvent(mEventSummary);
                        break;
                    case MotionEvent.ACTION_UP:
                        view.performClick();
                        return true;
                }

                return true;
            });
        }

        void bind(EventSummary eventSummary) {

            mEventSummary = eventSummary;
            mEventTitleTextView.setText(mEventSummary.EventName);
            mEventDateTextView.setText(DateUtils.formatDateForDisplay(mEventSummary.EventDate));
            if (mEventSummary.IsActive) {
                mEventActiveSwitch.setChecked(true);
            } else {
                mEventActiveSwitch.setChecked(false);
            }
        }

        @Override
        public void onClick(View view) {

            LogUtils.debug(TAG, "++MatchSummaryHolder::onClick(View)");
            mCallback.onSelected(mEventSummary);
        }
    }
}
