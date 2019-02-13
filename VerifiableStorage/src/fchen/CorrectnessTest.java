package fchen;

import java.math.BigInteger;

public class CorrectnessTest
{
    public static void main(String[] args)
    {
	//testSignature();
	testProtocol();
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
	
	rsa.printSecretKey();
	
	int messageSize = 4;
	
//	BigInteger[] message = new BigInteger[messageSize];
//	for (int i = 0; i < messageSize; i++)
//	    message[i] = new BigInteger("80");
//	
//	BigInteger[] signature = rsa.sign(message, -1);
//	
//	System.out.println("The message is:");
//	Utility.printBigIntegerArray(message);
//	System.out.println("The index is 0");
//	System.out.println("The signature is:");
//	Utility.printBigIntegerArray(signature);
//	
//	Boolean b = rsa.verify(message, signature, -1);
//	if (b == true)
//	    System.out.println("signature verification succeeds.");
//	else
//	    System.out.println("signature verification fails.");
	
	int challengeSize = 3;
	BigInteger[][] m2 = new BigInteger[challengeSize][messageSize];
	for (int i = 0; i < challengeSize; i++)
	    for (int j = 0; j < messageSize; j++)
		m2[i][j] = new BigInteger("90");
	
	BigInteger[][] s2 =new BigInteger[challengeSize][]; 
	for (int i = 0; i < challengeSize; i++)
	    s2[i] = rsa.sign(m2[i], -1);
	
	System.out.println("checking each message and its signature...");
	for (int i = 0; i < challengeSize; i++)
	{
	    if (rsa.verify(m2[i], s2[i], -1) == true)
		System.out.println("signature verification succeeds.");
	    else
		System.out.println("signature verification fails.");
	}
	
	BigInteger[] coefficients = new BigInteger[challengeSize], newMessage = new BigInteger[messageSize];
	for (int i = 0; i < challengeSize; i++)
	    coefficients[i] = new BigInteger("100");
	
	for (int i = 0; i < messageSize; i++)
	    newMessage[i] = new BigInteger("0");
	
	BigInteger[] aggregateSignature = rsa.combine(m2, s2, coefficients, newMessage);
	
	for (int i = 0; i < challengeSize; i++)
	{
	    System.out.println("message and signature " + i + ":");
	    Utility.printBigIntegerArray(m2[i]);
	    Utility.printBigIntegerArray(s2[i]);
	}
	System.out.println("the aggregated message is:");
	Utility.printBigIntegerArray(newMessage);
	System.out.println("the aggregated signature is:");
	Utility.printBigIntegerArray(aggregateSignature);
	
	if (rsa.verify(newMessage, aggregateSignature, -1) == true)
		System.out.println("aggregate signature verification succeeds.");
	else
		System.out.println("aggregate signature verification fails.");
    }
    
    public static void testProtocol()
    {
	String file = "D:\\cryptology.bib";
	VerifiableStorage vs = new VerifiableStorage(file);
	
	System.out.println("protocol key generation");
	vs.generateKey();
	System.out.println("protocol basic information:");
	vs.print();
	
	System.out.println("protocol outsource");
	vs.outsource();	
	
	System.out.println("protocol audit");
	ChallengeData c = vs.audit();
	System.out.println("challenge information:");
	c.print();
	
	System.out.println("protocol prove");
	ProofData proof = vs.prove(c);
	
	System.out.println("protocol verify");
	if (vs.verify(c, proof) == true)
	    System.out.println("the protocol is correct.");
	else
	    System.out.println("there is some bug in the implementation.");	
    }

}
