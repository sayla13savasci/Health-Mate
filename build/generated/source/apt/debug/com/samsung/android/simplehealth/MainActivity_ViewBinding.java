// Generated code from Butter Knife. Do not modify!
package com.samsung.android.simplehealth;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MainActivity_ViewBinding implements Unbinder {
  private MainActivity target;

  @UiThread
  public MainActivity_ViewBinding(MainActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public MainActivity_ViewBinding(MainActivity target, View source) {
    this.target = target;

    target.mStepCountTv = Utils.findRequiredViewAsType(source, R.id.stepCount, "field 'mStepCountTv'", TextView.class);
    target.mHeartRateCountTv = Utils.findRequiredViewAsType(source, R.id.heartRate, "field 'mHeartRateCountTv'", TextView.class);
    target.mWaterIntakeCountTv = Utils.findRequiredViewAsType(source, R.id.waterIntake, "field 'mWaterIntakeCountTv'", TextView.class);
    target.mBloodGlucoseTv = Utils.findRequiredViewAsType(source, R.id.bloodGlucose, "field 'mBloodGlucoseTv'", TextView.class);
    target.mWeightTv = Utils.findRequiredViewAsType(source, R.id.weightTracker, "field 'mWeightTv'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    MainActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mStepCountTv = null;
    target.mHeartRateCountTv = null;
    target.mWaterIntakeCountTv = null;
    target.mBloodGlucoseTv = null;
    target.mWeightTv = null;
  }
}
