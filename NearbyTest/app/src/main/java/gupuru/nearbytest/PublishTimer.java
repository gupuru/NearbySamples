package gupuru.nearbytest;

import android.os.Handler;

import java.util.TimerTask;

public class PublishTimer extends TimerTask {

    private OnPublishTimerListener onPublishTimerListener;
    private Handler handler;

    public interface OnPublishTimerListener {
        void onPublishTimerResult();
    }

    public void setOnPublishTimerListener(OnPublishTimerListener onPublishTimerListener) {
        this.onPublishTimerListener = onPublishTimerListener;
    }

    public PublishTimer(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        handler.post(new Runnable() {
            public void run() {
               onPublishTimerListener.onPublishTimerResult();
            }
        });
    }

}
