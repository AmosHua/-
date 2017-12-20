package huahang.ss.pku.weather;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by amoshua on 12/12/2017.
 */

public class UpdateWeatherService extends Service{
    private static final String TAG = "MiniWeather";
    private Timer timer = new Timer();
    private int  update_interval = 30*60*1000;//默认30分钟更新天气
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: UpdateWeatherService started .");
       // int temp = intent.getExtras().getInt("update_interval");

        int temp = 0;
        if(temp>0){
            update_interval = temp;
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Intent i = new Intent("time_to_update_weather");
                sendBroadcast(i);
            }
        }, 0, update_interval);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        Log.d(TAG, "onDestroy: UpdateWeatherService destroyed .");
    }
}
