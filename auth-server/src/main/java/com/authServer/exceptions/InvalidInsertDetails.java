package com.authServer.exceptions;

public class InvalidInsertDetails extends Exception{

    public InvalidInsertDetails(String message){
        super (message);
    }

    public InvalidInsertDetails(){
        super("Invalid insert details");
    }

}