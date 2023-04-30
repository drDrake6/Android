package step.learning.basics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addButton = findViewById(R.id.exitButton);
        addButton.setOnClickListener(this::exitButtonClick);

        findViewById(R.id.calcButton)
                .setOnClickListener(this::calcButtonClick);
        findViewById(R.id._2048Button)
                .setOnClickListener(this::game2048ButtonClick);
        findViewById(R.id.ratesButton)
                .setOnClickListener(this::ratesButtonClick);
        findViewById(R.id.chatButton)
                .setOnClickListener(this::chatButtonClick);
    }

    private void game2048ButtonClick(View v){
        Intent intent = new Intent(MainActivity.this, Game2048Activity.class);
        startActivity(intent);
    }

    private void ratesButtonClick(View v){
        Intent intent = new Intent(MainActivity.this, RatesActivity.class);
        startActivity(intent);
    }

    private void calcButtonClick(View v){
        Intent intent = new Intent(MainActivity.this, CalcActivity.class);
        startActivity(intent);
    }

    private void chatButtonClick(View v){
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        startActivity(intent);
    }

    private void exitButtonClick(View v){
        finish();
    }
}