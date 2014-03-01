package models;

import java.util.*;

public class Player {

	private String myName;
	private int myFood;
	private int myTechnology;
	private int myTechnologyLevel;
	private int turnCommitted;
	private Long myTimeStamp;

	public Player(String name){
		this.myName = name;
	}

	public String getName(){
		return myName;
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