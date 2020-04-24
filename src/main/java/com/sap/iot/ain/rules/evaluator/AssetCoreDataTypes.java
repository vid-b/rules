/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.iot.ain.rules.evaluator;

/**
 *
 * @author setup
 */
public enum AssetCoreDataTypes {
    Boolean("boolean"),
    String("string"),
    Date("date"),
    Numeric("numeric"),
    Enum("enum"),
    Picture("picture");
    
    private String dataType;

    private AssetCoreDataTypes(String dataType) {
        this.dataType = dataType;
    }
    
    public static AssetCoreDataTypes forValue(String dataType){
        return AssetCoreDataTypes.valueOf(dataType);
    }
    
    public String toValue(){
        return this.dataType;
    }
    
   
}
