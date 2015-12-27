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
            default:
                break;
        }
    }
}
