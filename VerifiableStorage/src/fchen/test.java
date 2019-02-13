package fchen;

import java.math.BigInteger;

public class test
{
    public static void main(String[] args)
    {
	
    }
    
    public static void testBasic()
    {
	byte a = (byte)0xff;
	System.out.println(a);
	System.out.println((int)a);
	
	byte[] c = new byte[1];
	c[0] = (byte)0xff;
	BigInteger b = new BigInteger(1, c);
	System.out.println(b.toString());
    }
    
    public static void testSignature()
    {
	SignatureRSA rsa = new SignatureRSA(17, 8, 2, 2);
	rsa.generateKey();
	while (rsa.isKeyValid() == false)
	{
	    rsa.generateKey();
	}
	
	BigInteger[] message = new BigInteger[2];
	for (int i = 0; i < 2; i++)
	    message[i] = new BigInteger("80");
	
	BigInteger[] signature = rsa.sign(message, null, 0);
	
	System.out.println("The message is:");
	Utility.printBigIntegerArray(message);
	System.out.println("The index is 0");
	System.out.println("The signature is:");
	Utility.printBigIntegerArray(message);
	
	Boolean b = rsa.verify(message, signature, 0);
	if (b == true)
	    System.out.println("signature verification succeeds.");
	else
	    System.out.println("signature verification fails.");
	
    }

}
