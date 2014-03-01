package models;

import java.util.*;
import models.TroopType;
import models.Player;

public class Army{

	private HashMap<TroopType, ArrayList<Troop>> myTroops;
	private Player myOwner;

	public Army(Player owner){
		myOwner = owner;
		myTroops = new HashMap<TroopType, ArrayList<Troop>>();
		myTroops.put(TroopType.INFANTRY, new ArrayList<Troop>());
		myTroops.put(TroopType.AUTOMATIC, new ArrayList<Troop>());
		myTroops.put(TroopType.ROCKETS, new ArrayList<Troop>());
		myTroops.put(TroopType.TANKS, new ArrayList<Troop>());
		myTroops.put(TroopType.IMPROVEDTANKS, new ArrayList<Troop>());
		myTroops.put(TroopType.PLANES, new ArrayList<Troop>());
	}

	public void addTroop(TroopType type, Troop t){
		ArrayList<Troop> troops = myTroops.get(type);
		troops.add(t);
		myTroops.put(type, troops);
	}

	public int getSize(){
		return myTroops.size();
	}

	public HashMap<TroopType, ArrayList<Troop>> getTroops(){
		return myTroops;
	}

	public void setTroops(HashMap<TroopType, ArrayList<Troop>> troops){
		myTroops = troops;
	}
	
	public void deleteTroop(TroopType type){
		ArrayList troops = myTroops.get(type);
		troops.remove(0);
		myTroops.put(type, troops);
	}

	public Troop getStrongest(){
		if(myTroops.get(TroopType.PLANES).size()>0){
			Troop strongest = myTroops.get(TroopType.PLANES).get(0);
			return strongest;
		}
		if(myTroops.get(TroopType.IMPROVEDTANKS).size()>0){
			Troop strongest = myTroops.get(TroopType.IMPROVEDTANKS).get(0);
			return strongest;
		}
		if(myTroops.get(TroopType.TANKS).size()>0){
			Troop strongest = myTroops.get(TroopType.TANKS).get(0);
			return strongest;
		}
		if(myTroops.get(TroopType.ROCKETS).size()>0){
			Troop strongest = myTroops.get(TroopType.ROCKETS).get(0);
			return strongest;
		}
		if(myTroops.get(TroopType.AUTOMATIC).size()>0){
			Troop strongest = myTroops.get(TroopType.AUTOMATIC).get(0);
			return strongest;
		}
		if(myTroops.get(TroopType.INFANTRY).size()>0){
			Troop strongest = myTroops.get(TroopType.INFANTRY).get(0);
			return strongest;
		}
		return null;
	}
	
	public Troop getWeakest(){
		if(myTroops.get(TroopType.INFANTRY).size()>0){
			Troop weakest = myTroops.get(TroopType.INFANTRY).get(0);
			return weakest;
		}
		if(myTroops.get(TroopType.AUTOMATIC).size()>0){
			Troop weakest = myTroops.get(TroopType.AUTOMATIC).get(0);
			return weakest;
		}
		if(myTroops.get(TroopType.ROCKETS).size()>0){
			Troop weakest = myTroops.get(TroopType.ROCKETS).get(0);
			return weakest;
		}
		if(myTroops.get(TroopType.TANKS).size()>0){
			Troop weakest = myTroops.get(TroopType.TANKS).get(0);
			return weakest;
		}
		if(myTroops.get(TroopType.IMPROVEDTANKS).size()>0){
			Troop weakest = myTroops.get(TroopType.IMPROVEDTANKS).get(0);
			return weakest;
		}
		if(myTroops.get(TroopType.PLANES).size()>0){
			Troop weakest = myTroops.get(TroopType.PLANES).get(0);
			return weakest;
		}
		return null;
}
}
