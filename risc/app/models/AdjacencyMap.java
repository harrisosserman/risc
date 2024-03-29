package models;

import java.util.*;

public class AdjacencyMap{

	private HashMap<Integer, ArrayList<Integer>> myAdjacencies; 
	public AdjacencyMap(){
		myAdjacencies = createMap();
	}
	public HashMap<Integer, ArrayList<Integer>> createMap(){
                HashMap<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();
                ArrayList<Integer> terr_0 = new ArrayList<Integer>();
                terr_0.add(1);
                terr_0.add(5);
                terr_0.add(6);
                ArrayList<Integer> terr_1 = new ArrayList<Integer>();
                terr_1.add(0);
                terr_1.add(2);
                terr_1.add(5);
                terr_1.add(6);
                terr_1.add(7);
                ArrayList<Integer> terr_2 = new ArrayList<Integer>();
                terr_2.add(1);
                terr_2.add(3);
                terr_2.add(6);
                terr_2.add(7);
                terr_2.add(8);
                ArrayList<Integer> terr_3 = new ArrayList<Integer>();
                terr_3.add(2);
                terr_3.add(4);
                terr_3.add(7);
                terr_3.add(8);
                terr_3.add(9);
                ArrayList<Integer> terr_4 = new ArrayList<Integer>();
                terr_4.add(3);
                terr_4.add(8);
                terr_4.add(9);
                ArrayList<Integer> terr_5 = new ArrayList<Integer>();
                terr_5.add(0);
                terr_5.add(1);
                terr_5.add(6);
                terr_5.add(10);
                terr_5.add(11);
                ArrayList<Integer> terr_6 = new ArrayList<Integer>();
                terr_6.add(0);
                terr_6.add(1);
                terr_6.add(2);
                terr_6.add(5);
                terr_6.add(7);
                terr_6.add(10);
                terr_6.add(11);
                terr_6.add(12);
                ArrayList<Integer> terr_7 = new ArrayList<Integer>();
                terr_7.add(1);
                terr_7.add(2);
                terr_7.add(3);
                terr_7.add(6);
                terr_7.add(8);
                terr_7.add(11);
                terr_7.add(12);
                terr_7.add(13);
                ArrayList<Integer> terr_8 = new ArrayList<Integer>();
                terr_8.add(2);
                terr_8.add(3);
                terr_8.add(4);
                terr_8.add(7);
                terr_8.add(9);
                terr_8.add(12);
                terr_8.add(13);
                terr_8.add(14);
                ArrayList<Integer> terr_9 = new ArrayList<Integer>();
                terr_9.add(3);
                terr_9.add(4);
                terr_9.add(8);
                terr_9.add(13);
                terr_9.add(14);
                ArrayList<Integer> terr_10 = new ArrayList<Integer>();
                terr_10.add(5);
                terr_10.add(6);
                terr_10.add(11);
                terr_10.add(15);
                terr_10.add(16);
                ArrayList<Integer> terr_11 = new ArrayList<Integer>();
                terr_11.add(5);
                terr_11.add(6);
                terr_11.add(7);
                terr_11.add(10);
                terr_11.add(12);
                terr_11.add(15);
                terr_11.add(16);
                terr_11.add(17);
                ArrayList<Integer> terr_12 = new ArrayList<Integer>();
                terr_12.add(6);
                terr_12.add(7);
                terr_12.add(8);
                terr_12.add(11);
                terr_12.add(13);
                terr_12.add(16);
                terr_12.add(17);
                terr_12.add(18);
                ArrayList<Integer> terr_13 = new ArrayList<Integer>();
                terr_13.add(7);
                terr_13.add(8);
                terr_13.add(9);
                terr_13.add(12);
                terr_13.add(14);
                terr_13.add(17);
                terr_13.add(18);
                terr_13.add(19);
                ArrayList<Integer> terr_14 = new ArrayList<Integer>();
                terr_14.add(8);
                terr_14.add(9);
                terr_14.add(13);
                terr_14.add(18);
                terr_14.add(19);
                ArrayList<Integer> terr_15 = new ArrayList<Integer>();
                terr_15.add(10);
                terr_15.add(11);
                terr_15.add(16);
                terr_15.add(20);
                terr_15.add(21);
                ArrayList<Integer> terr_16 = new ArrayList<Integer>();
                terr_16.add(10);
                terr_16.add(11);
                terr_16.add(12);
                terr_16.add(15);
                terr_16.add(17);
                terr_16.add(20);
                terr_16.add(21);
                terr_16.add(22);
                ArrayList<Integer> terr_17 = new ArrayList<Integer>();
                terr_17.add(11);
                terr_17.add(12);
                terr_17.add(13);
                terr_17.add(16);
                terr_17.add(18);
                terr_17.add(21);
                terr_17.add(22);
                terr_17.add(23);
                ArrayList<Integer> terr_18 = new ArrayList<Integer>();
                terr_18.add(12);
                terr_18.add(13);
                terr_18.add(14);
                terr_18.add(17);
                terr_18.add(19);
                terr_18.add(22);
                terr_18.add(23);
                terr_18.add(24);
                ArrayList<Integer> terr_19 = new ArrayList<Integer>();
                terr_19.add(13);
                terr_19.add(14);
                terr_19.add(18);
                terr_19.add(23);
                terr_19.add(24);
                ArrayList<Integer> terr_20 = new ArrayList<Integer>();
                terr_20.add(15);
                terr_20.add(16);
                terr_20.add(21);
                ArrayList<Integer> terr_21 = new ArrayList<Integer>();     
                terr_21.add(15);
                terr_21.add(16);
                terr_21.add(17);
                terr_21.add(20);
                terr_21.add(22);
                ArrayList<Integer> terr_22 = new ArrayList<Integer>();
                terr_22.add(16);
                terr_22.add(17);
                terr_22.add(18);
                terr_22.add(21);
                terr_22.add(23);
                ArrayList<Integer> terr_23 = new ArrayList<Integer>();
                terr_23.add(17);
                terr_23.add(18);
                terr_23.add(19);
                terr_23.add(22);
                terr_23.add(24);
                ArrayList<Integer> terr_24 = new ArrayList<Integer>();
                terr_24.add(18);
                terr_24.add(19);
                terr_24.add(23);

                map.put(0, terr_0);
                map.put(1, terr_1);
                map.put(2, terr_2);
                map.put(3, terr_3);
                map.put(4, terr_4);
                map.put(5, terr_5);
                map.put(6, terr_6);
                map.put(7, terr_7);
                map.put(8, terr_8);
                map.put(9, terr_9);
                map.put(10, terr_10);
                map.put(11, terr_11);
                map.put(12, terr_12);
                map.put(13, terr_13);
                map.put(14, terr_14);
                map.put(15, terr_15);
                map.put(16, terr_16);
                map.put(17, terr_17);
                map.put(18, terr_18);
                map.put(19, terr_19);
                map.put(20, terr_20);
                map.put(21, terr_21);
                map.put(22, terr_22);
                map.put(23, terr_23);
                map.put(24, terr_24);
                return map;
	}

	public ArrayList<Integer> getAdjacencies(Integer terr){
		return myAdjacencies.get(terr);
	}
} 