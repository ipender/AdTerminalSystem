package com.bupt.adsystem.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hadoop on 16-11-3.
 */
public class LifeCycleMgr {

    private static List<LifeCycle> sLifeCycleSet = new ArrayList<>();

    public static boolean registerLifeCycle(LifeCycle lifeCycle) {
        if (sLifeCycleSet == null) return false;
        if (sLifeCycleSet.add(lifeCycle)) return true;
        return false;
    }

    public static boolean unregisterLifeCycle(LifeCycle lifeCycle) {
        if (sLifeCycleSet == null) return false;
        if (sLifeCycleSet.remove(lifeCycle)) {
            return true;
        }
        return false;
    }

    public static void onStop() {
        if (sLifeCycleSet == null ) return;
        for (LifeCycle cycle : sLifeCycleSet) {
            cycle.toStop();
        }
    }

    public static void onResume() {
        if (sLifeCycleSet == null ) return;
        for (LifeCycle cycle : sLifeCycleSet) {
            cycle.toResume();
        }
    }

}
