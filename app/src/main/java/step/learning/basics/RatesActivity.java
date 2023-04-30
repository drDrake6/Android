package step.learning.basics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RatesActivity extends AppCompatActivity {
    private TextView tvJson;

    private List<Rate> rates;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);

        tvJson = findViewById(R.id.tvJson);
        new Thread(this::loadUrl).start();
    }

    private void loadUrl(){
        try(
        InputStream inputStream =
        new URL("https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json")
                .openStream()) {
            StringBuilder sb = new StringBuilder();
            int sym;
            while((sym = inputStream.read()) != -1){
                sb.append((char) sym);
            }
            content = new String(
                    sb.toString().getBytes(StandardCharsets.ISO_8859_1),
                            StandardCharsets.UTF_8);

            //tvJson.setText(sb.toString());

            new Thread(this::parseContent).start();
        } catch (MalformedURLException ex) {
            Log.d("loadUrl", "MalformedURLException: " + ex.getMessage());
        } catch (IOException ex) {
            Log.d("loadUrl","IOException: " + ex.getMessage());
        }
    }

    private void parseContent(){

        rates = new ArrayList<>();
        JSONArray jRates = null;

        try {
            jRates = new JSONArray(content);
            for(int i = 0; i < jRates.length(); ++i){
                rates.add(new Rate(jRates.getJSONObject(i)));
            }
        } catch (JSONException ex) {
            Log.d("parseContent()", ex.getMessage());
        }

        runOnUiThread(() -> showRates());

    }

    private void showRates(){
        Drawable ratesBg = AppCompatResources.getDrawable(
                getApplicationContext(), R.drawable.rates_bg);
        Drawable ratesBgRight = AppCompatResources.getDrawable(
                getApplicationContext(), R.drawable.rates_bg_right);

        StringBuilder str = new StringBuilder();
        LinearLayout container = findViewById(R.id.ratesContainer);
        LinearLayout.LayoutParams layoutParamsLeft = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsLeft.setMargins(7,5,7,5);

        LinearLayout.LayoutParams layoutParamsRight = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsRight.setMargins(7,5,7,5);
        layoutParamsRight.gravity = Gravity.RIGHT;
        try {
            str.append(rates.get(0).getExchangeDate() + "\n");

            for(int i = 0; i < rates.size(); ++i){
                TextView tv = new TextView(this);
                tv.setText(rates.get(i).toString());
                tv.setBackground(i % 2 == 0 ? ratesBg : ratesBgRight);
                tv.setPadding(7,5,7,5);
                tv.setLayoutParams(i % 2 == 0 ? layoutParamsLeft : layoutParamsRight);
                container.addView(tv);
                //str.append(i + ") " + rates.get(i).toString() + "\n");
            }
        } catch (Exception ex) {
            Log.d("parseContent()", ex.getMessage());
        }
    }

    static class Rate {
        private int r030;
        private String txt;
        private double rate;
        private String cc;

        public Rate(JSONObject obj) {
            try {
                setR030(obj.getInt("r030"));
                setTxt(obj.getString("txt"));
                setRate(obj.getDouble("rate"));
                setCc(obj.getString("cc"));
                setExchangeDate(obj.getString("exchangedate"));
            } catch (Exception ex) {
                Log.d("Rate()", ex.getMessage());
            }
        }

        public int getR030() {
            return r030;
        }

        public void setR030(int r030) {
            this.r030 = r030;
        }

        public String getTxt() {
            return txt;
        }

        public void setTxt(String txt) {
            this.txt = txt;
        }

        public double getRate() {
            return rate;
        }

        public void setRate(double rate) {
            this.rate = rate;
        }

        public String getCc() {
            return cc;
        }

        public void setCc(String cc) {
            this.cc = cc;
        }

        public String getExchangeDate() {
            return exchangeDate;
        }

        public void setExchangeDate(String exchangeDate) {
            this.exchangeDate = exchangeDate;
        }

        private String exchangeDate;

        @Override
        public String toString() {
            return  "r030=" + r030 + "\n" +
                    "txt='" + txt + "'\n" +
                    "rate=" + rate + "\n" +
                    "cc='" + cc;
        }
    }


}