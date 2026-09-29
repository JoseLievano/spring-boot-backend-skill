package com.agentForgeBackend.exceptions;

public class ItemAlreadyExist extends Exception{

    public ItemAlreadyExist(String message){
        super(message);
    }

    public ItemAlreadyExist(){
        super("This item already exist");
    }

}