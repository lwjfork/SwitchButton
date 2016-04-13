package com.lwj.fork;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.lwj.fork.view.SwitchButton;

public class MainActivity extends Activity {

    SwitchButton sb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sb = (SwitchButton) findViewById(R.id.sb);
        sb.setOnCheckedChangedListener(new SwitchButton.OnCheckedChangedListener() {
            @Override
            public void onCheckedChange(boolean isCheck) {
                Toast.makeText(MainActivity.this,""+isCheck,Toast.LENGTH_LONG).show();
            }
        });
    }
}
