package gupuru.nearbytest;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

public class GetBeaconActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    private boolean mResolvingNearbyPermissionError = false;
    private MessageListener messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_beacon);

        googleApiClient = new GoogleApiClient.Builder(GetBeaconActivity.this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        messageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                final String nearbyMessageString = new String(message.getContent());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GetBeaconActivity.this, "こんなのが送られてきたっぽい: " + nearbyMessageString, Toast.LENGTH_LONG).show();
                    }
                });
            }

            public void onLost(final Message message) {
                final String nearbyMessageString = new String(message.getContent());
                // メッセージを受信した時の処理
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GetBeaconActivity.this, "失敗しったぽい:" + nearbyMessageString, Toast.LENGTH_LONG).show();
                    }
                });
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        if (null != googleApiClient && googleApiClient.isConnected()) {
            unsubscribe();
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
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
                subscribe();
            } else {
                Toast.makeText(this, "失敗したっぽい " + resultCode,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * メッセージの受信
     */
    private void subscribe() {
        if (!googleApiClient.isConnected()) {
            if (!googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            SubscribeOptions options = new SubscribeOptions.Builder()
                    .setStrategy(Strategy.BLE_ONLY)
                    .setCallback(new SubscribeCallback() {
                        @Override
                        public void onExpired() {
                            super.onExpired();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(GetBeaconActivity.this, "終了したっぽい", Toast.LENGTH_LONG).show();
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
                                        Toast.makeText(GetBeaconActivity.this, "Subscribed成功したっぽい", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
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
                    status.startResolutionForResult(GetBeaconActivity.this,
                            Constants.REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (status.getStatusCode() == ConnectionResult.NETWORK_ERROR) {
                Toast.makeText(GetBeaconActivity.this,
                        "なんかネットワークに問題あるよ",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(GetBeaconActivity.this, "Unsuccessful: " +
                        status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }

}

