package com.sap.iot.ain.rules.models;

/**
 * Created by i810756 on 7/19/17.
 */
public class EntityNotFoundException extends Exception {

    @Override
    public String getMessage() {
        return "Entity with this alias or name does not exists ";
    }
}
