package gupuru.nearbytest;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import java.util.Random;
import java.util.Timer;

public class PassingActivity extends AppCompatActivity  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, PublishTimer.OnPublishTimerListener {

    private GoogleApiClient googleApiClient;
    private boolean mResolvingNearbyPermissionError = false;
    private Message mDeviceInfoMessage;
    private MessageListener messageListener;
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setDiscoveryMode(Strategy.DISCOVERY_MODE_DEFAULT)
            .setDistanceType(Strategy.DISTANCE_TYPE_DEFAULT)
            .setTtlSeconds(Strategy.TTL_SECONDS_DEFAULT)
            .build();
    //timer
    private Timer mTimer = null;
    private PublishTimer publishTimer;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passing);

        googleApiClient = new GoogleApiClient.Builder(PassingActivity.this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        messageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                final String nearbyMessageString = new String(message.getContent());
                // メッセージを受信した時の処理
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PassingActivity.this, "成功" + nearbyMessageString, Toast.LENGTH_LONG).show();
                    }
                });
            }

            public void onLost(final Message message) {
                final String nearbyMessageString = new String(message.getContent());
                // メッセージを受信した時の処理
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PassingActivity.this, "失敗しったぽい" + nearbyMessageString, Toast.LENGTH_LONG).show();
                    }
                });
            }
        };

    }

    @Override
    protected void onStart(){
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        if (null != googleApiClient && googleApiClient.isConnected()) {
            unsubscribe();
            unpublish();
            googleApiClient.disconnect();
        }
        if (mTimer != null) {
            mTimer.cancel();
            publishTimer.cancel();
            mTimer = null;
            publishTimer = null;
            mHandler = null;
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mDeviceInfoMessage = new Message(Constants.MESSAGE_INITIAL_VALUE.getBytes());
        Nearby.Messages.getPermissionStatus(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    subscribe();
                } else {
                    handleUnsuccessfulNearbyResult(status);
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_RESOLVE_ERROR) {
            mResolvingNearbyPermissionError = false;
            if (resultCode == Activity.RESULT_OK) {
                // メッセージの受信
                subscribe();
            } else {
                Toast.makeText(this, "Failed to resolve error with code " + resultCode,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * メッセージの送信
     */
    private void publish() {
        if (!googleApiClient.isConnected()) {
            if (!googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            String strMsg = "すれ違い通信だぜ" + Build.PRODUCT + new Random().nextInt(30);
            mDeviceInfoMessage = new Message(strMsg.getBytes());
            PublishOptions options = new PublishOptions.Builder()
                    .setStrategy(PUB_SUB_STRATEGY)
                    .setCallback(new PublishCallback() {
                        @Override
                        public void onExpired() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(PassingActivity.this, "PublishOptionsが終わった？？？？", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }).build();

            Nearby.Messages.publish(googleApiClient, mDeviceInfoMessage, options)
                    .setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(PassingActivity.this, "Published successfully", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Log.i("MainActivity", "Could not publish.");
                                handleUnsuccessfulNearbyResult(status);
                            }
                        }
                    });
        }
    }

    /**
     * メッセージの送信解除
     */
    private void unpublish() {
        Nearby.Messages.unpublish(googleApiClient, mDeviceInfoMessage);
    }

    /**
     * メッセージの受信
     */
    private void subscribe() {
        // GoogleApiClientに接続しているか確認
        if (!googleApiClient.isConnected()) {
            if (!googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            SubscribeOptions options = new SubscribeOptions.Builder()
                    .setStrategy(PUB_SUB_STRATEGY)
                    .setCallback(new SubscribeCallback() {
                        @Override
                        public void onExpired() {
                            super.onExpired();
                            // 受信状態が終了した時のコールバック
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(PassingActivity.this, "終わった", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    })
                    .build();

            Nearby.Messages.subscribe(googleApiClient, messageListener, options)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(PassingActivity.this, "Subscribed successfully", Toast.LENGTH_LONG).show();
                                    }
                                });
                                //タイマーインスタンス生成
                                mTimer = new Timer();
                                //タスククラスインスタンス生成
                                publishTimer = new PublishTimer(mHandler);
                                publishTimer.setOnPublishTimerListener(PassingActivity.this);
                                //タイマースケジュール設定＆開始
                                mTimer.scheduleAtFixedRate(publishTimer, 0, 10000);
                            } else {
                                Log.i("MainActivity", "Could not subscribe.");
                                // Check whether consent was given;
                                // if not, prompt the user for consent.
                                handleUnsuccessfulNearbyResult(status);
                            }
                        }
                    });
        }
    }

    /**
     * メッセージの受信解除
     */
    private void unsubscribe() {
        Nearby.Messages.unsubscribe(googleApiClient, messageListener);
    }

    private void handleUnsuccessfulNearbyResult(Status status) {
        if (status.getStatusCode() == NearbyMessagesStatusCodes.APP_NOT_OPTED_IN) {
            if (!mResolvingNearbyPermissionError) {
                try {
                    mResolvingNearbyPermissionError = true;
                    status.startResolutionForResult(PassingActivity.this,
                            Constants.REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (status.getStatusCode() == ConnectionResult.NETWORK_ERROR) {
                Toast.makeText(PassingActivity.this,
                        "なんかネットワークに問題あるよ",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(PassingActivity.this, "Unsuccessful: " +
                        status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onPublishTimerResult() {
        publish();
    }

}