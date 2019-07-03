package net.frostedbytes.android.countdown.common;

import net.frostedbytes.android.countdown.models.EventSummary;

import java.util.Comparator;

public class SortUtils {

  public static class ByEventDate implements Comparator<EventSummary> {

    public int compare(EventSummary a, EventSummary b) {

      return Long.compare(a.EventDate, b.EventDate);
    }
  }
}
