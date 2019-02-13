package fchen;

import javax.crypto.*;
import javax.crypto.spec.*;

public class Learning
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {

	// TODO Auto-generated method stub
	
	System.out.println((int) (5.4 / 3));

	SecretKeySpec key = new SecretKeySpec("123".getBytes(), "HmacSHA256");
	String temp = null;

	try
	{
	    Mac test = Mac.getInstance("HmacSHA256");
	    test.init(key);
	    temp = bytes2HexString(test.doFinal("123".getBytes()));
	} catch (Exception e)
	{
	    System.out.println(e);
	}
	System.out.println(temp);
    }

    private final static byte[] hex = "0123456789ABCDEF".getBytes();

    public static String bytes2HexString(byte[] b)
    {
	byte[] buff = new byte[2 * b.length];
	for (int i = 0; i < b.length; i++)
	{
	    buff[2 * i] = hex[(b[i] >> 4) & 0x0f];
	    buff[2 * i + 1] = hex[b[i] & 0x0f];
	}
	return new String(buff);
    }

}
