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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.frostedbytes.android.common.utils.TimeUtils;
import net.frostedbytes.android.countdown.BaseActivity;
import net.frostedbytes.android.countdown.R;
import net.frostedbytes.android.countdown.models.EventSummary;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.joda.time.Interval;
import org.joda.time.Period;

public class CountdownFragment extends Fragment {

  private static final String TAG = BaseActivity.BASE_TAG + CountdownFragment.class.getSimpleName();

  public interface OnCountdownListener {

    void onSchedulerFailed();
  }

  private OnCountdownListener mCallback;

  private EventSummary mEventSummary;
  private ScheduledExecutorService mScheduler;
  private int mTaskScheduled;

  private TextView mCreated;
  private ProgressBar mProgress;
  private TextView mProgressText;
  private EditText mRemainingDaysEdit;
  private EditText mRemainingTimeEdit;

  public static CountdownFragment newInstance(EventSummary eventSummary) {

    Log.d(TAG, "++newInstance(EventSummary)");
    Log.d(TAG, "Event=" + eventSummary.toString());
    CountdownFragment fragment = new CountdownFragment();
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
      mCallback = (OnCountdownListener) context;
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
    return inflater.inflate(R.layout.fragment_countdown, container, false);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "++onDestroy()");
    if (mScheduler != null) {
      mScheduler.shutdown();
      mScheduler = null;
      Log.d(TAG, "Scheduler shutdown!");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();

    Log.d(TAG, "++onDetach()");
    mCallback = null;
  }

  @Override
  public void onResume() {
    super.onResume();

    Log.d(TAG, "++onResume()");
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d(TAG, "++onViewCreated(View, Bundle)");
    TextView title = view.findViewById(R.id.countdown_text_title);
    title.setText(mEventSummary.EventName);
    TextView date = view.findViewById(R.id.countdown_text_date);
    date.setText(TimeUtils.getFull(mEventSummary.EventDate));

    mCreated = view.findViewById(R.id.countdown_text_created_value);
    mProgress = view.findViewById(R.id.countdown_progress);
    mProgressText = view.findViewById(R.id.countdown_text_progress);
    mRemainingDaysEdit = view.findViewById(R.id.countdown_edit_remaining_days);
    mRemainingDaysEdit.setText(getString(R.string.calculating));
    mRemainingTimeEdit = view.findViewById(R.id.countdown_edit_remaining_time);
    mRemainingTimeEdit.setText(getString(R.string.calculating));

    mProgress.setMax(100);
    mProgress.setMin(0);

    mScheduler = Executors.newScheduledThreadPool(1);

    mTaskScheduled = BaseActivity.ONE_MINUTE; // update every minute
    if ((mEventSummary.EventDate - Calendar.getInstance().getTimeInMillis()) < BaseActivity.ONE_DAY) {
      Log.d(TAG, "Within a day of the event; setting schedule to 1 second.");
      mTaskScheduled = BaseActivity.ONE_SECOND; // update every second
    }

//    mScheduler.scheduleAtFixedRate(
//      new Runnable() {
//        private Runnable update = () -> updateUI();
//
//        @Override
//        public void run() {
//
//          try {
//            if (getActivity() != null) {
//              getActivity().runOnUiThread(update);
//            } else {
//              mCallback.onSchedulerFailed();
//            }
//          } catch (Exception ex) {
//            Log.warn(TAG, "Exception when scheduling thread.", ex.getMessage());
//          }
//        }
//      },
//      1,
//      mTaskScheduled,
//      TimeUnit.MILLISECONDS);
    updateUI();
  }

  /*
    Private Method(s)
   */
  private void updateUI() {

    if (mTaskScheduled == BaseActivity.ONE_MINUTE) {
      Log.d(TAG, "++updateUI()");
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.US);
    Calendar eventCalendar = Calendar.getInstance();
    eventCalendar.setTimeInMillis(mEventSummary.EventDate);
    try {
      Date eventDate = simpleDateFormat.parse(eventCalendar.getTime().toString());
      Date startDate = simpleDateFormat.parse(Calendar.getInstance().getTime().toString());
      int percentRemaining = mEventSummary.getPercentRemaining();
      mProgress.setProgress(percentRemaining, false);
      mCreated.setText(TimeUtils.getFull(mEventSummary.CreatedDate));
      if (startDate.getTime() < eventDate.getTime()) {
        Interval interval = new Interval(startDate.getTime(), eventDate.getTime());
        Period period = interval.toPeriod();

        // construct format for remaining time
        mRemainingDaysEdit.setText(
          String.format(
            Locale.US,
            "%d Year(s), %d Month(s), %d Day(s)",
            period.getYears(),
            period.getMonths(),
            period.getDays()));
        mRemainingTimeEdit.setText(
          String.format(
            Locale.US,
            "%d Hours, %d Minutes, %d Seconds",
            period.getHours(),
            period.getMinutes(),
            period.getSeconds()));
        mProgressText.setText(String.format(Locale.US, "%d%%", percentRemaining));
      } else {
        if (mScheduler != null) {
          mScheduler.shutdown();
          mScheduler = null;
          Log.d(TAG, "Scheduler shutdown!");
        }

        mRemainingDaysEdit.setText(getString(R.string.complete));
        mRemainingTimeEdit.setText(getString(R.string.complete));
        mProgressText.setText(getString(R.string.one_hundred_percent));
      }
    } catch (Exception pe) {
      Log.w(TAG, pe);
      mRemainingDaysEdit.setText(getString(R.string.unknown));
      mRemainingTimeEdit.setText(getString(R.string.unknown));
    }
  }
}
