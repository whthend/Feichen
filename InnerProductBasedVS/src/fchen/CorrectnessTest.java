package fchen;

import java.math.BigInteger;

/**
 * This class checks whether the protocol is implemented correctly.
 * 
 * @author Chen, Fei (https://sites.google.com/site/chenfeiorange/)
 * @author Last updated on 04-12-2016.
 * @author Email: chenfeiorange@163.com
 * 
 */
public class CorrectnessTest
{
    public static void main(String[] args)
    {

	String file = "D:\\cryptology.bib";
	InnerProductBasedVS vs = new InnerProductBasedVS(file);

	System.out.println("protocol key generation");
	vs.generateKey();
	System.out.println("protocol basic information:");
	vs.print();

	System.out.println("protocol outsource");
	vs.outsource();

	System.out.println("protocol audit");
	ChallengeData c = vs.audit(10);
	System.out.println("challenge information:");
	c.print();

	System.out.println("protocol prove");
	ProofData proof = vs.prove(c);
	proof.print();

	System.out.println("protocol verify: corrent proof");
	if (vs.verify(c, proof) == true)
	    System.out.println("the protocol is correct.");
	else
	    System.out.println("there is some bug in the implementation.");
	
	proof.proof = new BigInteger("123456");
	System.out.println("protocol verify: invalid proof");
	if (vs.verify(c, proof) == false)
	    System.out.println("the protocol is correct.");
	else
	    System.out.println("there is some bug in the implementation.");
	
	System.out.println("\n***********************************\ndeterministic auditing");
	String key = vs.auditDeterministic();
	proof = vs.proveDeterministic(key);
	System.out.println("protocol verify: corrent proof");
	if (vs.verifyDeterministic(key, proof) == true)
	    System.out.println("the protocol is correct.");
	else
	    System.out.println("there is some bug in the implementation.");
	
	proof.proof = new BigInteger("123456");
	System.out.println("protocol verify: invalid proof");
	if (vs.verifyDeterministic(key, proof) == false)
	    System.out.println("the protocol is correct.");
	else
	    System.out.println("there is some bug in the implementation.");
    }
}
