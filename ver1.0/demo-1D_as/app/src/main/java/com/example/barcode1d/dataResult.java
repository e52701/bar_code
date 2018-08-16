package com.example.barcode1d;

import android.content.Context;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class dataResult {
    private String text;
    private String bar_code;//存放扫描传过来的条码
    private String pd_name;

    private String fileName = "json.txt";
    HashMap<String, String> hdata = new HashMap<String, String>();

    public dataResult(String bar_code){
        this.bar_code = bar_code;
    }
    public String avalible(Context context, String filePath){
        try {
            //返回assets包
            InputStream is = context.getAssets().open(filePath+".txt");
            int size = is.available();
            // 将内容存储到本地byte缓存
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            // 将byte转换成字符串
            text = new String(buffer, "gbk");

            //将数据转换成json格式
            String jdata = text;
            JSONArray array = JSONArray.fromObject(jdata);
            System.out.println(jdata);

            //判断数据是否存在
            int jsize = array.size(); //获取JSON数组大小
            for(int i=0;i<jsize;i++){
                JSONObject object = array.getJSONObject(i);
                if(object.getString("bar_code").equals(bar_code)){
                    hdata.put("pd_name",object.getString("pd_name"));
                    hdata.put("bar_code",object.getString("bar_code"));
                    hdata.put("pd_pder",object.getString("pd_pder"));
                    hdata.put("category",object.getString("category"));
                    hdata.put("pri_import",object.getString("pri_import"));
                    hdata.put("pri_sell",object.getString("pri_sell"));
                    return "数据存在";
                }else if(i == jsize-1){
                    return "数据库暂无此消息";
                }else{
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "未执行检查操作";
    }

    public HashMap<String, String> getData(){
        return hdata;
    }
}
