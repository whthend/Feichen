package fchen;

import java.math.BigInteger;

/**
 * This class encapsulates the public key information.
 * All the data fields are set to be public for ease use.
 * The only difference between a public key and a private key is the latter has the p, q where N = p * q. 
 * 
 * @author Chen, Fei  (https://sites.google.com/site/chenfeiorange/)
 * @author First draft on 18-06-2013. License: GNU GPL
 * @author Email: chenfeiorange@163.com
 *
 */
public class PublicKey
{
    public BigInteger N; // N = p * q
    public BigInteger e; // the exponent
    public BigInteger[] g; // a series of elements in Z{_N}^{*}
    public BigInteger[] h; // a series of elements in Z{_N}^{*}
    public int n; // the length of the sector, which is a vector of messages
    public int m; // the length of the coefficient vector. A file is divided
		   // into m blocks and each block contains n sectors.
    public PublicKey(BigInteger N, BigInteger e, BigInteger[] g, BigInteger[] h, int n, int m)
    {
	super();
	this.N = N;
	this.e = e;
	this.g = g;
	this.h = h;
	this.n = n;
	this.m = m;
    }
    
    public PublicKey()
    {
	
    }
}
