package fchen;

import java.math.BigInteger;

/**
 * This class encapsulates the proof data when the cloud proves that the storage is intact. 
 * 
 * @author Chen, Fei  (https://sites.google.com/site/chenfeiorange/)
 * @author First draft on 18-06-2013. License: GNU GPL
 * @author Email: chenfeiorange@163.com
 *
 */
public class ProofData
{
    BigInteger[] aggregateMessage, aggregateSignature;
    int size, signatureLen;
    
    public ProofData(int size, int signatureLen)
    {
	this.size         = size;
	this.signatureLen = signatureLen;
	
	this.aggregateMessage   = new BigInteger[this.size];  // linear combination of the selected data blocks
	for (int i = 0; i < this.size; i++)
	    this.aggregateMessage[i] = BigInteger.ZERO;
	
	this.aggregateSignature = new BigInteger[this.signatureLen];
	for (int i = 0; i < this.signatureLen; i++)
	    this.aggregateSignature[i] = new BigInteger("-1");
    }
    
    public void print()
    {
	System.out.println("the aggregated message is:");
	for (int i = 0; i < this.aggregateMessage.length; i++)
	    System.out.println(this.aggregateMessage[i].toString());
	
	System.out.println("the aggregated signature is:");
	for (int i = 0; i < this.aggregateSignature.length; i++)
	    System.out.println(this.aggregateSignature[i].toString());
    }

}
