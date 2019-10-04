/*
 * Copyright 2019 Ryan Ward
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package net.frostedbytes.android.countdown.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;

import net.frostedbytes.android.countdown.BaseActivity;
import net.frostedbytes.android.countdown.R;
import net.frostedbytes.android.countdown.models.EventSummary;

import java.util.Calendar;
import java.util.Locale;

public class CreateEventFragment extends Fragment {

  private static final String TAG = BaseActivity.BASE_TAG + CreateEventFragment.class.getSimpleName();

  public interface OnCreateEventListener {

    void onEventCreated(EventSummary eventSummary);

    void onSetEventTime(EventSummary eventSummary);
  }

  private OnCreateEventListener mCallback;

  private EditText mNameEditView;

  private int mDay;
  private EventSummary mEventSummary;
  private int mHour;
  private int mMinute;
  private int mMonth;
  private int mYear;

  public static CreateEventFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return newInstance(new EventSummary());
  }

  public static CreateEventFragment newInstance(EventSummary eventSummary) {

    Log.d(TAG, "++newInstance(EventSummary)");
    Log.d(TAG, "Event=" + eventSummary.toString());
    CreateEventFragment fragment = new CreateEventFragment();
    Bundle args = new Bundle();
    args.putParcelable(BaseActivity.ARG_EVENT_SUMMARY, eventSummary);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnCreateEventListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(String.format(Locale.US, "Missing interface implementations for %s", context.toString()));
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    Bundle arguments = getArguments();
    if (arguments != null) {
      mEventSummary = arguments.getParcelable(BaseActivity.ARG_EVENT_SUMMARY);
    } else {
      Log.e(TAG, "Arguments were null.");
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    return inflater.inflate(R.layout.fragment_create, container, false);
  }

  @Override
  public void onDetach() {
    super.onDetach();

    Log.d(TAG, "++onDetach()");
    mCallback = null;
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d(TAG, "++onViewCreated(View, Bundle)");
    mNameEditView = view.findViewById(R.id.create_edit_name);
    EditText mTimeEditView = view.findViewById(R.id.create_edit_time);
    CalendarView calendarView = view.findViewById(R.id.create_calendar_date);
    Button setTimeButton = view.findViewById(R.id.create_button_time);

    if (mEventSummary != null && !mEventSummary.EventName.isEmpty()) {
      mNameEditView.setText(mEventSummary.EventName);
    }

    calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {

      mDay = dayOfMonth;
      mMonth = month; // note: month value is based on 0-11
      mYear = year;
    });

    Calendar calendar = Calendar.getInstance();
    mDay = calendar.get(Calendar.DATE);
    mMonth = calendar.get(Calendar.MONTH);
    mYear = calendar.get(Calendar.YEAR);
    if (mEventSummary != null && mEventSummary.EventDate > 0) {
      calendar.setTimeInMillis(mEventSummary.EventDate);
      calendarView.setDate(mEventSummary.EventDate);
      mDay = calendar.get(Calendar.DATE);
      mMonth = calendar.get(Calendar.MONTH);
      mYear = calendar.get(Calendar.YEAR);
      mHour = calendar.get(Calendar.HOUR_OF_DAY);
      mMinute = calendar.get(Calendar.MINUTE);
      mTimeEditView.setText(String.format(Locale.US, "%02d:%02d", mHour, mMinute));
    }

    setTimeButton.setOnClickListener(v -> {

      EventSummary eventSummary = new EventSummary();
      eventSummary.EventName = mNameEditView.getText().toString();
      eventSummary.CreatedDate = Calendar.getInstance().getTimeInMillis();
      calendar.set(mYear, mMonth, mDay, mHour, mMinute, 0);
      eventSummary.EventDate = calendar.getTimeInMillis();
      mCallback.onSetEventTime(eventSummary);
    });

    Button createButton = view.findViewById(R.id.create_button_update);
    createButton.setOnClickListener(buttonView -> {

      if (mCallback != null) {
        EventSummary eventSummary = new EventSummary();
        eventSummary.EventName = mNameEditView.getText().toString();
        eventSummary.CreatedDate = Calendar.getInstance().getTimeInMillis();
        calendar.set(mYear, mMonth, mDay, mHour, mMinute, 0);
        eventSummary.EventDate = calendar.getTimeInMillis();

        // make sure we didn't create a similar event
        mCallback.onEventCreated(eventSummary);
      }
    });
  }
}
