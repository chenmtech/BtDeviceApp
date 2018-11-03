package com.cmtech.android.bledevice.ecgmonitor;

import android.content.Context;
import android.util.AttributeSet;

import com.cmtech.android.bledevice.ecgmonitor.ecgfile.EcgFile;
import com.cmtech.dsp.exception.FileException;

import java.util.Timer;
import java.util.TimerTask;

import static com.vise.utils.handler.HandlerUtil.runOnUiThread;

public class EcgFileReelWaveView extends ReelWaveView {
    private EcgFile ecgFile;

    private boolean replaying = false;

    public boolean isReplaying() {
        return replaying;
    }

    private int interval = 20;

    private class ShowTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        showData(ecgFile.readData());
                    } catch (FileException e) {
                        stopShow();
                    }
                }
            });
        }
    }
    private Timer showTimer;


    public EcgFileReelWaveView(Context context) {
        super(context);
    }

    public EcgFileReelWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEcgFile(EcgFile ecgFile) {
        stopShow();
        this.ecgFile = ecgFile;
        interval = 1000/ecgFile.getFs();
        ecgFile.seek(0);
    }

    public void startShow() {
        if(!replaying) {
            showTimer = new Timer();
            showTimer.scheduleAtFixedRate(new ShowTask(), interval, interval);
            replaying = true;
        }
    }

    public void stopShow() {
        if(replaying) {
            showTimer.cancel();
            showTimer = null;
            replaying = false;
        }
    }

    public void switchState() {
        if(replaying) {
            stopShow();
        } else {
            startShow();
        }
    }
}
