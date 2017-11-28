package huahang.ss.pku.weather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ActionBarOverlayLayout;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import huahang.ss.pku.app.MyApplication;
import huahang.ss.pku.bean.City;
import huahang.ss.pku.*;
/**
 * Created by amoshua on 2017/10/25.
 */

public class SelectCity extends Activity implements View.OnClickListener {
    private List<City> mCityList;
    private List<City> filterDataList;
    private ImageView mBackBtn;
    private ListView mList;
    ArrayAdapter<String> adapter;
    private ClearEditText mClwarEditText;
    int size=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
        initViews();
//
        mClwarEditText=(ClearEditText)findViewById(R.id.search_selectCity);
        mClwarEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                filterData(s.toString());
                mList.setAdapter(adapter);

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);


    }
    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.title_back:
//                Intent i=new Intent();
//                i.putExtra("cityCode","101160101");
//                setResult(RESULT_OK,i);
                finish();
                break;
            default:
                break;
        }
    }
    private void initViews(){
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        TextView cityName=findViewById(R.id.title_name);
        cityName.setText(sharedPreferences.getString("city_name","北京"));

        mList=(ListView)findViewById(R.id.title_list);
        MyApplication myApplication=(MyApplication)getApplication();
        mCityList=myApplication.getmCityList();
        size=mCityList.size();
        String[] data=new String[size];
        for(int i=0;i<mCityList.size();i++){
            City item=mCityList.get(i);
            data[i]=item.getAllFristPY()+"  "+item.getCity()+"  ("+item.getProvince()+")";
        }
        adapter=new ArrayAdapter<String>(
                SelectCity.this,android.R.layout.simple_list_item_1,data);
        mList.setAdapter(adapter);
        mList.setTextFilterEnabled(true);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city=mCityList.get(position);
                SharedPreferences.Editor editor = getSharedPreferences("config",MODE_PRIVATE).edit();
                editor.putString("main_city_code",city.getNumber());
                editor.commit();
                Intent i =new Intent();
                i.putExtra("cityCode",city.getNumber());
                setResult(RESULT_OK,i);
                finish();
            }
        });
    }
    //
    private void filterData(String filterStr){
        filterDataList=new ArrayList<City>();
        if(TextUtils.isEmpty(filterStr)){
            for(City city:mCityList){
                filterDataList.add(city);
            }
        }else{
            filterDataList.clear();
            for(City city:mCityList){
                if(city.getAllPY().indexOf(filterStr.toString())!=-1
                        ||city.getCity().indexOf(filterStr.toString())!=-1
                        ||city.getAllFristPY().indexOf(filterStr.toString())!=-1){
                    filterDataList.add(city);
                }
            }
        }
        size=filterDataList.size();
        String[] data=new String[size];
        for(int i=0;i<filterDataList.size();i++){
            City item=filterDataList.get(i);
            data[i]=item.getAllFristPY()+"  "+item.getCity()+"  ("+item.getProvince()+")";
        }
        adapter=new ArrayAdapter<String>(
                SelectCity.this,android.R.layout.simple_list_item_1,data);
    }
}
