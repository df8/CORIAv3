package com.bigbasti.coria.model;

import com.google.common.base.Strings;

public class DataSetMerge {

    private String first;
    private String second;
    private String name;
    private boolean extend;

    public DataSetMerge() {
    }

    public DataSetMerge(String first, String second, String name, boolean extend) {
        this.first = first;
        this.second = second;
        this.name = name;
        this.extend = extend;
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

    public boolean isExtend() {
        return extend;
    }

    public void setExtend(boolean extend) {
        this.extend = extend;
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
                ", extend=" + extend +
                '}';
    }
}
