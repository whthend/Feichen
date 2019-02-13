package fchen;

import java.math.BigInteger;

/**
 * This class provides some useful function data converting and printing functions used in thie project. 
 * 
 * @author Chen, Fei  (https://sites.google.com/site/chenfeiorange/)
 * @author First draft on 18-06-2013. License: GNU GPL
 * @author Email: chenfeiorange@163.com
 *
 */
public class Utility
{
    /**
     * Convert an unsigned byte number into an big integer.
     * @param num Input number
     * @return A big integer
     */
    public static BigInteger byteToBigInteger(byte num)
    {
	byte[] temp = new byte[1];
	temp[0] = num;
	return new BigInteger(1, temp);
    }
    
    /**
     * Convert an array of unsigned byte number into an big integer array.
     * @param num Input number array
     * @return A big integer array
     */
    public static BigInteger[] byteArraryToBigInteger(byte[] num)
    {
	BigInteger[] result = new BigInteger[num.length];
	byte[] temp = new byte[1];

	for (int i = 0; i < num.length; i++)
	{
	    temp[0] = num[i];
	    result[i] = new BigInteger(1, temp);
	}
	
	return result;
    }
    
    /**
     * Print the big integer array.
     * @param array Input data
     */
    public static void printBigIntegerArray(BigInteger[] array)
    {
	for (int i = 0; i < array.length; i++)
	    System.out.println(array[i].toString());
    }

}
