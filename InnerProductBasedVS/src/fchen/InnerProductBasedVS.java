package fchen;

import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

/**
 * This class implements a protocol that employs the string equality check
 * approach to solve the cloud storage integrity checking problem. Please refer
 * to our paper for the details of the protocol.
 * 
 * @author Chen, Fei (https://sites.google.com/site/chenfeiorange/)
 * @author Last updated on 04-12-2016.
 * @author Email: chenfeiorange@163.com
 * 
 */
public class InnerProductBasedVS
{
    private String file; // file to be outsourced
    private BigInteger[] data; // the file data
    private BigInteger[] shadow; // a kind of "authentication" data accompanying
				 // the file data

    private KeyData key; // secrete key of the protocol
    private int messageBitLength; // the bit length of a data block; it
				  // determines the underlying finite field of
				  // the message.
    private String keyPRF = "keyPRF"; // key for the pseudorandom function
    private String r = "999999"; // part of the secret key

    private long fileSize; // the size of the file in bytes
    private int blocks; // total number of data blocks, which is equal to
			// file_size / message_length

    /**
     * Read the file data and initialize the protocol object
     * 
     * @param file
     *            File to be outsourced
     */
    public InnerProductBasedVS(String file)
    {
	this.file = file;
	File f = new File(file);
	this.fileSize = f.length();
	this.messageBitLength = 1024; // each data block is 1024bits; it will
				      // always be a multiple of 8 bits

	this.blocks = (int) Math.ceil(this.fileSize * 8 / (double) messageBitLength);
	this.data = new BigInteger[this.blocks];
	this.shadow = new BigInteger[this.blocks];

	byte[] block = new byte[this.messageBitLength / 8];
	try
	{
	    FileInputStream source = new FileInputStream(this.file);
	    for (int i = 0; i < data.length; i++)
	    {
		source.read(block);
		this.data[i] = new BigInteger(1, block);
	    }
	    source.close();
	} catch (Exception e)
	{
	    System.out.println("Exception in InnerProductBasedVS when reading file into memory: " + e);
	}
    }

    /**
     * Generate the key of the protocol.
     */
    public void generateKey()
    {
	this.key = new KeyData(this.keyPRF, this.r, this.messageBitLength);
    }

    /**
     * Outsource the data that is specified by the input file name. The tuples (
     * m_i, g^(r*m+f_i) ) are outsourced.
     */
    public void outsource()
    {
	for (int i = 0; i < this.data.length; i++)

	{
	    BigInteger temp = this.key.r.multiply(this.data[i]);
	    temp = temp.add(this.key.getRandomElement(i));
	    this.shadow[i] = this.key.g.modPow(temp, this.key.p);
	}
    }

    /**
     * Generate a challenge to audit the cloud.
     * 
     * @return An object containing the indices and coefficients of the data
     *         blocks
     */
    public ChallengeData audit(int challengeLen)
    {
	BigInteger[] coefficients = new BigInteger[challengeLen]; // coefficients
								  // for the
								  // selected
								  // data blocks
	int[] index = new int[challengeLen];

	Random r = new Random();
	for (int i = 0; i < challengeLen; i++)
	{
	    index[i] = r.nextInt(this.blocks);

	    byte[] temp = new byte[this.messageBitLength / 8];
	    r.nextBytes(temp);
	    coefficients[i] = new BigInteger(1, temp);
	}

	return new ChallengeData(index, coefficients);
    }

    /**
     * Deterministic auditing: generate a key for the psedurandom generator.
     * 
     * @return a key for the psedurandom generator.
     */
    public String auditDeterministic()
    {
	String key = "";
	Random r = new Random();
	byte[] temp = new byte[32];
	r.nextBytes(temp);

	key = key + temp.toString();
	return key;
    }

    /**
     * Verify whether cloud's answer as in the 'proof' object is correct.
     * 
     * @param challenge
     *            : An object containing the indices and coefficients of the
     *            challenged data blocks
     * @param proof
     *            : A proof object containing the inner product and its proof as
     *            defined in our paper
     * @return true if the proof is correct; else false
     */
    public boolean verify(ChallengeData challenge, ProofData proof)
    {
	BigInteger temp = BigInteger.ZERO;
	for (int i = 0; i < challenge.coefficients.length; i++)
	    temp = temp.add(challenge.coefficients[i].multiply(this.key.getRandomElement(challenge.index[i])));

	temp = temp.add(this.key.r.multiply(proof.ip));
	temp = temp.mod(this.key.p.subtract(BigInteger.ONE));
	temp = this.key.g.modPow(temp, this.key.p);

	return temp.equals(proof.proof);

    }

    /**
     * Verify whether cloud's proof is correct.
     * @param key Key for a pseudorandom function to generate the secret challenge sequence.
     * @param proof A proof object returned by the cloud.
     * @return true if the proof is correct; else false.
     */
    public boolean verifyDeterministic(String key, ProofData proof)
    {
	BigInteger temp = BigInteger.ZERO;
	KeyData coefficient = new KeyData(key, "100", this.messageBitLength);
	
	for (int i = 0; i < this.data.length; i++)
	    temp = temp.add(coefficient.getRandomElement(i).multiply(this.key.getRandomElement(i)));

	temp = temp.add(this.key.r.multiply(proof.ip));
	temp = temp.mod(this.key.p.subtract(BigInteger.ONE));
	temp = this.key.g.modPow(temp, this.key.p);

	return temp.equals(proof.proof);

    }
    
    /**
     * Generate the proof data on receiving an auditing challenge. The cloud
     * computes \sum <m_i, s_i> and \product shadow_i ^ s_i
     * 
     * @param
     * @return
     */
    public ProofData prove(ChallengeData challenge)
    {
	BigInteger ip = BigInteger.ZERO, proof = BigInteger.ONE;
	BigInteger order = this.key.p.subtract(BigInteger.ONE);

	for (int i = 0; i < challenge.coefficients.length; i++)
	{
	    int j = challenge.index[i];
	    BigInteger c = challenge.coefficients[i];
	    
	    ip = ip.add(this.data[j].multiply(c));
	    ip = ip.mod(order);

	    proof = proof.multiply(this.shadow[j].modPow(c, this.key.p));
	    proof = proof.mod(this.key.p);
	}

	return new ProofData(ip, proof);
    }

    /**
     * Generate the proof data on receiving an deterministic auditing challenge. The cloud
     * computes \sum <m_i, s_i> and \product shadow_i ^ s_i from i = 0 to i = data_length - 1
     * 
     * @param
     * @return
     */
    public ProofData proveDeterministic(String key)
    {
	BigInteger ip = BigInteger.ZERO, proof = BigInteger.ONE;
	BigInteger order = this.key.p.subtract(BigInteger.ONE);
	
	KeyData coefficient = new KeyData(key, "100", this.messageBitLength);

	for (int i = 0; i < this.data.length; i++)
	{
	    BigInteger c = coefficient.getRandomElement(i);
	    ip = ip.add(this.data[i].multiply(c));
	    ip = ip.mod(order);

	    proof = proof.multiply(this.shadow[i].modPow(c, this.key.p));
	    proof = proof.mod(this.key.p);
	}

	return new ProofData(ip, proof);
    }

    /**
     * Benchmark uses. Return the file size in bytes.
     */
    public long getFileSize()
    {
	return this.fileSize;
    }

    /**
     * Benchmark uses.
     */
    public BigInteger[] getShadow()
    {
	return this.shadow;
    }

    public void print()
    {
	this.key.print();
    }
}
