package fchen;

import java.io.*;
import java.math.BigInteger;
import java.util.Random;

/**
 * This class implements the protocol which enables a client to check the integrity of the data in the cloud.
 * For the protocol, please refer to our paper.
 * Our papers links two different areas: network coding and cloud storage security. 
 * 
 * @author Chen, Fei  (https://sites.google.com/site/chenfeiorange/)
 * @author First draft on 18-06-2013. License: GNU GPL
 * @author Email: chenfeiorange@163.com
 *
 */
public class VerifiableStorage
{
    private String originalFile;  // input file name
    //private String processedFile; // file name with authentication information embeded
    byte[][] buffer;  // stores the input file data in the main memory
    BigInteger[][] signature; // stores the signatures of the input file data
	
    private SignatureRSA rsa;
    private long fileSize;  // the size of the original file in bytes
    
    private int n;  // total number of sectors in a block
    private int m;  // total number of blocks in a file
    private int messageBitLength; // the bit length of a sector; it determines the underlying finite field of the message.
    //private int p;  // finite field of the message; it is also the field for network coding
    
    private int challengeLen;  // the length of the auditing challenge
    
    /**
     * Construct an object handling the protocol execution.
     * The block size is 1KB or 1MB, i.e. n = 1024 or 1024 * 1024.
     * @param originalFile Input file name
     */
    public VerifiableStorage(String originalFile)
    {
	this.originalFile = originalFile;
	//this.processedFile = processedFile;
	this.n = 1024;  // 1KB = 1024B 1MB = 1024KB
	File f = new File(originalFile);
	this.fileSize = f.length();
	this.m = (int)Math.ceil(this.fileSize / (long)this.n);
	this.messageBitLength = 8; // m \in Z_{2^8+1}
	//this.p = 257;  // it equals to rsa.e
	this.rsa = new SignatureRSA(1024, this.messageBitLength, this.n, this.m);
	
	this.buffer = new byte[this.m][this.n];
	this.signature = new BigInteger[this.m][2];
	
	try
	{
	    FileInputStream source = new FileInputStream(this.originalFile);
	    for (int i = 0; i < this.m - 1; i++)
	   	source.read(buffer[i]);
	    for (int i = 0; i < this.n -1; i++)  // for performance evaluation, we don't handle the last block; however, we need to handle it well in practice.
		buffer[this.m - 1][i] = 0;
	    
	    source.close();
	}
	catch(Exception e)
	{
	    System.out.println(e.toString());
	}
	this.challengeLen = 10;
    }
    
    /**
     * Generate the key of the protocol.
     */
    public void generateKey()
    {
	//initialize SignatureRSA
	this.rsa.generateKey();
	
	while (this.rsa.isKeyValid() == false)
	{
	    this.rsa.generateKey();
	}
    }
    
    /**
     * Outsource the data that is specified by the input file name.
     */
    public void outsource()
    {
	for (int i = 0; i < this.m; i++)
	    this.signature[i] = rsa.sign(Utility.byteArraryToBigInteger(buffer[i]), i);
	
	//output processed file in practice
    }
    
    /**
     * Generate a challenge to audit the cloud.
     * @return An object containing the indices and coefficients of the data blocks
     */
    public ChallengeData audit()
    {
	ChallengeData c = new ChallengeData();
	// send challenges to the cloud
	byte[] temp = new byte[this.challengeLen];
	Random rnd = new Random();
	rnd.nextBytes(temp);
	
	c.index = new int[this.challengeLen];
	for (int i = 0; i < this.challengeLen; i++)
	    c.index[i] = rnd.nextInt(this.m);
	c.coefficients = Utility.byteArraryToBigInteger(temp);
	
	return c;
    }
    
    /**
     * Verify whether cloud's answer as in the 'proof' object is correct.
     * @param challenge An object containing the challenge data, which is sent from the client
     * @param proof An object containing the proof data, which is sent by the cloud
     * @return 'true' if the cloud is honest; 'false' if the cloud is malicious
     */
    public boolean verify(ChallengeData challenge, ProofData proof)
    {
	//check the validness of the message
//	BigInteger[] message = new BigInteger[this.n + this.m];
//	
//	for (int i = 0; i < this.n; i++)
//	    message[i] = proof.aggregateMessage[i];
//	for (int i = n; i < this.n + this.m; i++)
//	    message[i] = BigInteger.ZERO;
//	for (int i = 0; i < challenge.index.length; i++)
//	    message[n + challenge.index[i]] = challenge.coefficients[i];
	
	return rsa.verify(proof.aggregateMessage, proof.aggregateSignature, -1);
    }
    
    /**
     * Generate the proof data on receiving an auditing challenge.
     * @param challenge An object containing the challenge information
     * @return An object containing the proof information
     */
    public ProofData prove(ChallengeData challenge)
    {
	ProofData proof = new ProofData(this.n + this.m, 2);	
	
	BigInteger[][] message = new BigInteger[challenge.index.length][this.n + this.m];
	BigInteger[][] signature = new BigInteger[challenge.index.length][];
	
	for (int i = 0; i < challenge.index.length; i++)
	{
	    for (int j = 0; j < this.n; j++)
		message[i][j] = Utility.byteToBigInteger(buffer[challenge.index[i]][j]);
	    for (int j = this.n; j < this.n + this.m; j++)
		message[i][j] = BigInteger.ZERO;
	    
	    message[i][this.n + challenge.index[i]] = BigInteger.ONE;
	}
	
	for (int i = 0; i < challenge.index.length; i++)
	    signature[i] = this.signature[challenge.index[i]];
		
	proof.aggregateSignature = rsa.combine(message, signature, challenge.coefficients, proof.aggregateMessage);
	
	return proof;
    }
    
    public void print()
    {
	this.rsa.printSecretKey();
    }
    
    public long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(long fileSize)
    {
        this.fileSize = fileSize;
    }
    
    public BigInteger[][] getSignature()
    {
        return signature;
    }

    public void setSignature(BigInteger[][] signature)
    {
        this.signature = signature;
    }

}
