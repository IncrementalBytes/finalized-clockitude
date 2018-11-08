package net.frostedbytes.android.countdown.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class EventSummary implements Parcelable {

    public static final String ROOT = "Events";

    public String EventDate;

    @Exclude
    public String EventId;

    public String EventName;

    public boolean IsActive;

    public String UserId;

    public EventSummary() {

        this.EventDate = "";
        this.EventId = "";
        this.EventName = "";
        this.IsActive = false;
        this.UserId = "";
    }

    protected EventSummary(Parcel in) {

        this.EventDate = in.readString();
        this.EventId = in.readString();
        this.EventName = in.readString();
        this.IsActive = in.readInt() != 0;
        this.UserId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object other) {

        if (other instanceof EventSummary) {
            EventSummary otherEvent = (EventSummary) other;
            return this.EventName.equals(otherEvent.EventName) &&
                this.EventDate.equals(otherEvent.EventDate);
        }

        return false;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.EventDate);
        dest.writeString(this.EventId);
        dest.writeString(this.EventName);
        dest.writeInt(this.IsActive ? 1 : 0);
        dest.writeString(this.UserId);
    }

    /**
     * Creates a mapped object based on values of this event summary object
     * @return A mapped object of match summary
     */
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("EventDate", this.EventDate);
        result.put("EventName", this.EventName);
        result.put("IsActive", this.IsActive);
        result.put("UserId", this.UserId);
        return result;
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
