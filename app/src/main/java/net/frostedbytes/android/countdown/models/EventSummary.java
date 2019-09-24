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
package net.frostedbytes.android.countdown.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import net.frostedbytes.android.common.utils.TimeUtils;
import net.frostedbytes.android.countdown.BaseActivity;

import java.util.Calendar;
import java.util.Locale;

@IgnoreExtraProperties
public class EventSummary implements Parcelable {

  public static final String ROOT = "EventSummaries";

  public long CreatedDate;

  public long EventDate;

  @Exclude
  public String EventId;

  public String EventName;

  @Exclude
  public String UserId;

  public EventSummary() {

    CreatedDate = 0;
    EventDate = 0;
    EventId = BaseActivity.DEFAULT_EVENT_ID;
    EventName = "";
    UserId = BaseActivity.DEFAULT_USER_ID;
  }

  public EventSummary(EventSummary eventSummary) {

    CreatedDate = eventSummary.CreatedDate;
    EventDate = eventSummary.EventDate;
    EventId = eventSummary.EventId;
    EventName = eventSummary.EventName;
    UserId = eventSummary.UserId;
  }

  protected EventSummary(Parcel in) {

    CreatedDate = in.readLong();
    EventDate = in.readLong();
    EventId = in.readString();
    EventName = in.readString();
    UserId = in.readString();
  }

  @Exclude
  public int getPercentRemaining() {

    long difference = EventDate - CreatedDate;
    long elapsed = Calendar.getInstance().getTimeInMillis() - CreatedDate;
    return (int) (elapsed * 100 / difference);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public boolean equals(Object other) {

    if (other instanceof EventSummary) {
      EventSummary otherEvent = (EventSummary) other;
      return EventName.equals(otherEvent.EventName) && EventDate == otherEvent.EventDate;
    }

    return false;
  }

  @Override
  public String toString() {
    return String.format(
      Locale.US,
      "EventSummary { Name=%s, %s }",
      EventName,
      EventDate < Calendar.getInstance().getTimeInMillis() ? "Completed!" : "Date=" + TimeUtils.getFull(EventDate));
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {

    dest.writeLong(CreatedDate);
    dest.writeLong(EventDate);
    dest.writeString(EventId);
    dest.writeString(EventName);
  }

  public static final Creator<EventSummary> CREATOR = new Creator<EventSummary>() {
    @Override
    public EventSummary createFromParcel(Parcel in) {
      return new EventSummary(in);
    }

    @Override
    public EventSummary[] newArray(int size) {
      return new EventSummary[size];
    }
  };
}
