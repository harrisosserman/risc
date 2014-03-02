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

    public Player getOwner(){
        return myOwner;
    }

    public String getName(){
        return myOwner.getName();
    }
    
	/*public void addTroop(TroopType type, Troop t){
		ArrayList<Troop> troops = myTroops.get(type);
		troops.add(t);
		myTroops.put(type, troops);
	}*/
	public ArrayList<Troop> getArrayOfTroops(){
		ArrayList<Troop> troops = new ArrayList<Troop>();
		for(TroopType type : myTroops.keySet()){
			ArrayList<Troop> t = myTroops.get(type);
			for(Troop troop: t){
				troops.add(troop);
			}
		}
		return troops;
	}

	public int getNumberOfTroops(TroopType type){
		ArrayList<Troop> t = myTroops.get(type);
		return t.size();
	}

	public int getNumberOfTroops(){
		int troops = 0;
		for(TroopType type : myTroops.keySet()){
			ArrayList<Troop> t = myTroops.get(type);
			troops = troops + t.size();
		}
		return troops;
	}

	public HashMap<TroopType, ArrayList<Troop>> getTroops(){
		return myTroops;
	}

	public void setTroops(HashMap<TroopType, ArrayList<Troop>> troops){
		myTroops = troops;
	}

	public void addTroops(ArrayList<Troop> troops){
		for(Troop t : troops){
			addTroop(t);
		}
		
	}
	public boolean containsTroop(TroopType type){
		if(myTroops.get(type).size()>0){
			return true;
		}
		else{
			return false;
		}
	}

	public void addTroop(Troop t){
		ArrayList troops = myTroops.get(t.getType());
		troops.add(t);
		myTroops.put(t.getType(), troops);
	}

	public void addTroop(TroopType type){
		ArrayList troops = myTroops.get(type);
		troops.add(new Troop(myOwner, type));
		myTroops.put(type, troops);
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
	public void deleteWeakest(){
		if(myTroops.get(TroopType.INFANTRY).size()>0){
			deleteTroop(TroopType.INFANTRY);
		}
		else if(myTroops.get(TroopType.AUTOMATIC).size()>0){
			deleteTroop(TroopType.AUTOMATIC);
		}
		else if(myTroops.get(TroopType.ROCKETS).size()>0){
			deleteTroop(TroopType.ROCKETS);
		}
		else if(myTroops.get(TroopType.TANKS).size()>0){
			deleteTroop(TroopType.TANKS);
		}
		else if(myTroops.get(TroopType.IMPROVEDTANKS).size()>0){
			deleteTroop(TroopType.IMPROVEDTANKS);
		}
		else if(myTroops.get(TroopType.PLANES).size()>0){
			deleteTroop(TroopType.PLANES);
		}
    }
}
