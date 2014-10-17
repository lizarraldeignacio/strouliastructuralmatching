package com.isistan.util;

import gnu.trove.list.array.TShortArrayList;
import java.util.LinkedList;

public class Permutations {
	public static LinkedList<TShortArrayList> permuteUnique(TShortArrayList list) {
		LinkedList<TShortArrayList> result = new LinkedList<TShortArrayList>();
		permuteUnique(list, 0, result);
		return result;
	}
	
	private static void permuteUnique(TShortArrayList list, int start, LinkedList<TShortArrayList> result) {
	 
		if (start >= list.size() ) {
			result.add(list);
		}
	 
		for (int j = start; j <= list.size()-1; j++) {
			if (containsDuplicate(list, start, j)) {
				swap(list, start, j);
				permuteUnique(list, start + 1, result);
				swap(list, start, j);
			}
		}
	}
	
	private static boolean containsDuplicate(TShortArrayList arr, int start, int end) {
		for (int i = start; i <= end-1; i++) {
			if (arr.getQuick(i) == arr.getQuick(end)) {
				return false;
			}
		}
		return true;
	}
	 
	private static void swap(TShortArrayList a, int i, int j) {
		short temp = a.getQuick(i);
		a.setQuick(i, a.getQuick(j));
		a.setQuick(j, temp);
	}
}
