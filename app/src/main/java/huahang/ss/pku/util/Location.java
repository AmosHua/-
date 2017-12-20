package huahang.ss.pku.util;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static android.content.ContentValues.TAG;

/**
 * Created by amoshua on 12/12/2017.
 */

public class Location {
    private static final String ADDRESS = "http://api.map.baidu.com/location/ip?ak=gd4Ggp5ssVZtKLpFyA8kFACDnYIC9EXH";
    public static String getLocation() throws IOException {
        URL url = new URL(ADDRESS);
        URLConnection connection = null;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = (HttpURLConnection) connection;
        urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
        urlConnection.connect();
        String responseStr = "";
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
        return ascii2native(responseStr);
    }

    private static String ascii2native ( String asciicode )
    {
        String[] asciis = asciicode.split ("\\\\u");
        String nativeValue = asciis[0];
        try
        {
            for ( int i = 1; i < asciis.length; i++ )
            {
                String code = asciis[i];
                nativeValue += (char) Integer.parseInt (code.substring (0, 4), 16);
                if (code.length () > 4)
                {
                    nativeValue += code.substring (4, code.length ());
                }
            }
        }
        catch (NumberFormatException e)
        {
            return asciicode;
        }
        return nativeValue;
    }

    public static String getCity() throws IOException {
        String result = getLocation();
        JSONObject resultJson = JSON.parseObject(result);
        String city = resultJson.getJSONObject("content")
                .getJSONObject("address_detail")
                .getString("city");
        Log.d("myWeather", "Location result"+city);
        if (city.isEmpty()) {
            city = "no result";
        } else {
            city = city.replace("市", "");
        }
        return city;
    }
}
