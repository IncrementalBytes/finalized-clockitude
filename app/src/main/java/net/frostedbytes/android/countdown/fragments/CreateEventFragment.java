package net.frostedbytes.android.countdown.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;

import net.frostedbytes.android.countdown.BaseActivity;
import net.frostedbytes.android.countdown.R;
import net.frostedbytes.android.countdown.models.EventSummary;
import net.frostedbytes.android.countdown.utils.LogUtils;

import java.util.ArrayList;
import java.util.Locale;

import static net.frostedbytes.android.countdown.BaseActivity.BASE_TAG;

public class CreateEventFragment extends Fragment {

    private static final String TAG = BASE_TAG + CreateEventFragment.class.getSimpleName();

    public interface OnCreateEventListener {

        void onEventCreated(EventSummary eventSummary);
    }

    private OnCreateEventListener mCallback;

    private EditText mNameEditView;

    private int mDay;
    private ArrayList<EventSummary> mEventSummaries;
    private int mMonth;
    private int mYear;

    public static CreateEventFragment newInstance(ArrayList<EventSummary> eventSummaries) {

        LogUtils.debug(TAG, "++newInstance(ArrayList<EventSummary>)");
        CreateEventFragment fragment = new CreateEventFragment();
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
            mCallback = (OnCreateEventListener) context;
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
        final View view = inflater.inflate(R.layout.fragment_create, container, false);

        mNameEditView = view.findViewById(R.id.create_edit_name);
        CalendarView calendarView = view.findViewById(R.id.create_calendar_date);
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {

            mDay = dayOfMonth;
            mMonth = month + 1; // note: month value is based on 0-11
            mYear = year;
        });

        Button createButton = view.findViewById(R.id.create_button_update);
        createButton.setOnClickListener(buttonView -> {

            if (mCallback != null) {
                EventSummary eventSummary = new EventSummary();
                eventSummary.EventName = mNameEditView.getText().toString();
                eventSummary.EventDate = String.format(Locale.ENGLISH, "%04d-%02d-%02d", mYear, mMonth, mDay);

                // make sure we didn't create a similar event
                boolean found = false;
                for (EventSummary summary : mEventSummaries) {
                    if (summary.EventName.equals(eventSummary.EventName) && summary.EventDate.equals(eventSummary.EventDate)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    mCallback.onEventCreated(eventSummary);
                } else {
                    // TODO: display duplicate event message
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LogUtils.debug(TAG, "++onDestroy()");
        mCallback = null;
        mEventSummaries = null;
    }
}
