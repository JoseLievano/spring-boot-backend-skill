package com.authServer.exceptions;

public class InvalidDeleteOperation extends Exception{

    public InvalidDeleteOperation(String message){
        super(message);
    }

    public InvalidDeleteOperation(){
        super("Invalid delete operation");
    }

}