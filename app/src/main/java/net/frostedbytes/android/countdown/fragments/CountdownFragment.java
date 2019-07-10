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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.frostedbytes.android.countdown.BaseActivity;
import net.frostedbytes.android.countdown.R;
import net.frostedbytes.android.countdown.common.DateUtils;
import net.frostedbytes.android.countdown.models.EventSummary;
import net.frostedbytes.android.countdown.common.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

  private ProgressBar mProgress;
  private EditText mRemainingDaysEdit;
  private EditText mRemainingTimeEdit;

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
    try {
      mCallback = (OnCountdownListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
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
    final View view = inflater.inflate(R.layout.fragment_countdown, container, false);

    TextView title = view.findViewById(R.id.countdown_text_title);
    title.setText(mEventSummary.EventName);
    TextView date = view.findViewById(R.id.countdown_text_date);
    date.setText(DateUtils.formatDateForDisplay(mEventSummary.EventDate));

    mProgress = view.findViewById(R.id.countdown_progress);
    mRemainingDaysEdit = view.findViewById(R.id.countdown_edit_remaining_days);
    mRemainingTimeEdit = view.findViewById(R.id.countdown_edit_remaining_time);

    mProgress.setMax(100);
    mProgress.setMin(0);

    mScheduler = Executors.newScheduledThreadPool(1);

    int taskScheduled = 60; // update every minute
    long oneDay = 1000 * 60 * 60 * 24;
    if ((mEventSummary.EventDate - Calendar.getInstance().getTimeInMillis()) < oneDay) {
      taskScheduled = 1; // update every second
    }

    mScheduler.scheduleAtFixedRate(
      new Runnable() {
        private Runnable update = () -> updateUI();

        @Override
        public void run() {

          try {
            if (getActivity() != null) {
              getActivity().runOnUiThread(update);
            } else {
              mCallback.onSchedulerFailed();
            }
          } catch (Exception ex) {
            LogUtils.warn(TAG, "Exception when scheduling thread: %s", ex.getMessage());
          }
        }
      },
      1,
      taskScheduled,
      TimeUnit.SECONDS);

    return view;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    LogUtils.debug(TAG, "++onDestroy()");
    if (mScheduler != null) {
      mScheduler.shutdown();
      mScheduler = null;
      LogUtils.debug(TAG, "Scheduler shutdown!");
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    LogUtils.debug(TAG, "++onResume()");
    updateUI();
  }

  private void updateUI() {

    LogUtils.debug(TAG, "++updateUI()");

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.US);
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(mEventSummary.EventDate);
    try {
      Date startDate = simpleDateFormat.parse(calendar.getTime().toString());
      Date currentDate = simpleDateFormat.parse(Calendar.getInstance().getTime().toString());
      if (currentDate.getTime() < startDate.getTime()) {
        Interval interval = new Interval(currentDate.getTime(), startDate.getTime());
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
        mProgress.setProgress(mEventSummary.getPercentRemaining(), false);
      } else {
        if (mScheduler != null) {
          mScheduler.shutdown();
          mScheduler = null;
          LogUtils.debug(TAG, "Scheduler shutdown!");
        }

        mRemainingDaysEdit.setText(getString(R.string.completed));
        mRemainingTimeEdit.setText(getString(R.string.completed));
      }
    } catch (Exception pe) {
      LogUtils.warn(TAG, pe.getMessage());
      mRemainingDaysEdit.setText(getString(R.string.unknown));
      mRemainingTimeEdit.setText(getString(R.string.unknown));
    }
  }
}
