package com.example.barcode1d;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rscja.deviceapi.Barcode1D;
import com.barcode.BarcodeUtility;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.rscja.utility.StringUtility;

import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import net.sf.json.JSONObject;


public class MainActivity extends Activity {
	private boolean TIP = true;//提示的标志
	private Handler handler;
	private Thread thread;
	int readerStatus = 0;

	private boolean threadStop = true;
	private boolean isBarcodeOpened = false;//判断条码对象是否开启
	private Barcode1D mInstance;//条码对象
	private ExecutorService executor;

	// 控件
	private Button editNBtn;
	private Button deleteBtn;
	private Button editBtn;
	private Button submit;

	private RadioButton daily_Upload;
	private RadioButton daily_Import;
	private RadioButton mouth_Check;
	private ListView listview;
	private TextView shop_ID;
	// 数组
	private SimpleAdapter listItemAdapter;
	private ArrayList<HashMap<String, String>> listItem = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// 获取控件
		editNBtn = (Button) findViewById(R.id.edit_num);
		deleteBtn = (Button) findViewById(R.id.delete_id);
		editBtn = (Button) findViewById(R.id.edit_id);
		submit = (Button) findViewById(R.id.submit);
//		disp = (Button) findViewById(R.id.display);

		daily_Upload = (RadioButton) findViewById(R.id.daily_upload);
		daily_Import = (RadioButton) findViewById(R.id.daily_import);
		mouth_Check = (RadioButton) findViewById(R.id.mouth_check);
		listview = (ListView) findViewById(R.id.show_result);
		shop_ID = (TextView) findViewById(R.id.shop_id);

		// 初始化数据
		init();

		// 设置控件事件监听
		editNBtn.setOnClickListener(editNClick);
		deleteBtn.setOnClickListener(deleteClick);
		editBtn.setOnClickListener(editClick);
		submit.setOnClickListener(subClick);
//		disp.setOnClickListener(disClick);
	}

	/**
	 * 修改商铺ID按钮事件
	 */
	OnClickListener editClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// 加载输入框的布局文件
			LayoutInflater inflater = (LayoutInflater) MainActivity.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final LinearLayout layout = (LinearLayout) inflater.inflate(
					R.layout.input_add, null);

			// 弹出的对话框
			new AlertDialog.Builder(MainActivity.this)
					/* 弹出窗口的最上头文字 */
					.setTitle("修改一条数据")
					/* 设置弹出窗口的图式 */
					.setIcon(android.R.drawable.ic_dialog_info)
					/* 设置弹出窗口的信息 */
					.setView(layout)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {
									EditText shop_id = (EditText) layout.findViewById(R.id.input_shop_id);
									String id = shop_id.getText().toString();
									shop_ID.setText(id);
									saveID(id);
									Toast.makeText(getApplicationContext(), "设置成功", Toast.LENGTH_SHORT).show();
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() { /* 设置跳出窗口的返回事件 */
								public void onClick(
										DialogInterface dialoginterface, int i) {
									Toast.makeText(MainActivity.this,
											"取消了修改数据", Toast.LENGTH_SHORT).show();
								}
							}).show();
		}
	};

	/**
	 * 修改数量按钮事件
	 */
	OnClickListener editNClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// 加载输入框的布局文件
			LayoutInflater inflater = (LayoutInflater) MainActivity.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final LinearLayout layout = (LinearLayout) inflater.inflate(
					R.layout.input_edit, null);

			// 弹出的对话框
			new AlertDialog.Builder(MainActivity.this)
				/* 弹出窗口的最上头文字 */
				.setTitle("修改一条数据")
				/* 设置弹出窗口的图式 */
				.setIcon(android.R.drawable.ic_dialog_info)
				/* 设置弹出窗口的信息 */
				.setView(layout)
				.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialoginterface, int i) {
								EditText inputEditNum = (EditText) layout.findViewById(R.id.input_edit_num);
								String editNum = inputEditNum.getText().toString();
									int size = listItem.size();
									// 判断数字是否超出数组索引范围
									if (size > 0) {
										HashMap<String,String> hdata = listItem.get(0);
										hdata.put("num",editNum);
										listItem.set(0, hdata);
										listItemAdapter.notifyDataSetChanged();
									} else {
										Toast.makeText(getApplicationContext(), "没有数据可以修改", Toast.LENGTH_SHORT).show();
									}
								}
						})
				.setNegativeButton("取消",
						new DialogInterface.OnClickListener() { /* 设置跳出窗口的返回事件 */
							public void onClick(
									DialogInterface dialoginterface, int i) {
								Toast.makeText(MainActivity.this,
										"取消了修改数据", Toast.LENGTH_SHORT).show();
							}
						}).show();
		}
	};

	/**
	 * 清楚按钮事件
	 */
	OnClickListener deleteClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// 加载输入框的布局文件
			LayoutInflater inflater = (LayoutInflater) MainActivity.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final LinearLayout layout = (LinearLayout) inflater.inflate(
					R.layout.input_delete, null);

			//删除第一条数据（最新一条）
			int size = listItem.size();
			// 判断数字是否超出数组索引范围
			if (size>0) {
				String value = listItem.get(0).toString();
				listItem.remove(0);
				listItemAdapter.notifyDataSetChanged();
				Toast.makeText(MainActivity.this, "删除的数据为:" + value + "", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "列表为空，操作无效", Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * 提交按钮事件
	 */
	OnClickListener subClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(listItem == null || listItem.size() == 0){
				Toast.makeText(MainActivity.this, "提交失败，不能提交空表单", Toast.LENGTH_LONG).show();
			}else {
				boolean dataExa = false;//异常数据标志
				HashMap<String,String> hashMap = new HashMap<String, String>();
				int size = listItem.size();
				for(int i=0;i<size;i++){
					hashMap = listItem.get(i);
					if(shop_ID.getText().toString() == null || shop_ID.getText().toString().equals("")){//判断是否存在商铺ID，若不存在，则不允许提交表单
						Toast.makeText(MainActivity.this, "提交失败，商铺ID没有设置", Toast.LENGTH_LONG).show();
						break;
					}
					if(hashMap.get("name").equals("没有给商品的名字")){//判断数据库是否有商品消息，如果没有，则不允许提交表单
						Toast.makeText(MainActivity.this, "提交失败，存在空数据", Toast.LENGTH_LONG).show();
                        dataExa = false;
						break;
					}else if(hashMap.get("bar_code").equals("1234567890")){//判断是否存在初始数据，如果存在，则不允许提交表单
						Toast.makeText(MainActivity.this, "提交失败，初始数据未删除", Toast.LENGTH_LONG).show();
						dataExa = false;
						break;
					}else {
						dataExa = true;
					}
				}
				if(dataExa){
					report();
				}
			}
		}
	};

	private void init() {
		try {
			mInstance = Barcode1D.getInstance();
		} catch (ConfigurationException e) {
			Toast.makeText(MainActivity.this, R.string.rfid_mgs_error_config,
					Toast.LENGTH_SHORT).show();
			return;
		}
		checkID();//查看本地存储商铺id的文件
		listItem = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("num", "*提示*");
		map.put("bar_code","1234567890");
		map.put("pd_pder","生产商");
		map.put("category","类别");
		map.put("pri_import","进货价");
		map.put("pri_sell","零售价");
		map.put("name", "数量为0的商品不提交到后台\n请输入并确认所在商铺id\n请确认好提交类型后再提交\n" +
				"请把读取失败的数据删除\n修改数量的商品为列表第一条的商品\n" +
				"请把空内容的数据删除\n以免数据分析错误\n扫码时需要有一定间隔时间否则会出错\n" +
                "同一商品扫描一次即可，多次扫描会造成数据重复");
		listItem.add(map);
		listItemAdapter = new SimpleAdapter(getApplicationContext(), listItem,// 数据源
				R.layout.items, new String[] { "num", "name", "bar_code","pd_pder","category","pri_import","pri_sell"}, new int[] {
				R.id.num, R.id.name, R.id.bar_code,R.id.pd_pder,R.id.category,R.id.pri_import,R.id.pri_sell});
		listview.setAdapter(listItemAdapter);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg != null) {
					String strData = "";//条码显示框内容
					switch (msg.arg1) {
					case 0:
						strData = getString(R.string.yid_msg_scan_fail) + "\n";//扫描失败时，显示的内容
						break;
					case 1:
						strData += msg.obj.toString();
						showList(strData);
//						code = strData;
						break;
					default:
						break;
					}
					if(msg.what == 1){
						if(TIP){
							listItem.removeAll(listItem);
							listItemAdapter.notifyDataSetChanged();
							TIP = false;//当有新数据进来时，清除提示
						}
						HashMap hashMap = (HashMap<String, String>) msg.obj;
                        listItem.add(0,hashMap);
                        listItemAdapter.notifyDataSetChanged();
                    }
                    if(msg.what == 2){
//						Gson gson = new Gson();
						Bundle bundle = msg.getData();
						String result = bundle.getString("success");
                        if(result.equals("200.1")){//入库成功返回消息
                            listItem.removeAll(listItem);
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("num", "*提示*");
							map.put("name", "数量为0的商品不提交到后台\n请输入并确认所在商铺id\n请确认好提交类型后再提交\n" +
									"请把读取失败的数据删除\n修改数量的商品为列表第一条的商品\n" +
									"请把空内容的数据删除\n以免数据分析错误\n扫码时需要有一定间隔时间否则会出错\n" +
									"同一商品扫描一次即可，多次扫描会造成数据重复");
                            listItem.add(map);
                            listItemAdapter.notifyDataSetChanged();
                            listview.setAdapter(listItemAdapter);
                            Toast.makeText(MainActivity.this, "入库成功", Toast.LENGTH_LONG).show();
                        }else if(result.equals("200")){//入库失败返回消息
							Toast.makeText(MainActivity.this, "入库失败，请检查上传的数据信息", Toast.LENGTH_LONG).show();
						}else if(result.equals("500")){//入库失败返回信息
							Toast.makeText(MainActivity.this, "数据库中可能没有部分商品信息\n请联系管理员", Toast.LENGTH_LONG).show();
						}
					}
				}
			}
		};
	}

	private void scan() {
		if (threadStop) {
			Log.i("MY", "readerStatus " + readerStatus);
			boolean bContinuous = false;//连续扫描判断
			int iBetween = 0;
//			thread = new Thread(new GetBarcode(bContinuous, iBetween));
//			thread.start();
			executor.execute(new GetBarcode(bContinuous, iBetween));
		} else {
			threadStop = true;
		}
	}

	class GetBarcode implements Runnable {
		private boolean isContinuous = false;
		String barCode = "";
		private long sleepTime = 1000;
		Message msg = null;

		public GetBarcode(boolean isContinuous) {
			this.isContinuous = isContinuous;
		}
		public GetBarcode(boolean isContinuous, int sleep) {
			this.isContinuous = isContinuous;
			this.sleepTime = sleep;
		}

		@Override
		public void run() {
			do {
				barCode = mInstance.scan();
				Log.i("MY", "barCode " + barCode.trim());
				msg = new Message();
				if (StringUtility.isEmpty(barCode)) {
					msg.arg1 = 0;
					msg.obj = "";
				} else {
					msg.arg1 = 1;
					msg.obj = barCode;
				}
				handler.sendMessage(msg);

				SoundManage manager = null;
				manager.PlaySound(MainActivity.this, SoundManage.SoundType.SUCCESS);
			} while (isContinuous && !threadStop);//先执行一遍循环内容，若连续扫描与线程其中一个没开启，则跳出循环
		}

	}

	/**
	 * 设备上电异步类
	 * 
	 * @author liuruifeng 
	 */	
	public class InitTask extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog mypDialog;
		
		@Override
		protected Boolean doInBackground(String... params) {
			return mInstance.open();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			isBarcodeOpened = result;
			mypDialog.cancel();
			if (!result) {
				Toast.makeText(MainActivity.this, "init fail",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mypDialog = new ProgressDialog(MainActivity.this);
			mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mypDialog.setMessage("init...");
			mypDialog.setCanceledOnTouchOutside(false);
			mypDialog.show();
		}

	}
	@Override
	protected void onResume() {
		super.onResume();
		executor = Executors.newFixedThreadPool(6);
		new InitTask().execute();
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		threadStop = true;
		executor.shutdownNow();
		if (isBarcodeOpened) {
			mInstance.close();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == 139) {
			if (event.getRepeatCount() == 0) {
				scan();
				Log.i("MY", "keyCode " + keyCode);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 保存商铺id
	 */
	public void saveID(String id){
		try {
			FileOutputStream fos=openFileOutput("shopID.txt", Context.MODE_PRIVATE);
			OutputStreamWriter osw=new OutputStreamWriter(fos,"UTF-8");
			osw.write(id);
			//保证输出缓冲区中的所有内容
			osw.flush();
			fos.flush();
			//后打开的先关闭，逐层向内关闭
			fos.close();
			osw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询商铺ID
	 */
	public void checkID(){
		ArrayList<HashMap<String,String>> array = new ArrayList<HashMap<String, String>>();
		try {
			FileInputStream fis=openFileInput("shopID.txt");
			InputStreamReader is=new InputStreamReader(fis,"UTF-8");
			char input[]=new char[fis.available()];
			is.read(input);
			is.close();
			fis.close();
			String readed=new String(input);
			shop_ID.setText(readed);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		checkData.clear();
//		return "数据库暂无此消息";
	}

	/**
		查询商品信息
	 */
	public void showList(final String bar_code){
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				BufferedReader reader = null;
				try {
					URL url = new URL("http://39.108.235.163:8099/shop/phone/queryGoods");
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("POST");
					connection.setDoOutput(true);
					connection.setDoInput(true);
					connection.setUseCaches(false);
					connection.connect();

					String body = "bar_code="+bar_code+"&shop_id="+shop_ID.getText().toString();
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
					writer.write(body);
					writer.close();

					int responseCode = connection.getResponseCode();
					if(responseCode == HttpURLConnection.HTTP_OK){
						InputStream inputStream = connection.getInputStream();
						reader = new BufferedReader(new InputStreamReader(inputStream));
						StringBuffer response = new StringBuffer();
						String line;
						while ((line = reader.readLine()) != null){
							response.append(line);
						}
						String jsonString = response.toString();
						Gson gson = new Gson();
						Type mapType = new TypeToken<HashMap<String,HashMap>>(){}.getType();
						HashMap<String, HashMap> map = new HashMap<String, HashMap>();
						map = gson.fromJson(jsonString, mapType);
//						map.put("num","0");
						Message mes = handler.obtainMessage();
						final SerializableMap myMap=new SerializableMap();//传递map数据
						myMap.setMap(map.get("data"));//将map数据添加到封装的myMap中
						Bundle bundle=new Bundle();
						bundle.putSerializable("map", myMap);
						mes.what = 1;
						mes.obj = map.get("data");
						handler.sendMessage(mes);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}finally {
					if (reader != null){
						try {
							reader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (connection != null){
						connection.disconnect();
					}
				}
			}
		}).start();
	}

	/**
		入库操作
		分为三种类型：上货、入库和盘点
	 */
	public void report(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				String result = "";
				BufferedReader reader = null;
				try {
					URL url = new URL("http://39.108.235.163:8099/shop/phone/batchinputInfo");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setDoOutput(true);
					conn.setDoInput(true);
					conn.setUseCaches(false);
					conn.setRequestProperty("Charset", "UTF-8");
					conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");

                    Gson gson = new Gson();
					HashMap<String,String> t;//存放中间数据
					HashMap<String,String> reData = new HashMap<String, String>();
                    HashMap<String,String> lMap = new HashMap<String, String>();//存放中间数据
					Map MAP = new HashMap<String,String>();
                    ArrayList<HashMap<String,String>> arrayList = new ArrayList<HashMap<String, String>>();
					JSONObject json = new JSONObject();

					Message msg = new Message();
					Bundle bundle = new Bundle();
					msg.what = 2;

					int listSize = listItem.size();
					//把扫描的所有数据存为json格式数据
					for(int i=0;i<listSize;i++){
						t = listItem.get(i);
						//判断表单类型
						if(daily_Upload.isChecked()){
							reData.put("type","1");
						}else if(daily_Import.isChecked()){
							reData.put("type","2");
						}else if(mouth_Check.isChecked()){
							reData.put("type","3");
						}
						//判断是否有数量
						if(t.get("num") == null){//没有数量的默认设置为0
							reData.put("num","0");
							continue;
						}else if(t.get("num") == "0"){//数量为0的不给通过
							continue;
						}else{
                            reData.put("num",t.get("num"));
                        }
						reData.put("bar_code",t.get("bar_code"));
						reData.put("shop_id",shop_ID.getText().toString());
						json.put(i,reData);
					}
					//循环取出json数据的value，放到map里，再存进list里
                    Iterator<String> iterator =json.keys();
                    while(iterator.hasNext()){
                        String key = iterator.next();
                        String value = json.getString(key);
                        lMap = gson.fromJson(value, lMap.getClass());
                        arrayList.add(lMap);
                    }
					//取出list，分割出list的value
                    MAP.put("mapList",arrayList);
					String dt =JSONObject.fromObject(MAP).toString();
					dt = dt.substring(11,dt.length()-1);
					// 往服务器里面发送数据
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
                    writer.write(dt);
                    writer.close();

                    //对服务器返回的数据进行处理
					conn.setConnectTimeout(5000);//设置超时
                    int responseCode = conn.getResponseCode();
					if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = conn.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuffer response = new StringBuffer();
                        String line;
                        while ((line = reader.readLine()) != null){
                            response.append(line);
                        }
                        String jsonString = response.toString();
                        Type mapType = new TypeToken<HashMap<String,Boolean>>(){}.getType();
                        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
                        map = gson.fromJson(jsonString, mapType);
                        //将服务器返回的信息回传到handler进行判断
                        if(map.get("success")){
                            bundle.putString("success","200.1");//返回成功信息
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }else{
							bundle.putString("success","200");//返回成功信息
							msg.setData(bundle);
							handler.sendMessage(msg);
						}
					}else if(responseCode == 500){
						bundle.putString("success","500");//返回成功信息
						msg.setData(bundle);
						handler.sendMessage(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}
}