package com.bigbasti.coria.model;

import com.google.common.base.Strings;

public class DataSetMerge {

    private String first;
    private String second;
    private String name;

    public DataSetMerge() {
    }

    public DataSetMerge(String first, String second, String name) {
        this.first = first;
        this.second = second;
        this.name = name;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean idValid(){
        boolean valid = true;

        if(Strings.isNullOrEmpty(first)){
            valid = false;
        }
        if(Strings.isNullOrEmpty(second)){
            valid = false;
        }
        if(Strings.isNullOrEmpty(name)){
            valid = false;
        }

        return valid;
    }

    @Override
    public String toString() {
        return "DataSetMerge{" +
                "first='" + first + '\'' +
                ", second='" + second + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
