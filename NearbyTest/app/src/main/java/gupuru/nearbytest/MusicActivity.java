package gupuru.nearbytest;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.gson.Gson;

import java.io.IOException;

public class MusicActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private GoogleApiClient googleApiClient;
    private boolean mResolvingNearbyPermissionError = false;
    private Message mDeviceInfoMessage;
    private MessageListener messageListener;
    private MediaPlayer mediaPlayer;
    private TextView stateTextView;
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setDiscoveryMode(Strategy.DISCOVERY_MODE_DEFAULT)
            .setDistanceType(Strategy.DISTANCE_TYPE_DEFAULT)
            .setTtlSeconds(Strategy.TTL_SECONDS_DEFAULT)
            .build();

    //region lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        googleApiClient = new GoogleApiClient.Builder(MusicActivity.this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mediaPlayer = MediaPlayer.create(this, R.raw.music);
        stateTextView = (TextView) findViewById(R.id.state);

        messageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                music(new String(message.getContent()));
            }

            public void onLost(final Message message) {
                // メッセージを受信失敗したとき
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MusicActivity.this, "失敗しったぽい", Toast.LENGTH_LONG).show();
                    }
                });
            }
        };

        Button startBtn = (Button) findViewById(R.id.start);
        startBtn.setOnClickListener(this);
        Button stopBtn = (Button) findViewById(R.id.stop);
        stopBtn.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //googleApiClient 接続
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        //googleApiClient, nearbyapi解除
        if (null != googleApiClient && googleApiClient.isConnected()) {
            unsubscribe();
            unpublish();
            googleApiClient.disconnect();
        }
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        super.onStop();
    }

    //endregion

    //region googleApiClient callback

    @Override
    public void onConnected(Bundle bundle) {
        mDeviceInfoMessage = new Message(Constants.MESSAGE_INITIAL_VALUE.getBytes());
        //権限確認
        Nearby.Messages.getPermissionStatus(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    //権限OK済み -> 受信
                    subscribe();
                } else {
                    //権限NGまたは、初回 -> 権限ダイアログ表示
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

    //endregion

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                //送信
                publish(Constants.MUSIC_START);
                break;
            case R.id.stop:
                //送信
                publish(Constants.MUSIC_STOP);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_RESOLVE_ERROR) {
            mResolvingNearbyPermissionError = false;
            if (resultCode == Activity.RESULT_OK) {
                // メッセージ受信
                subscribe();
            } else {
                Toast.makeText(this, "Failed to resolve error with code " + resultCode,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    //region publish

    /**
     * メッセージ送信
     */
    private void publish(String action) {
        if (!googleApiClient.isConnected()) {
            if (!googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {

            MusicEntity musicEntity = new MusicEntity(action, System.currentTimeMillis());
            String data = new Gson().toJson(musicEntity);

            mDeviceInfoMessage = new Message(data.getBytes());
            PublishOptions options = new PublishOptions.Builder()
                    .setStrategy(PUB_SUB_STRATEGY)
                    .setCallback(new PublishCallback() {
                        @Override
                        public void onExpired() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MusicActivity.this, "通信終了したっぽい", Toast.LENGTH_LONG).show();
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
                                        Toast.makeText(MusicActivity.this, "送信成功したっぽい", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                //失敗
                                handleUnsuccessfulNearbyResult(status);
                            }
                        }
                    });
        }
    }

    /**
     * メッセージ送信解除
     */
    private void unpublish() {
        Nearby.Messages.unpublish(googleApiClient, mDeviceInfoMessage);
    }

    //endregion

    //region subscribe

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
                                    Toast.makeText(MusicActivity.this, "終了したっぽい", Toast.LENGTH_LONG).show();
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
                                        Toast.makeText(MusicActivity.this, "受信できるようになったぽい", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                //失敗
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

    //endregion

    private void handleUnsuccessfulNearbyResult(Status status) {
        if (status.getStatusCode() == NearbyMessagesStatusCodes.APP_NOT_OPTED_IN) {
            if (!mResolvingNearbyPermissionError) {
                try {
                    mResolvingNearbyPermissionError = true;
                    status.startResolutionForResult(MusicActivity.this,
                            Constants.REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (status.getStatusCode() == ConnectionResult.NETWORK_ERROR) {
                Toast.makeText(MusicActivity.this, "なんかネットワークに問題あるっぽいよ",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MusicActivity.this, "なんかよく分からんけど失敗したっぽい: " +
                        status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void music(String action){
        MusicEntity musicEntity = new Gson().fromJson(action, MusicEntity.class);
        if (musicEntity != null) {
            stateTextView.setText(musicEntity.getAction());
            switch (musicEntity.getAction()) {
                case Constants.MUSIC_START:
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                    }
                    break;
                case Constants.MUSIC_STOP:
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        try {
                            mediaPlayer.prepare();
                        } catch (IllegalStateException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

}