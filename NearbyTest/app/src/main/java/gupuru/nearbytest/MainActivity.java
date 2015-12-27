package gupuru.nearbytest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button messageBtn = (Button) findViewById(R.id.message);
        messageBtn.setOnClickListener(this);
        Button passingBtn = (Button) findViewById(R.id.passing);
        passingBtn.setOnClickListener(this);
        Button beaconBtn = (Button) findViewById(R.id.beacon);
        beaconBtn.setOnClickListener(this);
        Button musicBtn = (Button) findViewById(R.id.music);
        musicBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.message:
                intent = new Intent(MainActivity.this, MessageActivity.class);
                startActivity(intent);
                break;
            case R.id.passing:
                intent = new Intent(MainActivity.this, PassingActivity.class);
                startActivity(intent);
                break;
            case R.id.beacon:
                intent = new Intent(MainActivity.this, GetBeaconActivity.class);
                startActivity(intent);
                break;
            case R.id.music:
                intent = new Intent(MainActivity.this, MusicActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

}
