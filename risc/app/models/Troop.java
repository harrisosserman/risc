package models;

import java.util.*;
import models.TroopType;

public class Troop{

	private int myStrength;
	private Player myOwner;
	private TroopType myType;


	public Troop(Player owner, TroopType type){
		myOwner = owner;
		myType = type;
		myStrength = findStrength();
	}

	public int findStrength(){
		switch(myType){
			case INFANTRY: myStrength = 0;
					break;
			case AUTOMATIC: myStrength = 1;
				break;
			case ROCKETS: myStrength = 3;
				break;
			case TANKS: myStrength = 6;
				break;
			case IMPROVEDTANKS: myStrength = 12;
				break;
			case PLANES: myStrength = 15;
				break;
			default: myStrength = 0;
				break;
		}
		return myStrength;
	}

    public Player getOwner(){
        return myOwner;
    }
    
    public void setOwner(Player player){
        myOwner = player;
    }
    
	public int getStrength(){
		return myStrength;
	}

	public TroopType getType(){
		return myType;
	}

	public double battle(){
        double value = (double)(Math.round(Math.random()*19 + 1) + myStrength);
        return value;
	}

} 