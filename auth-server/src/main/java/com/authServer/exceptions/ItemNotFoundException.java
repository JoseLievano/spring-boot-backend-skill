package com.authServer.exceptions;

public class ItemNotFoundException extends Exception{

    public ItemNotFoundException(String message){
        super (message);
    }

    public ItemNotFoundException(){
        super("Element not found");
    }
}