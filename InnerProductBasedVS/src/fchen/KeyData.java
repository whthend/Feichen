package fchen;

import java.math.BigInteger;
import java.security.*;
import javax.crypto.*;
import java.util.*;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;

/**
 * This class encapsulates(封装) all secret key material in one object.
 * The key material includes the finite field information, the random
 * field element r and the pseudorandom function.
 *  
 * @author fchen
 * @author First draft on 04-11-2013. License: GNU GPL
 * @author Email: chenfeiorange@163.com
 */
public class KeyData
{
    public int bitLen;   // the bit length of the underlying finite field 
    public BigInteger p;  // prime number which determines the field
    public BigInteger g;  // generator of the field
    
    public BigInteger r;  // a random element of the field
    public String keyPRF;  // key for the pseudorandom function
    
    private Mac mac;  // HMAC is used here to construct a pseudorandom function
    private SecureRandom seed;  // for HMAC use
    private KeyGenerator kg;  // for HMAC use
    private SecretKey sk;  // for HMAC use

    /**
     * Construct all the secret key material.
     * @param keyPRF Key for pseudorandom function
     * @param r Random finite field number
     * @param bitLen Bit length of the finite field
     */
    public KeyData(String keyPRF, String r, int bitLen)
    {
			// first generate the random finite field
			Random temp = new Random();
			this.p = BigInteger.probablePrime(bitLen, temp);
			this.r = new BigInteger(r);
			
			byte[] gTemp = new byte[bitLen / 8];
			temp.nextBytes(gTemp);
			this.g = new BigInteger(1, gTemp); 
				
			this.bitLen = bitLen;

			this.seed = new SecureRandom(keyPRF.getBytes());
			try
				{
					this.kg = KeyGenerator.getInstance("HmacSHA256");
					this.kg.init(this.seed);
					this.sk = this.kg.generateKey();
					this.mac = Mac.getInstance("HmacSHA256");
					this.mac.init(this.sk);
				} catch (Exception e)
				{
					System.out.println("Exception in KeyData: initializing mac: " + e);
				}
				}

    /**
     * It implements a pseudorandom function using HMAC as the underlying constructing component.
     * @param index Psedudorandom function input
     * @return Psedudorandom function Output
     */
    public BigInteger getRandomElement(int index)
    {
			int length = bitLen / 8;
			byte[] value = new byte[length];

			int loop = (bitLen / 256) * 32;
			String temp = "";
			int i = 0;
			for (i = 0; i < loop; i = i + 32) // 256 bits are 32 bytes;
			{
				temp = temp + Integer.toString(index);
				this.mac.update(temp.getBytes());
				try{
					this.mac.doFinal(value, i);
				}
				catch (Exception e){
					System.out.println("Exception when generating a random element: " + e);
				}
			}
			
			if (loop % 256 != 0)
			{
				temp = temp + Integer.toString(index);
				byte[] tempMac = this.mac.doFinal(temp.getBytes());
				for (int j = i; j < value.length; j++)
				value[j] = tempMac[j - i];
			}

			return new BigInteger(1, value);
    }

    /**
     * For deug use. Show all the key information.
     */
    public void print()
    {
			System.out.println("The system key is as follows:");
			System.out.println("p: " + p.toString());
			System.out.println("g: " + g.toString());
			System.out.println("r: " + r.toString());
			System.out.println("block size: " + bitLen);
    }
}
