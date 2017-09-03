package com.bupt.adsystem.model;

import android.os.Looper;

import com.bupt.adsystem.exception.NotInMainThreadException;

/**
 * Created by hadoop on 17-9-3.
 * 因为此领域对象模型代表此终端信息，全程只需实例化一次
 * 此对象只允许在主进程修改，所以使用单例模式设计此类
 */
public class ElevatorInfo {

    private int isRepair = 0;
    private int moveDir = 0;
    private int battery = 0;
    private int doorOpen = 0;
    private int hasPerson = 0;
    private int CFloor = 0;
    private int CSignal = 0;

    private static class Singleton {
        private static ElevatorInfo sElevatorInfo = new ElevatorInfo();
    }

    public static ElevatorInfo instance() {
        return Singleton.sElevatorInfo;
    }

    public int getIsRepair() {
        return isRepair;
    }

    public int getMoveDir() {
        return moveDir;
    }

    public int getBattery() {
        return battery;
    }

    public int getDoorOpen() {
        return doorOpen;
    }

    public int getHasPerson() {
        return hasPerson;
    }

    public int getCFloor() {
        return CFloor;
    }

    public int getCSignal() {
        return CSignal;
    }

    public void setIsRepair(int isRepair) {
        checkRunInMainThread();
        this.isRepair = isRepair;
    }

    public void setMoveDir(int moveDir) {
        checkRunInMainThread();
        this.moveDir = moveDir;
    }

    public void setBattery(int battery) {
        checkRunInMainThread();
        this.battery = battery;
    }

    public void setDoorOpen(int doorOpen) {
        checkRunInMainThread();
        this.doorOpen = doorOpen;
    }

    public void setHasPerson(int hasPerson) {
        checkRunInMainThread();
        this.hasPerson = hasPerson;
    }

    public void setCFloor(int CFloor) {
        checkRunInMainThread();
        this.CFloor = CFloor;
    }

    public void setCSignal(int CSignal) {
        checkRunInMainThread();
        this.CSignal = CSignal;
    }

    private void checkRunInMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new NotInMainThreadException();
        }
    }

}
