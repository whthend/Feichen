package fchen;

import java.math.BigInteger;

/**
 * This class encapsulates the private key information.
 * All the data fields are set to be public for ease use.
 * The only difference between a public key and a private key is the latter has the p, q where N = p * q. 
 * 
 * @author Chen, Fei  (https://sites.google.com/site/chenfeiorange/)
 * @author First draft on 18-06-2013. License: GNU GPL
 * @author Email: chenfeiorange@163.com
 *
 */
public class PrivateKey
{
    public BigInteger N;	// public key N = p * q
    public BigInteger p;
    public BigInteger q;
    public BigInteger e;	// public key
    public BigInteger dp;	// e * dp = 1 mod p; it helps in constucting the signature
    public BigInteger dq;	// e * dq = 1 mod q; it helps in constucting the signature
    public BigInteger[] g;
    public BigInteger[] h;
    public int n; // the length of the sector, which is a vector of messages
    public int m; // the length of the coefficient vector. A file is divided
		   // into m blocks and each block contains n sectors.
    
    public PublicKey extractPublicKey()
    {
	return new PublicKey(N, e, g, h, n, m);
    }

    public PrivateKey(BigInteger N, BigInteger p, BigInteger q, BigInteger e, BigInteger[] g, BigInteger[] h, int n, int m)
    {
	super();
	this.N = N;
	this.p = p;
	this.q = q;
	this.e = e;
	this.g = g;
	this.h = h;
	this.n = n;
	this.m = m;
    }
    
    public PrivateKey()
    {

    }
    
    public void print()
    {
	System.out.println("The private key is:");
	System.out.print("n,  m is:" + n);
	System.out.println(",  " + m);
	System.out.println("N is:" + N.toString());
	System.out.println("p is:" + p.toString());
	System.out.println("q is:" + q.toString());
	System.out.println("e is:" + e.toString());
	for (int i = 0; i < n + 1; i++)
	    System.out.println("g[" + i + "] is:" + g[i].toString());
	for (int i = 0; i < m; i++)
	    System.out.println("h[" + i + "] is:" + h[i].toString());
	
    }

}
