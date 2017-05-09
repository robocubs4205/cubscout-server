package com.robocubs4205.cubscout.rest;

/**
 * Created by trevor on 3/22/17.
 */
public class JsonArrayContainer<T> {
    public Iterable<T> data;
    public JsonArrayContainer(Iterable<T> data){
        this.data=data;
    }
}
