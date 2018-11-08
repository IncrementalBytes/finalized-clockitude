package net.frostedbytes.android.countdown.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import net.frostedbytes.android.countdown.BaseActivity;
import net.frostedbytes.android.countdown.R;
import net.frostedbytes.android.countdown.models.EventSummary;
import net.frostedbytes.android.countdown.utils.LogUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import static net.frostedbytes.android.countdown.BaseActivity.BASE_TAG;

public class CountdownFragment extends Fragment {

    private static final String TAG = BASE_TAG + CountdownFragment.class.getSimpleName();

    private Instant mCurrent;
    private EventSummary mEventSummary;

    public static CountdownFragment newInstance(EventSummary eventSummary) {

        LogUtils.debug(TAG, "++newInstance(EventSummary)");
        CountdownFragment fragment = new CountdownFragment();
        Bundle args = new Bundle();
        args.putParcelable(BaseActivity.ARG_EVENT_SUMMARY, eventSummary);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        LogUtils.debug(TAG, "++onAttach(Context)");
        mCurrent = Instant.now();
        Bundle arguments = getArguments();
        if (arguments != null) {
            mEventSummary = arguments.getParcelable(BaseActivity.ARG_EVENT_SUMMARY);
        } else {
            LogUtils.error(TAG, "Arguments were null.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LogUtils.debug(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
        final View view = inflater.inflate(R.layout.fragment_countdown, container, false);

        Instant eventInstant = Instant.parse(String.format("%sT00:00:00.00Z", mEventSummary.EventDate));
        eventInstant = eventInstant.plus(Duration.ofDays(1));
        Duration duration = Duration.between(mCurrent, eventInstant);

        TextView title = view.findViewById(R.id.countdown_text_title);
        title.setText(mEventSummary.EventName);

        TextView daysHeader = view.findViewById(R.id.countdown_text_days);
        daysHeader.setText(String.format(Locale.ENGLISH, "Day(s) until %s", mEventSummary.EventDate));
        EditText days = view.findViewById(R.id.countdown_edit_days);
        days.setText(String.format(Locale.ENGLISH, "%,d", duration.toDays()));

        TextView hoursHeader = view.findViewById(R.id.countdown_text_hours);
        hoursHeader.setText(String.format(Locale.ENGLISH, "Hour(s) until %s", mEventSummary.EventDate));
        EditText hours = view.findViewById(R.id.countdown_edit_hours);
        hours.setText(String.format(Locale.ENGLISH, "%,d", duration.toHours()));

        TextView minutesHeader = view.findViewById(R.id.countdown_text_minutes);
        minutesHeader.setText(String.format(Locale.ENGLISH, "Minute(s) until %s", mEventSummary.EventDate));
        EditText minutes = view.findViewById(R.id.countdown_edit_minutes);
        minutes.setText(String.format(Locale.ENGLISH, "%,d", duration.toMinutes()));

        // TODO: add timer that fires every minute to update values

        return view;
    }
}
