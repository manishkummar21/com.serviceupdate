package locationtracking.com.rapidobike;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import locationtracking.com.rapidobike.model.Points;
import locationtracking.com.rapidobike.service.ITrackingService;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class LocationUpdateService extends Service {

    private NotificationManager mNotificationManager;
    private final static String FOREGROUND_CHANNEL_ID = "foreground_channel_id";
    private ITrackingService service;
    private NotificationCompat.Builder mBuilder = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        service = provideClient();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, getNotification(""));
        }

        RequestLocationUpdate();

        return START_STICKY;
    }


    private void RequestLocationUpdate() {

        Observable.interval(15, TimeUnit.SECONDS)
                .flatMap(new Function<Long, ObservableSource<Response<Points>>>() {
                    @Override
                    public ObservableSource<Response<Points>> apply(Long aLong) throws Exception {
                        return service.getPoints();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Response<Points>>() {
                    @Override
                    public void onNext(Response<Points> response) {
                        if (response.isSuccessful()) {

                            boolean isSent = LocalBroadcastManager.getInstance(LocationUpdateService.this)
                                    .sendBroadcast(new Intent("com.locationupdate")
                                            .putExtra("lat", response.body().getLatitude())
                                            .putExtra("lng", response.body().getLongitude()));

                            if (!isSent || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                UpdateLocation(response.body().getLatitude(), response.body().getLongitude());
                            else
                                RemoveNotification();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("error");
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("complete");
                    }
                });
    }

    private void RemoveNotification() {

        //removing the notification when app is in foreground and less than oreo device
        mNotificationManager.cancel(1);
        mBuilder = null;
    }

    private Notification getNotification(String text) {

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(LocationUpdateService.this, MainActivity.class), 0);


        //Build your notification
        mBuilder = new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Location Updating...")
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        // Create Channel for Android O. DON'T FORGET
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID,
                    "Location",
                    NotificationManager.IMPORTANCE_LOW);

            mNotificationManager.createNotificationChannel(channel);
        }
        return mBuilder.build();
    }

    private void UpdateLocation(double lat, double lng) {
        String text = "Last Known Location is " + lat + " " + lng;

        if (mBuilder == null)
            getNotification(text);
        mBuilder.setContentText(text);
        mNotificationManager.notify(1, mBuilder.build());
    }


    private ITrackingService provideClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(okHttpClient)
                .build().create(ITrackingService.class);

    }
}
