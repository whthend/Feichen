package fchen;

import java.math.BigInteger;

/**
 * This class encapsulates the challenge data when auditing the cloud. 
 * 
 * @author Chen, Fei  (https://sites.google.com/site/chenfeiorange/)
 * @author First draft on 18-06-2013. License: GNU GPL
 * @author Email: chenfeiorange@163.com
 *
 */
public class ChallengeData
{
    public BigInteger[] coefficients;  // coefficients for the selected data  blocks
    public int[] index;  // indices for the selected data blocks
    
    public ChallengeData()
    {
	
    }
    
    public void print()
    {
	System.out.println("index   chanllenge:");
	for (int i = 0; i < index.length; i++)
	{
	    System.out.println(index[i] + "   " + coefficients[i].toString());
	}
    }

}
