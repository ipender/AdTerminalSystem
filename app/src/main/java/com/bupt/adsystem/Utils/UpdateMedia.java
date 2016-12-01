package com.bupt.adsystem.Utils;

import android.support.annotation.NonNull;

/**
 * Created by hadoop on 16-8-23.
 */
public interface UpdateMedia {

    void updateWhenAlarmUp(boolean isTimeStart);
    void updateWhenIntervalAddOrEdit(@NonNull String startTime, @NonNull String endTime);
    void updateWhenIntervalDelete(@NonNull String startTime, @NonNull String endTime);
    void updateWhenStrategyChanged();
    void updateWhenFileDelete();
    void updateWhenDownloadFinished();
}
