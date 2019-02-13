package fchen;

import com.javamex.classmexer.*;

/**
 * This class measure the performance of our sting equality based cloud storage
 * integrity checking protocol. The metric includes storage, computation and
 * communication cost.
 * 
 * @author Chen, Fei (https://sites.google.com/site/chenfeiorange/)
 * @author Last updated on 28-11-2013. License: GNU GPL
 * @author Email: chenfeiorange@163.com
 * 
 */
public class Benchmark
{

    private long storage = 0;
    private long communication = 0;
    private long time[]; // 0: key generation; 1: outsource; 2: audit; 3: prove;
			 // 4: verify.
    private long fileSize = 0;

    private String file;
    public final static int LOOP_TIMES = 2; // we run the performance
					     // evaluation for such times and
					     // then average the result.

    public Benchmark(String file)
    {
	super();
	this.file = file;
	this.time = new long[5];
    }

    /**
     * This is the main function to evaluate the performance.
     */
    public void run()
    {
	deterministicAudit();	
	randomizedAudit();
    }

    private void randomizedAudit()
    {
	InnerProductBasedVS vs = new InnerProductBasedVS(file);
	this.fileSize = vs.getFileSize();

	this.storage = 0;
	this.communication = 0;
	for (int i = 0; i < this.time.length; i++)
	    this.time[i] = 0;

	long startTime = 0, endTime = 0;

	startTime = System.nanoTime();
	vs.generateKey();
	endTime = System.nanoTime();
	this.time[0] = endTime - startTime;

	startTime = System.nanoTime();
	vs.outsource();
	endTime = System.nanoTime();
	this.time[1] = endTime - startTime;

	this.storage = MemoryUtil.deepMemoryUsageOf(vs.getShadow());

	Boolean b;
	int count = 0, challengeLen = 10;

	for (int i = 0; i < LOOP_TIMES; i++)
	{
	    ChallengeData c;
	    startTime = System.nanoTime();
	    c = vs.audit(challengeLen);
	    endTime = System.nanoTime();
	    this.time[2] = this.time[2] + (endTime - startTime);

	    ProofData proof;
	    startTime = System.nanoTime();
	    proof = vs.prove(c);
	    endTime = System.nanoTime();
	    this.time[3] = this.time[3] + (endTime - startTime);

	    this.communication = this.communication + MemoryUtil.deepMemoryUsageOf(proof);

	    startTime = System.nanoTime();
	    b = vs.verify(c, proof);
	    endTime = System.nanoTime();
	    this.time[4] = this.time[4] + (endTime - startTime);

	    if (b == false)
		count++;

	    System.out.println("system is running in loop " + i);
	}

	this.time[2] = (long) (this.time[2] / LOOP_TIMES);
	this.time[3] = (long) (this.time[3] / LOOP_TIMES);
	this.time[4] = (long) (this.time[4] / LOOP_TIMES);

	this.communication = (long) (this.communication / LOOP_TIMES);

	System.out.println("\nRandomized Auditting");
	System.out.println("TEST CASE: " + this.file);
	System.out.println("file size is: " + this.fileSize + "Bytes");
	System.out.println("storage cost is: " + this.storage + "Bytes");
	System.out.println("communication size is: " + this.communication + "Bytes");
	System.out.println("time is: (ns)");
	for (int i = 0; i < this.time.length; i++)
	    System.out.print(this.time[i] + "    ");
	System.out.println("\ncorrespoding to keygen(0), outsource (1), audit(2), prove(3), verify(4)");

	System.out.println("verification error: " + count);
    }

    private void deterministicAudit()
    {
	InnerProductBasedVS vs = new InnerProductBasedVS(file);
	this.fileSize = vs.getFileSize();

	this.storage = 0;
	this.communication = 0;
	for (int i = 0; i < this.time.length; i++)
	    this.time[i] = 0;

	long startTime = 0, endTime = 0;

	startTime = System.nanoTime();
	vs.generateKey();
	endTime = System.nanoTime();
	this.time[0] = endTime - startTime;

	startTime = System.nanoTime();
	vs.outsource();
	endTime = System.nanoTime();
	this.time[1] = endTime - startTime;

	this.storage = MemoryUtil.deepMemoryUsageOf(vs.getShadow());

	String c;
	startTime = System.nanoTime();
	c = vs.auditDeterministic();
	endTime = System.nanoTime();
	this.time[2] = this.time[2] + (endTime - startTime);

	ProofData proof;
	startTime = System.nanoTime();
	proof = vs.proveDeterministic(c);
	endTime = System.nanoTime();
	this.time[3] = this.time[3] + (endTime - startTime);

	this.communication = this.communication + MemoryUtil.deepMemoryUsageOf(proof);

	startTime = System.nanoTime();
	Boolean b = vs.verifyDeterministic(c, proof);
	endTime = System.nanoTime();
	this.time[4] = this.time[4] + (endTime - startTime);

	if (b == false)
	    System.out.println("verification error.");
	
	System.out.println("\nDeterministic Auditting");
	System.out.println("TEST CASE: " + this.file);
	System.out.println("file size is: " + this.fileSize + "Bytes");
	System.out.println("storage cost is: " + this.storage + "Bytes");
	System.out.println("communication size is: " + this.communication + "Bytes");
	System.out.println("time is: (ns)");
	for (int i = 0; i < this.time.length; i++)
	    System.out.print(this.time[i] + "    ");
	System.out.println("\ncorrespoding to keygen(0), outsource (1), audit(2), prove(3), verify(4)");

	
    }
}
