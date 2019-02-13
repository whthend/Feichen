package fchen;

import java.math.BigInteger;

/**
 * This class encapsulates the proof data when the cloud proves that the storage
 * is intact.
 * 
 * @author Chen, Fei (https://sites.google.com/site/chenfeiorange/)
 * @author First draft on 04-11-2013. License: GNU GPL
 * @author Email: chenfeiorange@163.com
 * 
 */
public class ProofData
{
    protected BigInteger ip, proof;

    public ProofData(BigInteger ip, BigInteger proof)
    {
	this.ip = ip;
	this.proof = proof;
    }

    public void print()
    {
	System.out.println("The inner product and the proof are: ");
	System.out.println(ip.toString());
	System.out.println(proof.toString());
    }

}
