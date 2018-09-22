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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import android.content.SharedPreferences;
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
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;


public class MainActivity extends Activity {
	private boolean TIP = true;//提示的标志
	private static boolean VIEWLIST = false;//查看列表标志
	private Handler handler;
	private Thread thread;
	int readerStatus = 0;
	String ch_Type = "4";//表单类型
	boolean isSet = false;//是否设置了商铺id的标志
	int id;//接收从后台传来的商铺id

	private boolean threadStop = true;
	private boolean isBarcodeOpened = false;//判断条码对象是否开启
	private Barcode1D mInstance;//条码对象
	private ExecutorService executor;

	// 控件
	private Button deleteBtn;
	private Button submit;
	private Button editBtn;
	private Button modify;
	private Button viewList;


	private RadioGroup list_Type;
	private RadioButton change_message;
	private RadioButton out_stock;
	private RadioButton new_Sub;
	private ListView listview;
	private TextView shop_ID;
	private TextView pd_Name;

	// 数组
	private SimpleAdapter listItemAdapter;
	private ArrayList<HashMap<String, String>> listItem = null;
	public static ArrayList<HashMap<String,String>> reViewList = new ArrayList<HashMap<String, String>>();//接收查看列表的list

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// 获取控件
		deleteBtn = (Button) findViewById(R.id.delete_id);
		submit = (Button) findViewById(R.id.submit);
		modify = (Button) findViewById(R.id.modify_data);
		editBtn = (Button) findViewById(R.id.edit_id);
		viewList = (Button) findViewById(R.id.viewList);

		list_Type = (RadioGroup) findViewById(R.id.list_type);
		change_message = (RadioButton) findViewById(R.id.change_message);
		out_stock = (RadioButton) findViewById(R.id.out_stock);
		new_Sub = (RadioButton) findViewById(R.id.new_sub);
		listview = (ListView) findViewById(R.id.show_result);
		shop_ID = (TextView) findViewById(R.id.shop_id);
		pd_Name = (TextView) findViewById(R.id.name);

		// 初始化数据
		init();

		// 设置控件事件监听
		deleteBtn.setOnClickListener(deleteClick);
		submit.setOnClickListener(subClick);
		modify.setOnClickListener(moClick);
		editBtn.setOnClickListener(editClick);
		viewList.setOnClickListener(viewClick);

		/*
		设置单选按钮监听事件
	 	*/
		list_Type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				//如果选择“新建清单”选项，修改商品信息按钮开启，反之关闭
				if(checkedId == change_message.getId()){//修改商品信息
					ch_Type = "6";
				}else if(checkedId == out_stock.getId()){//商品下架
					ch_Type = "5";
				}else if(checkedId == new_Sub.getId()){//新建清单
					ch_Type = "4";
				}
			}
		});

//		getShopId(shop_ID.getText().toString());//从后台获取商铺id
	}

	/**
	 * 拦截返回按钮
	 * @param event
	 * @return
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
			return true;
		}else {
			return super.dispatchKeyEvent(event);
		}
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
					.setTitle("修改商铺ID")
					/* 设置弹出窗口的图式 */
					.setIcon(android.R.drawable.ic_dialog_info)
					/* 设置弹出窗口的信息 */
					.setView(layout)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialoginterface, int i) {
									EditText shop_id = (EditText) layout.findViewById(R.id.input_shop_id);
									String id = shop_id.getText().toString();
									shop_ID.setText(id);
									saveID(id);
									Toast.makeText(getApplicationContext(), "修改成功", Toast.LENGTH_SHORT).show();
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
	 * 设置查看列表监听
	 */
	OnClickListener viewClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			TIP = true;
			VIEWLIST = !VIEWLIST;//双数次点击清空列表
			showList("110011101");
		}
	};

	/**
	 * 	设置修改商品信息监听
	 */
	OnClickListener moClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 加载输入框的布局文件
			LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.modify, null);

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
									EditText modify_num = (EditText) layout.findViewById(R.id.modify_num);
									EditText modify_name = (EditText) layout.findViewById(R.id.modify_name);
									String num = modify_num.getText().toString();
									String name = modify_name.getText().toString();
									int size = listItem.size();
									// 判断数字是否超出数组索引范围
									if (size > 0) {
										HashMap<String,String> hdata = listItem.get(0);
										hdata.put("num",num);
										if(name.equals("") || name == null){
											HashMap<String,String> map = new HashMap<String, String>();
											map = listItem.get(0);
											hdata.put("name",map.get("name"));
										}else{
											hdata.put("name",name);
										}
										listItem.set(0, hdata);
										listItemAdapter.notifyDataSetChanged();
										Toast.makeText(getApplicationContext(), "修改成功", Toast.LENGTH_SHORT).show();
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
	 * 清除按钮事件
	 */
	OnClickListener deleteClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// 加载输入框的布局文件
			LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.input_delete, null);

			//删除第一条数据（最新一条）
			int size = listItem.size();
			// 判断数字是否超出数组索引范围
			if (size>0) {
				listItem.remove(0);
				listItemAdapter.notifyDataSetChanged();
				Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
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
					if(hashMap.get("name").equals("没有给商品的信息！")){//判断数据库是否有商品消息，如果没有，则不允许提交表单
						Toast.makeText(MainActivity.this, "提交失败，存在空数据", Toast.LENGTH_LONG).show();
                        dataExa = false;
						break;
					}else if(hashMap.get("bar_code").equals("1234567890")){//判断是否存在初始数据，如果存在，则不允许提交表单
						Toast.makeText(MainActivity.this, "提交失败，初始数据未删除", Toast.LENGTH_LONG).show();
						dataExa = false;
						break;
					}else{
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
		map.put("name", "数量为0的商品不提交到后台\n默认商品名的数据不提交到后台\n请确认好提交类型后再提交\n" +
				"请把读取失败的数据删除\n修改数量的商品为列表第一条的商品\n" +
				"以免数据分析错误\n扫码时需要有一定间隔时间否则会出错\n" +
                "同一商品扫描一次即可，多次扫描会造成数据重复");
		listItem.add(map);
		listItemAdapter = new SimpleAdapter(getApplicationContext(), listItem,// 数据源
				R.layout.items, new String[] { "num", "name", "bar_code"}, new int[] {
				R.id.num, R.id.name, R.id.bar_code});
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
						break;
					default:
						break;
					}
					Bundle bundle = msg.getData();
					String result = bundle.getString("success");
					switch(msg.what){
						case 1:
							if(TIP){
								listItem.removeAll(listItem);
								listItemAdapter.notifyDataSetChanged();
								TIP = false;//当有新数据进来时，清除提示
							}
							if(msg.obj.equals("renew")){
								if(VIEWLIST){
									listItem.removeAll(listItem);
									listItem.addAll(reViewList);
									listItemAdapter.notifyDataSetChanged();
									setBtRa(false);

								}else{
									listItem.removeAll(listItem);
									listItemAdapter.notifyDataSetChanged();
									setBtRa(true);
								}
							}else{
								HashMap hashMap = (HashMap<String, String>) msg.obj;
								listItem.add(0,hashMap);
								listItemAdapter.notifyDataSetChanged();
								if(listItem.size()>1){
									change_message.setEnabled(false);
								}else{
									change_message.setEnabled(true);
								}
							}
							break;
						case 2:
							if(result.equals("200.1")){//入库成功返回消息
								listItem.removeAll(listItem);
								HashMap<String, String> map = new HashMap<String, String>();
								map.put("num", "*提示*");
								map.put("bar_code","1234567890");
								map.put("name", "数量为0的商品不提交到后台\n默认商品名的数据不提交到后台\n请确认好提交类型后再提交\n" +
										"请把读取失败的数据删除\n修改数量的商品为列表第一条的商品\n" +
										"以免数据分析错误\n扫码时需要有一定间隔时间否则会出错\n" +
										"同一商品扫描一次即可，多次扫描会造成数据重复");
								listItem.add(map);
								listItemAdapter.notifyDataSetChanged();
								listview.setAdapter(listItemAdapter);
								TIP = true;//提交成功后，提示信息标识设置为有效
								Toast.makeText(MainActivity.this, "入库成功", Toast.LENGTH_LONG).show();
							}else if(result.equals("200")){//入库失败返回消息
								Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
							}else if(result.equals("500")){//入库失败返回信息
								Toast.makeText(MainActivity.this, "服务器无响应\n请联系管理员", Toast.LENGTH_LONG).show();
							}
							break;
						case 400://服务器连接错误大类
							Toast.makeText(MainActivity.this, "服务器连接失败", Toast.LENGTH_LONG).show();//请求信息服务器出错响应
							break;
						case 404:
							Toast.makeText(MainActivity.this, "连接服务器失败", Toast.LENGTH_LONG).show();//请求信息服务器出错响应
							break;
						case 501://服务器响应错误大类
							Toast.makeText(MainActivity.this, "服务器出错", Toast.LENGTH_LONG).show();//请求信息服务器出错响应
							break;
						case 666://自处理
							Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();//请求信息服务器出错响应
							break;
						default:
							break;
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
			} while (isContinuous && !threadStop);//先执行一遍循环内容，若连续扫描与线程其中一个没开启，则跳出循环
			SoundManage manager = null;
			manager.PlaySound(MainActivity.this, SoundManage.SoundType.SUCCESS);
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
			if(shop_ID.getText() != "" || shop_ID.getText() != null){
				getShopId(shop_ID.getText().toString());//从后台获取商铺id
			}
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
			if(shop_ID.getText() != "" || shop_ID.getText() != null){
				getShopId(shop_ID.getText().toString());//从后台获取商铺id
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
		查询商品信息
	 */
	public void showList(final String bar_code){
	    if(ch_Type.equals("6") && listItem.size() == 1 && !TIP){//修改数据只允许修改一条，并且第一条数据不是默认数据
            Toast.makeText(MainActivity.this, "修改数据只能单条修改！", Toast.LENGTH_LONG).show();
	        return;
        }
		if(shop_ID.getText().toString().equals("") || shop_ID.getText().toString() == null){
			Toast.makeText(MainActivity.this, "扫描失败，商铺ID未设置", Toast.LENGTH_LONG).show();
		}else{
			new Thread(new Runnable() {
				@Override
				public void run() {
					HttpURLConnection connection = null;
					BufferedReader reader = null;
					try {
						if(bar_code.equals("110011101")){
							URL url = new URL("http://39.108.235.163:8088/shop/goodsshop/goodslist?shopId="+id);
							connection = (HttpURLConnection) url.openConnection();
							connection.setRequestMethod("GET");
							connection.setConnectTimeout(8000);
							connection.setReadTimeout(8000);
							connection.connect();
						}else{
							URL url = new URL("http://39.108.235.163:8088/shop/phone?shop_id="+id+"&bar_code="+bar_code);
							connection = (HttpURLConnection) url.openConnection();
							connection.setRequestMethod("GET");
							connection.setConnectTimeout(8000);
							connection.setReadTimeout(8000);
							connection.connect();
						}
						Message msg = new Message();
						Bundle bundle = new Bundle();
						int responseCode = connection.getResponseCode();
						switch (responseCode){
							case 200:
								InputStream inputStream = connection.getInputStream();
								reader = new BufferedReader(new InputStreamReader(inputStream));
								StringBuffer response = new StringBuffer();
								String line;
								while ((line = reader.readLine()) != null){
									response.append(line);
								}
								String jsonString =response.toString();
								JSONObject jsonArray = JSONObject.fromObject(jsonString);
								HashMap<String, Object> map;
								String js = jsonArray.get("data").toString();

								if(js.equals("\"不存在商品信息！\"")){
									msg.what = 666;
									msg.obj = js;
									handler.sendMessage(msg);
									break;
								}else{
									map = parseJSON2Map(js);//将json字符串转成map
									msg.what = 1;

									if(map.size()<1){
										msg.obj = "renew";
									}else{
										msg.obj = map;
									}
									handler.sendMessage(msg);
									break;
								}
							case 404:
								msg = new Message();
								msg.what = 404;
								handler.sendMessage(msg);
								break;
							case 500:
								msg = new Message();
								msg.what = 501;
								handler.sendMessage(msg);
								break;
							default:
								break;
						}
					} catch (Exception e) {
						Message msg = new Message();
						msg.what = 400;
						msg.obj = e;
						handler.sendMessage(msg);
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
					URL url = new URL("http://39.108.235.163:8088/shop/phone");
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
						reData.put("type",ch_Type);
						//判断是否有数量
						if(t.get("num") == null){//没有数量的默认设置为0
							reData.put("num","0");
							continue;
						}else if(t.get("num") == "0"){//数量为0的直接抛弃
							continue;
						}else{
                            reData.put("num",t.get("num"));
                        }
                        //判断是否是新建商品信息
						if(t.get("name").equals("没有给商品的信息！")){//没有修改名字的商品直接抛弃
							continue;
						}else if(ch_Type.equals("4") || ch_Type.equals("6")){
							reData.put("name",t.get("name"));
						}
						reData.put("bar_code",t.get("bar_code"));
						reData.put("shop_id",id+"");
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
                    switch (responseCode){
						case 200:
							InputStream inputStream = conn.getInputStream();
							reader = new BufferedReader(new InputStreamReader(inputStream));
							StringBuffer response = new StringBuffer();
							String line;
							while ((line = reader.readLine()) != null){
								response.append(line);
							}
							String jsonString = response.toString();
							JSONObject jsonArray = JSONObject.fromObject(jsonString);
							HashMap<String, Object> map;
							String success = jsonArray.get("success").toString();
							//将服务器返回的信息回传到handler进行判断
							if(success.equals("true")){
								bundle.putString("success","200.1");//返回成功信息
								msg.setData(bundle);
								handler.sendMessage(msg);
							}else{
								msg.obj = jsonArray.get("errMsg");
								msg.what = 666;
								handler.sendMessage(msg);
							}
							break;
						case 500:
							bundle.putString("success","500");//返回成功信息
							msg.setData(bundle);
							handler.sendMessage(msg);
							break;
						default:
							break;
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

	/**
	 * 将json数据转成map
	 * @param jsonStr
	 * @return
	 */
	public static HashMap<String, Object> parseJSON2Map(String jsonStr){
		HashMap<String, Object> map = new HashMap<String, Object>();
		HashMap<String, String> hashMap;
		ArrayList<HashMap<String,String>> arrayList = new ArrayList<HashMap<String,String>>();
		//判断是否有多条数据
		if(jsonStr.substring(0,1).equals("[")){
			List<pudInfo> list=jsonToList(jsonStr, pudInfo.class);
			Iterator it = list.iterator();
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					pudInfo value = list.get(i);
					hashMap = new HashMap<String, String>();
					hashMap.put("name", value.getFname());
					hashMap.put("bar_code",value.getBarcode());
					arrayList.add(0,hashMap);
				}
			}
			reViewList = arrayList;
			return map;
		}else{
			JSONObject json = JSONObject.fromObject(jsonStr);
			for(Object k : json.keySet()){
				Object v = json.get(k);
				if(v instanceof JSONArray){
					List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
					Iterator<JSONObject> it = ((JSONArray)v).iterator();
					while(it.hasNext()){
						JSONObject json2 = it.next();
						list.add(parseJSON2Map(json2.toString()));
					}
					map.put(k.toString(), list);
				} else {
					if(k.toString().equals("success")){
						continue;
					}
					map.put(k.toString(), v);
				}
			}
		}
		return map;
	}

	/**
	 * 获取商铺信息
	 */
	public void getShopId(String shopNum){
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				BufferedReader reader = null;
				try {
					URL url = new URL("http://39.108.235.163:8088/shop/shopinfo?shopNum="+shop_ID.getText().toString());
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					connection.connect();

					Message msg;
					Bundle bundle = new Bundle();
					int responseCode = connection.getResponseCode();
					switch (responseCode){
						case 200:
							InputStream inputStream = connection.getInputStream();
							reader = new BufferedReader(new InputStreamReader(inputStream));
							StringBuffer response = new StringBuffer();
							String line;
							while ((line = reader.readLine()) != null){
								response.append(line);
							}
							String jsonString =response.toString();
							JSONObject jsonArray = JSONObject.fromObject(jsonString);
							HashMap<String, Object> map;
							String js = com.alibaba.fastjson.JSONObject.toJSONString(jsonArray.get("data"));

							if(js.length()>50){
								map = parseJSON2Map(js);//将json字符串转成map
								String result = map.get("shopId").toString();
								id =Integer.parseInt(result);
								break;
							}else{
								String result = (String)jsonArray.get("data");
								if(result.equals("没有该商铺信息！")){
									msg = new Message();
									msg.what = 666;
									msg.obj = js;
									handler.sendMessage(msg);
									break;
								}
							}
							break;
						case 404:
							msg = new Message();
							msg.what = 404;
							handler.sendMessage(msg);
							break;
						case 500:
							msg = new Message();
							msg.what = 501;
							handler.sendMessage(msg);
							break;
						default:
							break;
					}
				} catch (Exception e) {
					Message msg = new Message();
					msg.what = 400;
					msg.obj = e;
					handler.sendMessage(msg);
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

	// 将jsonArray字符串转换成List集合
	public static List jsonToList(String json, Class beanClass) {
		if (!StringUtils.isBlank(json)) {//这里的JSONObject引入的是 com.alibaba.fastjson.JSONObject;
			return com.alibaba.fastjson.JSONObject.parseArray(json, beanClass);
		} else {
			return null;
		}
	}

	/**
	 * 设置按钮与单选是否可用
	 */
	public void setBtRa(boolean type){
		change_message.setEnabled(type);
		out_stock.setEnabled(type);
		new_Sub.setEnabled(type);
		submit.setEnabled(type);
		deleteBtn.setEnabled(type);
		editBtn.setEnabled(type);
		modify.setEnabled(type);
	}
}