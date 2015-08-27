package com.weather.ku.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.weather.ku.R;
import com.weather.ku.model.City;
import com.weather.ku.model.CoolWeatherDB;
import com.weather.ku.model.County;
import com.weather.ku.model.Province;
import com.weather.ku.util.HttpCallbackListener;
import com.weather.ku.util.HttpUtil;
import com.weather.ku.util.Utility;
//遍历省市县数据的活动
public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList=new ArrayList<String>();
	
	
	//省列表,市列表，县列表
	
	private List<Province> provinceList;
	
	private List<City> cityList;
	
	private List<County> countyList;
	
	
	
	//选中的省份，城市
	private Province selectedProvince;
	private City selectedCity;
	
	
	//当前选中的级别
	private int currentLevel;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView=(ListView)findViewById(R.id.list_view);
		titleText=(TextView)findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		coolWeatherDB=CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long arg3) {
				if(currentLevel==LEVEL_PROVINCE)
				{
					selectedProvince=provinceList.get(index);
					queryCities();
				}else if(currentLevel==LEVEL_CITY)
				{
					selectedCity=cityList.get(index);
					queryCounties();
				}
			
				
			}			
		});
		queryProvinces();
	
	}
	
	//查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
	
	private void queryProvinces()
	{
		provinceList=coolWeatherDB.loadProvinces();
		if(provinceList.size()>0)
		{
			dataList.clear();
			for(Province p:provinceList)
			{
				dataList.add(p.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINCE;
		}
		else{
			queryFromServer(null,"province");
		}
	}
	
	//查询选中省内所有的市，优先从数据库中查询，如果没有再去服务器上查询
	
	private void queryCities()
	{
		cityList=coolWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size()>0)
		{
			dataList.clear();
			for(City c:cityList)
			{
				dataList.add(c.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_CITY;
		}
		else {
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	
	private void queryCounties()
	{
		countyList=coolWeatherDB.loadCounties(selectedCity.getId());
		if(countyList.size()>0)
		{
			dataList.clear();
			for(County county:countyList)
			{
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel=LEVEL_COUNTY;
			
		}
		else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	
	
	private void queryFromServer(final String code,final String type)
	{
		String address;
		if(!TextUtils.isEmpty(code))
		{
			address="view-source:http://www.weather.com.cn/data/list3/city"+code+".xml";	//中国天气API接口已变
		}
		else{
			address="view-source:http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address,new  HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result=false;
				if("province".equals(type))
				{
					result=Utility.handleProvincesResponse(coolWeatherDB, response);
				}else if("city".equals(type))
				{
					result=Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if("county".equals(type))
				{
					result=Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				
				if(result)
				{
					runOnUiThread(new Runnable() {
						
						
						public void run() {
						closeProgressDialog();
						if("province".equals(type))
						{
							queryProvinces();
						}else if("city".equals(type))
						{
							queryCities();
						}else if("county".equals(type))
						{
							queryCounties();
						}
							
						}
					});
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				//通过runOnUiTread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
						
					}
				});
				
			}
		});
		
		
	}
	
	private void showProgressDialog()
	{
		if(progressDialog==null)
		{
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加載···");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	private void closeProgressDialog()
	{
		if(progressDialog!=null)
		{
			progressDialog.dismiss();
		}
	}

	//重寫onBackPressed()方法覆蓋默認Back行為
	public void onBackPressed()
	{
		if(currentLevel==LEVEL_COUNTY)
		{
			queryCities();
		}else if(currentLevel==LEVEL_CITY)
		{
			queryProvinces();
		}else {
			finish();
		}
}

}
