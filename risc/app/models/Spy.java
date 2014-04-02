package models;

import java.util.*;
import models.TroopType;

public class Spy{

	private Player myOwner;
	private TroopType myPreviousType;
	private Integer myCatchPercentage;
	private Territory myLocation;
    private boolean hasMovedThisTurn;



	public Spy(Player owner, TroopType type){
		myOwner = owner;
		myPreviousType = type;
	    hasMovedThisTurn = false;
    }

    public boolean canMove(){
        if(hasMovedThisTurn==false){
            hasMovedThisTurn=true;
            return hasMovedThisTurn;
        }
        else{
            return false;
        }
    }

    public Player getOwner(){
        return myOwner;
    }
    
    public void setOwner(Player player){
        myOwner = player;
    }

    public int findStrength(){
    	return 2;
    }

	public TroopType getType(){
		return myPreviousType;
	}

	public void setType(TroopType type){
        myPreviousType = type;
    }

    public Territory getLocation(){
        return myLocation;
    }

    public void setLocation(Territory location){
        myLocation = location;
    }

    public Integer getCatchPercentage(){
        return myCatchPercentage;
    }
    
    public int getPosition(){
        return myLocation.getPosition();
    }

    public void setCatchPercentage(Integer percentage){
        myCatchPercentage = percentage;
    }

    public boolean spyCaughtOrNot(){
        myCatchPercentage = myCatchPercentage + 7;
        double value =  (Math.random() * 100);
        if(myCatchPercentage>=value){
            return true;
        }
        else{
            return false;
        }
    }


} 