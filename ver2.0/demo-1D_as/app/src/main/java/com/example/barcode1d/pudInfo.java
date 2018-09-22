package com.example.barcode1d;

public class pudInfo {
    int goodsid;
    String barcode;
    String fname;
    int unitprice;
    public String getBarcode() {
        return barcode;
    }
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    public String getFname() {
        return fname;
    }
    public void setFname(String fname) {
        this.fname = fname;
    }
    public int getId() {
        return goodsid;
    }
    public void setId(int goodsid) {
        this.goodsid = goodsid;
    }
    public int getPrice() {
        return unitprice;
    }
    public void setPrice(int unitprice) {
        this.unitprice = unitprice;
    }
}
