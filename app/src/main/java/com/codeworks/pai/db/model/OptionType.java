package com.codeworks.pai.db.model;

/**
 * Created by glennverner on 12/2/14.
 */
public enum OptionType {
    P("Put"),C("Call");
    String value;
    OptionType(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
