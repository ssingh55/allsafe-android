package infosecadventures.allsafe.challenges;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import infosecadventures.allsafe.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NoteReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        // Validate the intent action
        if (intent.getAction() != null && !intent.getAction().equals("infosecadventures.allsafe.action.PROCESS_NOTE")) {
            Log.w("NoteReceiver", "Received unexpected action: " + intent.getAction());
            return;
        }

        // Validate and sanitize incoming extras
        String server = intent.getStringExtra("server");
        String note = intent.getStringExtra("note");
        String notification_message = intent.getStringExtra("notification_message");

        if (server != null) {
            server = server.trim();
        }
        if (note != null) {
            note = note.trim();
        }
        if (notification_message != null) {
            notification_message = notification_message.trim();
        }

        // Basic validation: ensure critical parameters are not null or empty
        if (server == null || server.isEmpty() || note == null || note.isEmpty()) {
            Log.e("NoteReceiver", "Missing required 'server' or 'note' extra.");
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("http")
                .host(server)
                .addPathSegment("api")
                .addPathSegment("v1")
                .addPathSegment("note")
                .addPathSegment("add")
                .addQueryParameter("auth_token", "YWxsc2FmZV9kZXZfYWRtaW5fdG9rZW4=")
                .addQueryParameter("note", note)
                .build();

        Request request = new Request.Builder()
                .url(httpUrl)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // initially you'll probably get "Unable to resolve host" error message
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            }
        });

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ALLSAFE");
        builder.setContentTitle("Notification from Allsafe");
        builder.setContentText(notification_message != null ? notification_message : "Default message");
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setAutoCancel(true);
        builder.setChannelId("ALLSAFE");
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel("ALLSAFE", "ALLSAFE_NOTIFICATION", NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(1, notification);
    }
}
