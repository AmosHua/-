package huahang.ss.pku.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import huahang.ss.pku.bean.City;
import huahang.ss.pku.db.CityDB;

/**
 * Created by amoshua on 2017/11/1.
 */

public class MyApplication extends Application{
    private static final String TAG = "MyAPP";
    private static MyApplication myApplication;
    private CityDB mCityDB;
    private List<City> mCityList;
    private String[] cityCode;
    private String[] cityName;
    private List<Map<String, Object>> listems;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"MyApplication->Oncreate");
        myApplication=this;
        mCityDB=openCityDB();
        initCityList();
    }
    private void initCityList(){
        mCityList =new ArrayList<City>();
        new Thread(new Runnable(){
            @Override
            public void run() {
                prepareCityList();
            }
        }).start();
    }
    private boolean prepareCityList(){
        mCityList =mCityDB.getAllCity();
        ArrayList<String> cityCodeList = new ArrayList<String>();
        ArrayList<String> cityNameList = new ArrayList<String>();
        listems = new ArrayList<Map<String, Object>>();
        int i=0;
        for(City city:mCityList){
            i++;
            String cityName=city.getCity();
            String cityCode=city.getNumber();
            Log.d(TAG,cityCode+":"+cityName);
            Map<String, Object> listem = new HashMap<String, Object>();
            listem.put("city", city.getCity());
            listem.put("province", city.getProvince());
            listems.add(listem);
            cityNameList.add(city.getCity());
            cityCodeList.add(city.getNumber());
        }
        cityCode = new String[cityCodeList.size()];
        cityCode = cityCodeList.toArray(cityCode);
        cityName = new String[cityNameList.size()];
        cityName = cityNameList.toArray(cityName);
        Log.d(TAG,"i="+i);
        return true;
    }


    public List<City> getmCityList() {
        return mCityList;
    }

    public static MyApplication getInstance(){
        return myApplication;
    }
    private CityDB openCityDB() {
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "databases"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db = new File(path);
        Log.d(TAG, path);
        if (!db.exists()) {
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName() + File.separator + "databases" + File.separator;
            File dirFirstFolder = new File(pathfolder);
            if (!dirFirstFolder.exists()) {
                dirFirstFolder.mkdirs();
                Log.i("MyApp", "mkdirs");
            }
            Log.i("MyApp", "db is not exists");
            try {
                InputStream is = getAssets().open("city.db");
                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    fos.flush();
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path);
    }
    public String[] getCityCodeList(){
        return cityCode;
    }
    public String[] getCityNameList(){
        return cityName;
    }
}