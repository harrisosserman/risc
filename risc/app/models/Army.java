package models;

import java.util.*;
import models.TroopType;
import models.Player;

public class Army{

	private ArrayList<Troop> myTroops;
	private Player myOwner;

	public Army(Player owner){
		myOwner = owner;
		myTroops = new ArrayList<Troop>();
	}

	public void addTroop(Troop t){
		myTroops.add(t);
		return;
	}

	public int getSize(){
		return myTroops.size();
	}

	public ArrayList<Troop> getTroops(){
		return myTroops;
	}

	public void setTroops(ArrayList<Troop> troops){
		myTroops = troops;
	}
	
	public void deleteTroop(TroopType type){
		for(int i=0; i<myTroops.size(); i++){
			if(myTroops.get(i).getType().equals(type)){
				myTroops.remove(i);
			}
		}
	}

	public Troop getStrongest(){
		if(myTroops.size() == 0 ){
			System.out.println("the army is empty");
			return null;
		}
		int strength = 0;
        Troop strongest = myTroops.get(0); 
		for(int i=0; i<myTroops.size(); i++)
		{   
			Troop sample = myTroops.get(i);
			int sample_strength = sample.getStrength();
			if(sample_strength>=strength);
			{
				strength = sample_strength;
				strongest = sample;
			}
		}
			return strongest;
	}
	
	public Troop getWeakest(){
		if(myTroops.size() == 0 ){
			System.out.println("the army is empty");
			return null;
		}

		int strength = 0;
        Troop weakest = myTroops.get(0); 
		for(int i=0; i<myTroops.size(); i++)
		{
			Troop sample = myTroops.get(0);
			int sample_strength = sample.getStrength();
			if(sample_strength>=strength);
			{
				strength = sample_strength;
				weakest = sample;
			}
		}
			return weakest;
	}
}

