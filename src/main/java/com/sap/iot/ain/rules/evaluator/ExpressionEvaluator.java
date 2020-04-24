/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.iot.ain.rules.evaluator;

public class ExpressionEvaluator<T extends Comparable<T>> {

    private T t;

    public T get() {
        return this.t;
    }

    public void set(T t) {
        this.t = t;
    }

    public <T extends Comparable<T>> int compare(T t1, T t2) {
        return t1.compareTo(t2);
    }

}
