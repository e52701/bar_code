package com.example.barcode1d;

import java.io.Serializable;
import java.util.HashMap;

class SerializableMap implements Serializable {
    private HashMap<String,String> map;

    public HashMap<String,String> getMap() {
        return map;
    }

    public void setMap(HashMap<String,String> map) {
        this.map = map;
    }
}
