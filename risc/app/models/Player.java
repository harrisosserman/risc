package models;

import java.util.*;

public class Player {

	private String myName;
	private int myFood;
	private int myTechnology;
	private int myTechnologyLevel;
	private int turnCommitted;
    private int additionalTroops;
	private Long myTimeStamp;
	private ArrayList<Player> allies;

	public Player(String name){
		this.myName = name;
		allies = new ArrayList<Player>();
	}

	public String getName(){
		return myName;
	}

	public void addAlly(Player p){
		for(Player k : allies){
			if(k.equals(p)){ 
				return;}
		}
		allies.add(p);
	}

	public void removeAlly(Player p){
		int j = 0;
		for(Player k : allies){
			if(k.equals(p)){ allies.remove(j);
				return;}
			j++;
		}
	}

	public ArrayList<Player> getAllies(){
		return allies;
	}

    public void setAdditionalTroops(int numTroops){
        additionalTroops = numTroops;
    }
    
    public int getAdditionalTroops(){
        return additionalTroops;
    }
    
	public void setName(String name){
		myName = name;
	}

	public void setTurnCommitted(int committed){
		turnCommitted = committed;
	}

	public int getTurnCommitted(){
		return turnCommitted;
	}

	public void setTimeStamp(Long timeStamp){
		myTimeStamp = timeStamp;
	}

	public Long getTimeStamp(){
		return myTimeStamp;
	}

	public int getFood(){
		return myFood;
	}

	public int getTechnology(){
		return myTechnology;
	}

	public void setTechnology(int tech){
		myTechnology = tech;
	}

	public void setFood(int food){
		myFood = food;
	}

	public int getTechnologyLevel(){
		return myTechnologyLevel;
	}

	public void setTechnologyLevel(int tech){
		myTechnologyLevel = tech;
	}

}