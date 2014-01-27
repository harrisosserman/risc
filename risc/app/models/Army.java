package models;

import java.util.*;

public class Army{

	private ArrayList<Troop> myTroops;

	public Army(int totalStrength){
		myTroops = new ArrayList<Troop>();

		for (int i = 0; i < totalStrength; i++) {
			Troop t = new Troop();
			myTroops.add(t);
		}
		System.out.println("created an army");
	}

	public Army branch(int totalStrength){
		Army branchedArmy = new Army(totalStrength);
		myTroops.subList(0, totalStrength).clear();
		return branchedArmy;
	}

	public int getTotalStrength(){
		int totalStrength = 0;
		for (Troop t : myTroops) {
			totalStrength += t.getStrength();
		}
		return totalStrength;
	}
}