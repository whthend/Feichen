package fchen;

import java.math.*;
import java.util.*;

/**
 * This class implements an improved version of the vector signature scheme in PKC'2012.
 * The security can be proven in the standard model under the RSA assumption.
 * The original security is based on the strong RSA assumption. 
 * 
 * @author Chen, Fei  (https://sites.google.com/site/chenfeiorange/)
 * @author First draft on 18-06-2013. License: GNU GPL
 * @author Email: chenfeiorange@163.com
 *
 */
public class SignatureRSA
{
    private int primeBitLength;   // bit length of the primes p,q where the modulus N is p * q
    private int messageBitLength; // the bit length of the message, which should be smaller than the length of the exponent e
    private int n; // the length of the sector, which is a vector of messages
    private int m; // the length of the coefficient vector. A file is divided
		   // into m blocks and each block contains n sectors.
    public  PublicKey  pk;  // an object encapsulates the public key, which can be extracted from the private key
    private PrivateKey sk;  // an object encapsulates the private key
    
    private Random rnd;
    
    /**
     * Construct an object to handle the signature work.
     * @param primeBitLength length of p,q which determines the security level of the signature scheme 
     * @param messageBitLength
     * @param n
     * @param m
     */
    public SignatureRSA(int primeBitLength, int messageBitLength, int n, int m)
    {
	this.primeBitLength   = primeBitLength;
	this.messageBitLength = messageBitLength;
	this.n = n;
	this.m = m;	
	
	this.rnd  = new Random(0xffeeffee);	
	this.sk   = new PrivateKey();
	this.sk.n = n;
	this.sk.m = m;
	this.sk.g = new BigInteger[n + 1];  // g[0] ... g[n-1] are used to sign the message, i.e. g[i]^m[i]; g[n] is used to randomize the signature, i.e. g[n]^s
	this.sk.h = new BigInteger[m];
    }
    
    /**
     * Sign a message, which is an vector of big integers. All arithmetics are done in a finite field or ring.
     * The algorithm is as follows: x^e = g^s * (\product g_i^m_i) * (\product h_i^coefficient_i) mod N;
     * (s,x) is the signature.
     * @param message A vector of big integers.
     * @param flag If flag < 0, the coefficient is included in message, i.e. dimension of message is n + m;
     *             else, the coefficient of the message is an unitary vector and 'flag' indicates its non-zero position, i.e. dimension of message is n.
     * @return (s,x)
     */
    public BigInteger[] sign(BigInteger[] message, int flag)
    {
	BigInteger s = new BigInteger(this.messageBitLength, rnd); // s is uniform in Z_e	
	
	BigInteger t = this.sk.g[n].modPow(s, this.sk.N);	   // g^s; the last element of the array g[] is just "g" as in the paper
	for (int i = 0; i < n; i++)
	{
	    t = t.multiply( this.sk.g[i].modPow(message[i], this.sk.N) );
	    t = t.mod(this.sk.N);
	}  //(\product g_i^m_i)
	
	if (flag >= 0)
	{
	    t = t.multiply(this.sk.h[flag]);
	    t = t.mod(this.sk.N);
	}
	else
	{
	    for (int i = 0; i < m; i++)
	    {
		t = t.multiply( this.sk.h[i].modPow(message[n + i], this.sk.N) );
		t = t.mod(this.sk.N);
	    }
	}  // (\product h_i^coefficient_i);
	
	BigInteger xp = t.mod(sk.p), xq = t.mod(sk.q);
	xp = xp.modPow(sk.dp, sk.p);
	xq = xq.modPow(sk.dq, sk.q);
	
	BigInteger x;
	x = xp.multiply( sk.q.multiply(sk.q.modInverse(sk.p)) );
	x = x.add(  xq.multiply( sk.p.multiply(sk.p.modInverse(sk.q)) )  );
	x = x.mod(sk.N);
	
	BigInteger[] result = new BigInteger[2];
	result[0] = s;
	result[1] = x; 
		
	return result;
    }
    
    /**
     * Verify the correctness of the signature of a vector of messages, i.e. checking
     * whether x^e = g^s * (\product g_i^m_i) * (\product h_i^coefficient_i) mod N.
     * @param message A vector of messages.
     * @param signature (s,x)
     * @param flag If flag < 0, dimension of message is n + m, i.e. the coefficient in contained in the message;
     *             else, dimension of message is n.
     * @return true if the signature is correct; else false.
     */
    public boolean verify(BigInteger[] message, BigInteger[] signature, int flag)
    {
	BigInteger left, right;  // left hand and right hand of the signature equation
	
	left  = signature[1].modPow(sk.e, sk.N);
	right = sk.g[n].modPow(signature[0], sk.N);
	for (int i = 0; i < n; i++)
	{
	    right = right.multiply( this.sk.g[i].modPow(message[i], this.sk.N) );
	    right = right.mod(sk.N);
	}
	if (flag >= 0)
	{
	    right = right.multiply(this.sk.h[flag]);
	    right = right.mod(sk.N);
	}
	else
	{
	    for (int i = 0; i < m; i++)
	    {
		right = right.multiply( this.sk.h[i].modPow(message[n + i], this.sk.N) );
		right = right.mod(sk.N);
	    }
	}
	
	if (left.equals(right))
	    return true;
	else
	    return false;
    }
    
    /**
     * Compute the signature of a linear combination of messages.
     * @param message A group of message vectors of dimension n + m.
     * @param signature A group of message signatures of dimension 2.
     * @param coefficients Linear coefficients.
     * @param newMessage The result message of the linear combination. It is used as an output and should be initialized before being passed to the function.
     * @return The signature for the new message.
     */
    public BigInteger[] combine(BigInteger[][] message, BigInteger[][] signature, BigInteger[] coefficients, BigInteger[] newMessage)
    {
	BigInteger s = BigInteger.ZERO, x = BigInteger.ONE, s1;  //s1 = (s - s mod e) / e
	BigInteger[] m1 = new BigInteger[n + m];  // m1 = (m - m mod e) / e
	for (int i = 0; i < coefficients.length; i++)  // in a previous version, I mistook i = 1 - error!
	{
	    s = s.add(coefficients[i].multiply(signature[i][0]));	   
	    x = x.multiply(signature[i][1].modPow(coefficients[i], sk.N));
	    x = x.mod(sk.N);
	}
	s1 = s.subtract(s.mod(sk.e));
	s1 = s1.divide(sk.e);
	s  = s.mod(sk.e);
	
	for (int i = 0; i < n + m; i++)
	{
	    for (int j = 0; j < coefficients.length; j ++)
		newMessage[i] = newMessage[i].add(message[j][i].multiply(coefficients[j]));
	    m1[i] = newMessage[i].subtract(newMessage[i].mod(sk.e));
	    m1[i] = m1[i].divide(sk.e);
	    newMessage[i] = newMessage[i].mod(sk.e);
	}
	
	BigInteger xTemp = sk.g[n].modPow(s1, sk.N);  // g^s'
	for (int i = 0; i < n; i++)
	{
	    xTemp = xTemp.multiply(sk.g[i].modPow(m1[i], sk.N));
	    xTemp = xTemp.mod(sk.N);
	}
	for (int i = 0; i < m; i++)
	{
	    xTemp = xTemp.multiply(sk.h[i].modPow(m1[i + n], sk.N));
	    xTemp = xTemp.mod(sk.N);
	}
	xTemp = xTemp.modInverse(sk.N);
	x     = x.multiply(xTemp);
	x     = x.mod(sk.N);
	
	BigInteger[] result = new BigInteger[2];
	result[0] = s;
	result[1] = x; 
	
	return result;
    }
    
    /**
     * Generate the private and public key for the signature scheme.
     */
    public void generateKey()
    {   //N e g h p q
	sk.p = new BigInteger(this.primeBitLength, 10, rnd);
	sk.q = new BigInteger(this.primeBitLength, 10, rnd);
	sk.N = sk.p.multiply(sk.q);
	//sk.e = new BigInteger(this.messageBitLength + 1, 10, rnd);
	sk.e = new BigInteger("257");  // we use finite field Z_257 for performance evaluation
	
	int totalLength = sk.N.bitLength();
	for (int i = 0; i <= sk.n; i++)
	    sk.g[i] = new BigInteger(totalLength - 1, rnd);
	for (int i = 0; i < sk.m; i++)
	    sk.h[i] = new BigInteger(totalLength - 1, rnd);
	
	this.pk = this.sk.extractPublicKey();
	this.precompute();
    }
    
    /**
     * Check whether gcd(e, p - 1) = 1 and gcd(e, q - 1) = 1.
     * @return 'true' if the key is valid; else 'false' 
     */
    public boolean isKeyValid()
    {
	if (sk.e.gcd(sk.p.subtract(BigInteger.ONE)).equals(BigInteger.ONE) == false)
	    return false;
	if (sk.e.gcd(sk.q.subtract(BigInteger.ONE)).equals(BigInteger.ONE) == false)
	    return false;
	
	return true;
    }
    
    /**
     * Compute e^-1 mod p - 1 and e^-1 mod q - 1.
     * This helps to find the x such that x^e = t mod N given (e, t, N = p * q).
     */
    public void precompute()
    {
	sk.dp = sk.e.modInverse(sk.p.subtract(BigInteger.ONE));
	sk.dq = sk.e.modInverse(sk.q.subtract(BigInteger.ONE));
    }
    
    public void printSecretKey()
    {
	this.sk.print();
    }

}
