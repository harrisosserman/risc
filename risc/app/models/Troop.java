package models;

import java.util.*;

public class Troop{

	private int myStrength;

	public Troop(){
		myStrength = 1;
	}

	public Troop(int strength){
		myStrength = strength;
	}

	public int getStrength(){
		return myStrength;
	}
} 