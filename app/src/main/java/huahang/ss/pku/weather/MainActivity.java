package huahang.ss.pku.weather;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import huahang.ss.pku.app.MyApplication;
import huahang.ss.pku.bean.*;
import huahang.ss.pku.util.*;
import huahang.ss.pku.weather.*;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener{

    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final int GET_LOCATION = 2;
    private String currentCityCode;
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private ViewPagerAdapter weatherWeeklyAdapter;
    private ViewPager weatherWeeklyViewPager;
    private List<View> weatherWeeklyViews;
    private ImageView[] dots;
    private int[] ids = {R.id.iv1, R.id.iv2};
    private ProgressBar updateProgress;
    private String currentCityName;
    private final int WEEK = 6;
    private MyApplication app;
    private TextView[] weeksTv, temperatureWeeksTv, climateWeeksTv, windWeeksTv;
    private ImageView[] weeksImg;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_Tv, fengxiangTv;
    private ImageView weatherImg, pmImg;
    private ImageView mLocationBtn;
    private ImageView mShareBtn;
    TextView week_day;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateWeather( msg.obj);
                    break;
                case GET_LOCATION:
                    queryWeather();
                    break;
                default:
                    break;
            }
        }
    };
    private IntentFilter filter = new IntentFilter("time_to_update_weather");
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: 自动更新天气");
            queryWeather();
        }
    };
    private void checkCurrentCity(){
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        currentCityCode = sharedPreferences.getString("cityCode",null);
        currentCityName = sharedPreferences.getString("cityName", "北京");
        city_name_Tv.setText(currentCityName+"天气");
        cityTv.setText(currentCityName);
        if(currentCityCode==null){
            //定位
            getCurrentLocation();
        }
        queryWeather();
        startAutoUpdateWeatherService();
    }

    public static String getXml(String address) throws Exception{
        URL url = new URL(address);
        URLConnection connection = url.openConnection();
        HttpURLConnection urlConnection = (HttpURLConnection) connection;
        urlConnection.connect();
        String responseStr = null;
        if (200 == urlConnection.getResponseCode()) {
            InputStream responseStream = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
            StringBuilder response = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                response.append(str);
            }
            responseStr = response.toString();
        }
        return responseStr;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);
        initWeatherWeeklyView();
        mUpdateBtn = (ImageView) findViewById( R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);
        mLocationBtn = (ImageView) findViewById(R.id.title_location);
        mLocationBtn.setOnClickListener(this);
        app = (MyApplication) getApplicationContext();
        Log.d("MyApp","MainActivity->onCreate");
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            Toast.makeText(MainActivity.this, "网络OK！", Toast.LENGTH_LONG).show();
        } else {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
        }
        initView();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_city_manager) {
            Intent i = new Intent(this, SelectCity.class);
            i.putExtra("currentCity", currentCityName);
            i.putExtra("currentCode", currentCityCode);
            //startActivity(i);
            startActivityForResult(i, 1);
        }
        if (view.getId() == R.id.title_update_btn) {
            view.setVisibility(View.INVISIBLE);
            ProgressBar mUpadeprogress = (ProgressBar) findViewById(R.id.title_update_btn_progress);
            mUpadeprogress.setVisibility(mUpadeprogress.VISIBLE);
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            Log.d("myWeather", cityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(cityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
        if (view.getId() == R.id.title_location) {
            getCurrentLocation();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            currentCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather", "选择的城市代码为" + currentCityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(currentCityCode);
                startAutoUpdateWeatherService();
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }
    /**
     * @param cityCode
     */
    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                ArrayList<WeekDayWeather> weekDayWeathers = null;
                Map<String,Object> result = new HashMap();
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);
                    todayWeather = parseXML(responseStr);
                    weekDayWeathers = parseXmlByPullWeeks(responseStr);
                    if (todayWeather != null|| weekDayWeathers != null) {
                        result.put( "todayWeather", todayWeather);
                        Log.d("myWeather", todayWeather.toString());
                        result.put("weekDayWeathers", weekDayWeathers);
                        Log.d("myWeather",weekDayWeathers.toString());
                    }
                    Message msg = new Message();
                    msg.what = UPDATE_TODAY_WEATHER;
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    /**
     * 解析函数
     */
    private TodayWeather parseXML(String xmldate) {
        TodayWeather todayWeather = null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldate));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                                Log.d("myWeather", "city:" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                                Log.d("myWeather", "updatetime:" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                                Log.d("myWeather", "shidu:    " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                                Log.d("myWeather", "wendu:    " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                                Log.d("myWeather", "pm25:    " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                                Log.d("myWeather", "quality:    " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                Log.d("myWeather", "fengxiang:    " + xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                Log.d("myWeather", "fengli:    " + xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                Log.d("myWeather", "date:    " + xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                Log.d("myWeather", "high:    " + xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                Log.d("myWeather", "low:    " + xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                Log.d("myWeather", "type:    " + xmlPullParser.getText());
                                typeCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
    }
    public static ArrayList<WeekDayWeather> parseXmlByPullWeeks(String xmldata){
        List<WeekDayWeather> weekDayWeathers = new ArrayList<>();
        WeekDayWeather weekDayWeather = null;
        try{
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int evetType = xmlPullParser.getEventType();
            while (evetType != xmlPullParser.END_DOCUMENT) {
                switch (evetType) {
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("weather")) {
                            weekDayWeather = new WeekDayWeather();
                        }
                        if (weekDayWeather != null) {
                            if (xmlPullParser.getName().equals("date")) {
                                evetType = xmlPullParser.next();
                                weekDayWeather.setWeek(xmlPullParser.getText());
                                Log.d("myWeatherhhhhh", "type:    " + xmlPullParser.next());
                            } else if (xmlPullParser.getName().equals("high")) {
                                evetType = xmlPullParser.next();
                                weekDayWeather.setHigh(xmlPullParser.getText().substring(3));
                            } else if (xmlPullParser.getName().equals("low")) {
                                evetType = xmlPullParser.next();
                                weekDayWeather.setLow(xmlPullParser.getText().substring(3));
                            } else if (xmlPullParser.getName().equals("type")) {
                                evetType = xmlPullParser.next();
                                weekDayWeather.setType(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengli")) {
                                evetType = xmlPullParser.next();
                                String fengli = xmlPullParser.getText();
                                if (fengli.equals("微风级")) {
                                    fengli = "微风";
                                }
                                weekDayWeather.setWind(fengli);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (xmlPullParser.getName().equals("weather")&&weekDayWeather!=null){
                            Log.d("test", "parseXmlByPullWeeks: " + weekDayWeather);
                            weekDayWeathers.add(weekDayWeather);
                        }
                        break;
                    default:
                        break;
                }
                evetType = xmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (ArrayList<WeekDayWeather>) weekDayWeathers;
    }

    void initView() {

        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        fengxiangTv = (TextView) findViewById(R.id.fengxiang);
        updateProgress = (ProgressBar)findViewById( R.id.title_update_btn_progress );

        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
        fengxiangTv.setText("N/A");
        SharedPreferences settings = (SharedPreferences) getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("city_name", "北京");
        editor.putString("main_city_code", "101010100");
        editor.commit();
    }
    private void updateWeather(Object p){
        HashMap<String,Object> result = (HashMap<String,Object>)p;
        updateTodayWeather((TodayWeather) result.get("todayWeather"));
        ArrayList<WeekDayWeather> weekDayWeathers = (ArrayList<WeekDayWeather>) result.get("weekDayWeathers");
        updateWeatherWeeklyView(weekDayWeathers);
        final Bitmap shot = myShot(this);
        updateProgress.setVisibility(View.INVISIBLE);
        mUpdateBtn.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    saveToSD(shot,"mnt/sdcard/pictures/","shot.png");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void initWeatherWeeklyView(){
        LayoutInflater inflater = LayoutInflater.from(this);
        weatherWeeklyViews = new ArrayList<View>();

        weatherWeeklyViews.add(inflater.inflate(R.layout.weather_info_page_1,null));
        weatherWeeklyViews.add(inflater.inflate(R.layout.weather_info_page_2,null));
        weatherWeeklyAdapter = new ViewPagerAdapter(weatherWeeklyViews, this);
        weatherWeeklyViewPager = (ViewPager) findViewById(R.id.weather_this_week);
        weatherWeeklyViewPager.setAdapter(weatherWeeklyAdapter);
        weatherWeeklyViewPager.setOnPageChangeListener(this);
        weeksTv = new TextView[WEEK];
        temperatureWeeksTv = new TextView[WEEK];
        climateWeeksTv = new TextView[WEEK];
        windWeeksTv = new TextView[WEEK];
        weeksImg = new ImageView[WEEK];
        Resources res = getResources();
        int idxView = 0;
        for(int i=0;i<WEEK;++i) {
            if(i==3){
                idxView = 1;
            }
            int id = res.getIdentifier("day_" + i, "id", getPackageName());
            LinearLayout dayInfo = (LinearLayout) weatherWeeklyViews.get(idxView).findViewById(id);
            weeksTv[i] = (TextView) dayInfo.findViewById(R.id.week_day);
            temperatureWeeksTv[i] = (TextView) dayInfo.findViewById(R.id.week_temperature);
            climateWeeksTv[i] = (TextView) dayInfo.findViewById(R.id.week_climate);
            windWeeksTv[i] = (TextView) dayInfo.findViewById(R.id.week_wind);
            weeksImg[i] = (ImageView) dayInfo.findViewById(R.id.week_img);
        }
    }
    private void updateWeatherWeeklyView(ArrayList<WeekDayWeather> weekDayWeathers){
        Log.d(TAG, "updateWeatherWeeklyView: "+weekDayWeathers);
        for (int i = 0; i < Math.min(6, weekDayWeathers.size()); ++i) {
            Log.d(TAG, "updateWeatherWeeklyView: "+weeksTv[i].getText());
            weeksTv[i].setText(weekDayWeathers.get(i).getWeek());
            temperatureWeeksTv[i].setText(weekDayWeathers.get(i).getHigh() + "~" + weekDayWeathers.get(i).getLow());
            climateWeeksTv[i].setText(weekDayWeathers.get(i).getType());
            windWeeksTv[i].setText(weekDayWeathers.get(i).getWind());
            int typeImgId = R.drawable.biz_plugin_weather_qing;
            String str =weekDayWeathers.get(i).getType();
            setweathertypeima( str,weeksImg[i]);
            Log.d(TAG, "updateWeatherWeeklyView: "+weeksTv[i].getText());
        }
    }
    void updateTodayWeather(TodayWeather todayWeather) {

        city_name_Tv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度：" + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        SharedPreferences settings = (SharedPreferences) getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        String ss = (String) city_name_Tv.getText();
        editor.putString("city_name", ss);
        editor.commit();
        try {

            if (todayWeather.getPm25()!=null){
                int pmDate = Integer.parseInt( todayWeather.getPm25() );
                if (pmDate >= 0 && pmDate <= 50) {
                    pmImg.setImageResource( R.drawable.biz_plugin_weather_0_50 );
                } else if (pmDate > 50 && pmDate <= 100) {
                    pmImg.setImageResource( R.drawable.biz_plugin_weather_51_100 );
                } else if (pmDate > 100 && pmDate <= 150) {
                    pmImg.setImageResource( R.drawable.biz_plugin_weather_101_150 );
                } else if (pmDate > 150 && pmDate <= 200) {
                    pmImg.setImageResource( R.drawable.biz_plugin_weather_151_200 );
                } else if (pmDate > 200 && pmDate <= 300) {
                    pmImg.setImageResource( R.drawable.biz_plugin_weather_201_300 );
                } else if (pmDate > 300) {
                    pmImg.setImageResource( R.drawable.biz_plugin_weather_greater_300 );
                }
        }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        String str = todayWeather.getType();
        setweathertypeima( str,weatherImg );

        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:" + todayWeather.getFengli());
        fengxiangTv.setText(todayWeather.getFengxiang());
        ProgressBar mUpadeprogress = (ProgressBar) findViewById(R.id.title_update_btn_progress);
        mUpadeprogress.setVisibility(mUpadeprogress.INVISIBLE);
        View view1 = (View) findViewById(R.id.title_update_btn);

    }
    private void setweathertypeima(String str ,ImageView weatherImg){
        switch (str) {
            case "暴雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "暴雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "大雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case "大雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "多云":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "雷阵雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "晴":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
            case "沙尘暴":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "特大暴雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "雾":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "小雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "小雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "阴":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
                break;
            case "雨夹雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "阵雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "中雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "中雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            default:
                break;

        }
    }
    private void queryWeather(){
        Log.d("myApp", "选择的城市代码为"+currentCityCode);
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            queryWeatherCode(currentCityCode);
        } else {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
        }
    }

    private void startAutoUpdateWeatherService(){
        Intent i = new Intent(getBaseContext(), UpdateWeatherService.class);
        i.putExtra("update_interval",0);
        startService(i);
        filter.addAction("time_to_update_weather");
        registerReceiver(receiver,filter);
    }
    private void getCurrentLocation() {
        Toast.makeText(MainActivity.this,"定位中...",Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String city = Location.getCity();
                    if (city.equals("no result")) {
                        Log.d(TAG, "run: 定位失败，未得到结果");
                    }else{
                        String[] nameList = app.getCityNameList();
                        Log.d("myWeather", "nameList"+nameList[0]);
                        String[] codeList = app.getCityCodeList();
                        int idx = 0;
                        for (;idx<nameList.length;++idx) {
                            if(nameList[idx].equals(city)) break;
                        }
                        currentCityName = nameList[idx];
                        currentCityCode = codeList[idx];
                        Log.d(TAG, "run: "+currentCityName+" "+currentCityCode);
                        Message msg = new Message();
                        msg.what = GET_LOCATION;
                        mHandler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void saveToSD(Bitmap bmp, String dirName,String fileName) throws IOException {
        // 判断sd卡是否存在
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File dir = new File(dirName);
            // 判断文件夹是否存在，不存在则创建
            if(!dir.exists()){
                dir.mkdir();
            }

            File file = new File(dirName + fileName);
            // 判断文件是否存在，不存在则创建
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                if (fos != null) {
                    // 第一参数是图片格式，第二个是图片质量，第三个是输出流
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    // 用完关闭
                    fos.flush();
                    fos.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private Bitmap myShot(Activity activity) {
        // 获取windows中最顶层的view
        View view = activity.getWindow().getDecorView();
        view.buildDrawingCache();
        // 获取状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeights = rect.top;
        Display display = activity.getWindowManager().getDefaultDisplay();
        // 获取屏幕宽和高
        int widths = display.getWidth();
        int heights = display.getHeight();
        // 允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);
        // 去掉状态栏
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0,
                statusBarHeights, widths, heights - statusBarHeights);
        // 销毁缓存信息
        view.destroyDrawingCache();
        return bmp;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
    private void changeUpdateInterval(int interval){
        Intent i = new Intent(getBaseContext(), UpdateWeatherService.class);
        stopService(i);
        if (interval > 0) {
            i.putExtra("update_interval",interval);
            startService(i);
            filter.addAction("time_to_update_weather");
            registerReceiver(receiver,filter);
        }
    }
    @Override
    public void onPageSelected(int position) {
        for (int a=0;a<ids.length;++a) {
            if (a == position) {
                dots[a].setImageResource(R.drawable.page_indicator_focused);
            } else {
                dots[a].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}