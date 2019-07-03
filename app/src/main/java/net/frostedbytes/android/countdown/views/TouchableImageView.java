package net.frostedbytes.android.countdown.views;

import android.content.Context;
import android.util.AttributeSet;

public class TouchableImageView extends androidx.appcompat.widget.AppCompatImageView {

  public TouchableImageView(Context context) {
    super(context);
  }

  public TouchableImageView(Context context, AttributeSet attrs) {
    super(context, attrs);

  }

  @Override
  public boolean performClick() {
    super.performClick();

    return true;
  }
}
