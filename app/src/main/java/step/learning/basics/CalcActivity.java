package step.learning.basics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class CalcActivity extends AppCompatActivity {

    private TextView tvHistory;
    private TextView tvResult;

    private String firstParam;
    private String secondParam;

    private String prevOp;

    private boolean newParam = true;
    private boolean calculated = true;

    private String minusSing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        tvHistory = findViewById(R.id.tvHistory);
        tvResult = findViewById(R.id.tvResult);
        tvHistory.setText("");
        tvResult.setText("0");
        firstParam = "0";
        secondParam = "0";
        prevOp = "NOP";
        minusSing = getString(R.string.minus_sign);

        for(int i = 0; i < 10; ++i){
            findViewById(
                    getResources()
                            .getIdentifier(
                                    "btn" + i,
                                    "id",
                                    getPackageName()
                            )
            ).setOnClickListener(this::digitClick);
        }

        findViewById(R.id.btnBackspace).setOnClickListener(this::BackspaceClick);
        findViewById(R.id.btnC).setOnClickListener(this::CClick);
        findViewById(R.id.btnCE).setOnClickListener(this::CEClick);
        findViewById(R.id.btnEqual).setOnClickListener(this::equalClick);
        findViewById(R.id.btnPlus).setOnClickListener(this::operationClick);
        findViewById(R.id.btnMinus).setOnClickListener(this::operationClick);
        findViewById(R.id.btnMultiply).setOnClickListener(this::operationClick);
        findViewById(R.id.btnDivide).setOnClickListener(this::operationClick);
        findViewById(R.id.btnComa).setOnClickListener(this::comaClick);
        findViewById(R.id.btnDivideByOne).setOnClickListener(this::invertClick);
        findViewById(R.id.btnSquare).setOnClickListener(this::squareClick);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("history", tvHistory.getText());
        outState.putCharSequence("result", tvResult.getText());
        outState.putCharSequence("firstParam", firstParam);
        outState.putCharSequence("secondParam", secondParam);
        outState.putCharSequence("prevOp", prevOp);
        outState.putCharSequence("newParam", newParam + "");
        outState.putCharSequence("calculated", calculated + "");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvHistory.setText(savedInstanceState.getCharSequence("history"));
        tvResult.setText(savedInstanceState.getCharSequence("result"));
        firstParam = savedInstanceState.getCharSequence("firstParam").toString();
        secondParam = savedInstanceState.getCharSequence("secondParam").toString();
        prevOp = savedInstanceState.getCharSequence("prevOp").toString();
        newParam = Boolean.parseBoolean(savedInstanceState.getCharSequence("newParam").toString());
        calculated = Boolean.parseBoolean(savedInstanceState.getCharSequence("calculated").toString());
    }

    private void digitClick(View v) {

        String result = tvResult.getText().toString();

        if(result.replaceAll("\\.", "").length() >= 10) return;

        String digit = ((Button) v).getText().toString();
        if(newParam == true){
            result = digit;
            newParam = false;
        }
        else{
            result += digit;
        }
        if(calculated){
            firstParam = result;
        }
        else
            secondParam = result;
        tvResult.setText(result);
    }

    private void BackspaceClick(View v) {

        String result = tvResult.getText().toString();
        result = result.substring(0, result.length() - 1);
        if(calculated) firstParam = result;
        else secondParam = result;
        tvResult.setText(result);
    }

    private void CEClick(View v) {
        tvResult.setText("0");
        secondParam = "0";
        newParam = true;
    }

    private void CClick(View v) {
        tvHistory.setText("");
        tvResult.setText("0");
        firstParam = "0";
        secondParam = "0";
        prevOp = "NOP";
        newParam = true;
        calculated = true;
    }

    private void equalClick(View v) {
        if(prevOp == "NOP")
        {
            tvHistory.setText(firstParam + " =");
            return;
        }

        String history = firstParam + " " + prevOp + " " + secondParam + " =";

        tvHistory.setText(history);
        firstParam = Calculate();
        if (firstParam == "NaN") {
            CClick(v);
            return;
        }
        calculated = true;
        tvResult.setText(firstParam);
        newParam = true;
    }

    private void operationClick(View v) {
        if(newParam == false){
            firstParam = Calculate();
            if (firstParam == "NaN") {
                CClick(v);
                return;
            }
            secondParam = "0";
        }
        calculated = false;

        String op = ((Button) v).getText().toString();
        String history = firstParam + " " + op;

        tvHistory.setText(history);
        tvResult.setText(firstParam);
        prevOp = op;
        newParam = true;
    }

    private void invertClick(View v){
        secondParam = firstParam;
        firstParam = "1";
        prevOp = "\u00F7";
        equalClick(v);
    }

    private void squareClick(View v){
        secondParam = firstParam;
        prevOp = "\u00D7";
        equalClick(v);
    }

    private void comaClick(View v) {
        String result = tvResult.getText().toString();
        if(!result.contains("."))
            result += ".";
        tvResult.setText(result);

        if (newParam) newParam = false;
    }

    private double getArgument(String param){
        param = param.replace(minusSing, "-");
        return Double.parseDouble(param);
    }

    private String deleteZeros(StringBuilder param){
        int firstIndex = param.charAt(0) == '-' ? 2 : 1;
        while(param.charAt(param.length() - 1) == '0'){
                param.deleteCharAt(param.length() - 1);
        }
        if(param.charAt(param.length() - 1) == '.') {
            param.deleteCharAt(param.length() - 1);
            return param.toString();
        }

        while(param.charAt(firstIndex) == '0'){
                param.deleteCharAt(firstIndex);
        }

        return param.toString();
    }

    private void alert(int messageId){
        Toast
                .makeText(
                        this,
                        messageId,
                        Toast.LENGTH_SHORT )
                .show() ;
        long[] vibrationPattern = {0, 200, 100, 200};
        Vibrator vibrator;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            VibratorManager vibratorManager = (VibratorManager)
                    getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        }
        else{
            vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        }


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            vibrator.vibrate(
                    VibrationEffect.createWaveform(vibrationPattern, -1)
            );
        }
        else {
            vibrator.vibrate(vibrationPattern, -1);
        }
    }

    private int getDigitsAmount(String num){
        String tmp = num.replaceAll("[^\\d]", "");
        return tmp.length();
    }

    private String Calculate()
    {
        int dec1 = new StringBuilder(firstParam).reverse().toString().indexOf('.');
        int dec2 = new StringBuilder(secondParam).reverse().toString().indexOf('.');
        String paramForFormat = dec1 >= dec2 ? firstParam : secondParam;

        DecimalFormat decimalFormat = null;

        Double res = 0.0;
        if(prevOp.equals("\u002B"))
            res += getArgument(firstParam) + getArgument(secondParam);
        else if(prevOp.equals(minusSing))
            res += getArgument(firstParam) - getArgument(secondParam);
        else if(prevOp.equals("\u00D7"))
            res += getArgument(firstParam) * getArgument(secondParam);
        else if(prevOp.equals("\u00F7")) {
            if(secondParam.equals("0"))
            {
                alert(R.string.calc_divide_by_zero);
                return "NaN";
            }
            res += getArgument(firstParam) / getArgument(secondParam);
        }
        else
            return tvResult.getText().toString();

        Log.d("res", res.doubleValue() + "");

        if(res % 1 == 0){

            String resStr = "" + res.intValue();

            if(("" + Math.abs(res)).length() > 10)
            {
                Toast
                        .makeText(
                                this,
                                "number is over 10 digits",
                                Toast.LENGTH_SHORT )
                        .show() ;
                return firstParam;
            }
            return resStr;
        }
        else {
            if(!paramForFormat.contains(".")) {
                paramForFormat = res.toString();
            }

            paramForFormat = paramForFormat.toCharArray()[0] == '-' || paramForFormat.toCharArray()[0] == '\u2212' ?
                    paramForFormat.substring(1) : paramForFormat;

            String[] strs = paramForFormat.split("\\.");
            strs[0] = strs[0].replaceAll("\\d", "#");
            strs[1] = strs[1].replaceAll("\\d", "0");
            paramForFormat = String.join(".", strs);

            decimalFormat = new DecimalFormat(paramForFormat);

            StringBuilder resStr = new StringBuilder(res + "");

            if (Math.abs(getArgument(resStr.toString())) < 1 && resStr.charAt(0) != '0') {
                resStr = resStr.insert(resStr.indexOf("."), 0);
            }

            if(getDigitsAmount(resStr.toString()) > 10){
                alert(R.string.too_big_number);
                return firstParam;
            }

            return deleteZeros(resStr);
        }
    }
}