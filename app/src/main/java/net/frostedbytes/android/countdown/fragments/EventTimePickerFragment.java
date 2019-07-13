package net.frostedbytes.android.countdown.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import net.frostedbytes.android.countdown.BaseActivity;
import net.frostedbytes.android.countdown.R;
import net.frostedbytes.android.countdown.common.DateUtils;
import net.frostedbytes.android.countdown.common.LogUtils;
import net.frostedbytes.android.countdown.models.EventSummary;

import java.util.Calendar;
import java.util.Locale;

public class EventTimePickerFragment extends Fragment {

  private static final String TAG = BaseActivity.BASE_TAG + "EventTimePickerFragment";

  private OnEventTimeSetListener mCallback;

  public interface OnEventTimeSetListener {

    void onEventTimeSet(EventSummary eventSummary);
  }

  private TimePicker mTimePicker;

  private EventSummary mEventSummary;

  public static EventTimePickerFragment newInstance(EventSummary eventSummary) {

    LogUtils.debug(TAG, "++newInstance(long)");
    Bundle args = new Bundle();
    args.putParcelable(BaseActivity.ARG_EVENT_SUMMARY, eventSummary);
    EventTimePickerFragment fragment = new EventTimePickerFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    LogUtils.debug(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnEventTimeSetListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        // TODO: update with list of events
        String.format(Locale.US, "%s must implement TBD.", context.toString()));
    }

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
    final View view = inflater.inflate(R.layout.fragment_time_picker, container, false);

    mTimePicker = view.findViewById(R.id.time_picker);
    if (mEventSummary != null && mEventSummary.EventDate > 0) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(mEventSummary.EventDate);
      mTimePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
      mTimePicker.setMinute(calendar.get(Calendar.MINUTE));
    } else {
      mTimePicker.setHour(0);
      mTimePicker.setMinute(0);
    }

    Button okButton = view.findViewById(R.id.time_button_ok);
    okButton.setOnClickListener(v -> {

      EventSummary eventSummary = new EventSummary(mEventSummary);
      Calendar previous = Calendar.getInstance();
      previous.setTimeInMillis(eventSummary.EventDate);
      LogUtils.debug(TAG, "EventTime was: %s", DateUtils.formatDateForDisplay(previous.getTimeInMillis()));
      previous.set(Calendar.HOUR_OF_DAY, mTimePicker.getHour());
      previous.set(Calendar.MINUTE, mTimePicker.getMinute());
      eventSummary.EventDate = previous.getTimeInMillis();
      LogUtils.debug(TAG, "EventTime is: %s", DateUtils.formatDateForDisplay(eventSummary.EventDate));
      mCallback.onEventTimeSet(eventSummary);
    });

    Button cancelButton = view.findViewById(R.id.time_button_cancel);
    cancelButton.setOnClickListener(v -> mCallback.onEventTimeSet(mEventSummary));

    return view;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    LogUtils.debug(TAG, "++onDestroy()");
    mCallback = null;
  }
}
