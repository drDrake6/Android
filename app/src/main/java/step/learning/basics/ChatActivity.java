package step.learning.basics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private LinearLayout chatContainer;
    private final String CHAT_URL = "https://diorama-chat.ew.r.appspot.com/story";
    private String content;
    private List<ChatMessage> chatMessages;
    private ChatMessage chatMessage;
    private EditText etAuthor;
    private EditText etMessage;
    private ScrollView svContainer;
    private Handler handler;
    private static final String CHANNEL_ID = "chat_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatContainer = findViewById(R.id.chatContainer);
        etAuthor = findViewById(R.id.etUserName);
        etMessage = findViewById(R.id.etMessage);
        content = "";
        findViewById(R.id.chatButtonSend).setOnClickListener(this::sendButtonClick);
        chatMessages = new ArrayList<>();
        svContainer = findViewById(R.id.sv_container);
        handler = new Handler();
        handler.post(this::updateChat);
    }

    private  void updateChat(){
        new Thread(this::loadUrl).start();
        handler.postDelayed(this::showNotification, 2000);
    }

    private void showNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.sym_def_app_icon)
                        .setContentTitle("Chat")
                        .setContentText("Message from chat")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification notification = builder.build();
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(ChatActivity.this);
        notificationManager.notify(1001, notification);
    }

    private void sendButtonClick(View view){
        if(etAuthor.getText().length() == 0 || etMessage.getText().length() == 0){
            Toast.makeText(this, "Author name or message is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        this.chatMessage = new ChatMessage();
        chatMessage.setAuthor(etAuthor.getText().toString());
        chatMessage.setText(etMessage.getText().toString());
        new Thread(this::postChatMessage).start();
    }

    private  void postChatMessage(){
        try {
            URL url = new URL(CHAT_URL);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "*/*");
            urlConnection.setChunkedStreamingMode(0);

            OutputStream body = urlConnection.getOutputStream();
            body.write(String.format("{\"author\": \"%s\", \"txt\":\"%s\"}",
                    chatMessage.getAuthor(), chatMessage.getText()).getBytes());
            body.flush();
            body.close();

            int responseCode = urlConnection.getResponseCode();
            if(responseCode != 200){
                Log.d("postChatMessage","Response code: " + responseCode);
                return;
            }
            InputStream reader = urlConnection.getInputStream();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int len;
            while((len = reader.read(chunk)) != -1){
                bytes.write(chunk, 0, len);
            }
           Log.d("postChatMessage",
                   new String(bytes.toByteArray(), StandardCharsets.UTF_8));
            bytes.close();
            reader.close();
            urlConnection.disconnect();
        } catch (MalformedURLException ex) {
            Log.d("postChatMessage","MalformedURLException: " + ex.getMessage());
        } catch (IOException ex) {
            Log.d("postChatMessage","IOException: " + ex.getMessage());
        }
        loadUrl();
    }

    private void loadUrl(){
        try(
                InputStream inputStream =
                        new URL(CHAT_URL)
                                .openStream()) {
            StringBuilder sb = new StringBuilder();
            int sym;
            while((sym = inputStream.read()) != -1){
                sb.append((char) sym);
            }
            content = new String(
                    sb.toString().getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8);

            new Thread(this::parseContent).start();
        } catch (MalformedURLException ex) {
            Log.d("loadUrl", "MalformedURLException: " + ex.getMessage());
        } catch (IOException ex) {
            Log.d("loadUrl","IOException: " + ex.getMessage());
        }
    }

    private void parseContent() {
        try {                                                       // server response:
            JSONObject js = new JSONObject( content ) ;             // { "status": "success",
            JSONArray jMessages = js.getJSONArray( "data" ) ;     //   "data": [ {},{},... ]
            if( "success".equals( js.get("status") ) ) {            // }
                for( int i = 0; i < jMessages.length(); ++i ) {
                    ChatMessage tmpMsg = new ChatMessage( jMessages.getJSONObject( i ) ) ;
                    if (!chatMessages.stream().anyMatch( msg -> msg.getId().equals(tmpMsg.getId()) ) ) {
                        chatMessages.add( tmpMsg ) ;
                    }
                }
                chatMessages.sort(Comparator.comparing(ChatMessage::getMoment));
                runOnUiThread( this::showChatMessages ) ;
            }
            else {
                Log.d( "parseContent",
                        "Server responses status: " + js.getString( "status" ) ) ;
            }
        }
        catch( JSONException ex ) {
            Log.d( "parseContent", ex.getMessage() ) ;
        }
    }

    private void showChatMessages() {
        Drawable otherBg = AppCompatResources.getDrawable(
                getApplicationContext(), R.drawable.rates_bg);
        Drawable myBg = AppCompatResources.getDrawable(
                getApplicationContext(), R.drawable.rates_bg_right);
        LinearLayout.LayoutParams otherParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        otherParams.setMargins(7, 5, 7, 5);
        LinearLayout.LayoutParams myParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        myParams.setMargins(7, 5, 7, 5);
        myParams.gravity = Gravity.END;
        boolean needScrollDown = false;
        String author = etAuthor.getText().toString();
        for (ChatMessage chatMessage : this.chatMessages) {
            if(chatMessage.getView() != null) continue;

            TextView tv = new TextView(this);
            String msg = chatMessage.toString();
            tv.setText(msg);
            if (chatMessage.getAuthor().equals(author)) {
                tv.setBackground(myBg);
                tv.setLayoutParams(myParams);
            } else {
                tv.setBackground(otherBg);
                tv.setLayoutParams(otherParams);  // путь к setMargins
            }
            tv.setPadding(7, 5, 7, 5);
            chatContainer.addView( tv ) ;
            chatMessage.setView(tv);
            needScrollDown = true;
        }
        if(needScrollDown){
            svContainer.post(() -> svContainer.fullScroll(View.FOCUS_DOWN));
        }
    }

    static class ChatMessage {
        private View view;
        private UUID id;
        private String author;
        private String text;

        private UUID idReplay;
        private String replayPreview;

        public UUID getIdReplay() {
            return idReplay;
        }

        public void setIdReplay(UUID idReplay) {
            this.idReplay = idReplay;
        }

        public String getReplayPreview() {
            return replayPreview;
        }

        public void setReplayPreview(String replayPreview) {
            this.replayPreview = replayPreview;
        }

        public ChatMessage() {
        }

        public ChatMessage(JSONObject obj) {
            idReplay = null;
            replayPreview = null;
            try {
                setAuthor(obj.getString("author"));
                setId((UUID.fromString(obj.getString("id"))));
                setText(obj.getString("txt"));
                if(obj.has("idReplay")){
                    setIdReplay((UUID.fromString(obj.getString("idReplay"))));
                }
                if(obj.has("replayPreview")){
                    setReplayPreview((obj.getString("replayPreview")));
                }
                setMoment(dateFormat.parse(obj.getString("moment")));
            } catch (JSONException ex) {
                Log.d("Message() JSON", ex.getMessage());
            } catch (ParseException ex) {
                Log.d("Message() Parse", ex.getMessage());
            }

        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Date getMoment() {
            return moment;
        }

        public void setMoment(Date moment) {
            this.moment = moment;
        }

        private Date moment;
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy KK:mm:ss a");


        @Override
        public String toString() {
            return  moment + ": " +
                    author + " - "  +
                    text;
        }
    }
}