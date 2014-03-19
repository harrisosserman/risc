package models;

import java.util.*;
import models.TroopType;

public class Spy{

	private Player myOwner;
	private TroopType myPreviousType;
	private Integer myCatchPercentage;
	private Territory myLocation;



	public Spy(Player owner, TroopType type){
		myOwner = owner;
		myPreviousType = type;
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
    
    public void setCatchPercentage(Integer percentage){
        myCatchPercentage = percentage;
    }


} 