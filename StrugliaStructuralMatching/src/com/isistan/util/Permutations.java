package com.isistan.util;

import gnu.trove.list.array.TByteArrayList;

import java.util.LinkedList;

public class Permutations {
	public static LinkedList<TByteArrayList> permuteUnique(TByteArrayList list) {
		LinkedList<TByteArrayList> result = new LinkedList<TByteArrayList>();
		permuteUnique(list, 0, result);
		return result;
	}
	
	private static void permuteUnique(TByteArrayList list, int start, LinkedList<TByteArrayList> result) {
	 
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
	
	private static boolean containsDuplicate(TByteArrayList arr, int start, int end) {
		for (int i = start; i <= end-1; i++) {
			if (arr.getQuick(i) == arr.getQuick(end)) {
				return false;
			}
		}
		return true;
	}
	 
	private static void swap(TByteArrayList a, int i, int j) {
		byte temp = a.getQuick(i);
		a.setQuick(i, a.getQuick(j));
		a.setQuick(j, temp);
	}
}
