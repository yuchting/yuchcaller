package com.yuchs.yuchcaller.db;

/**
 * bineary search for number
 * @author tzz
 *
 */
public abstract class BinSearchNumber {
	
	/**
	 * child class must derive this function
	 * to compare a number to bineary search
	 * @param _number
	 * @return
	 */
	public abstract int Compare(int _number);
}
