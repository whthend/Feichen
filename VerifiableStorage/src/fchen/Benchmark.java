package fchen;

import com.javamex.classmexer.*;

/**
 * This class measure the performance of our verifiable cloud storage protocol.
 * The metric includes storage, computation and communication cost.
 * 
 * @author Chen, Fei (https://sites.google.com/site/chenfeiorange/)
 * @author First draft on 18-06-2013. License: GNU GPL
 * @author Email: chenfeiorange@163.com
 * 
 */
public class Benchmark
{
    private long storage       = 0;
    private long communication = 0;
    private long time[]; // 0: key generation; 1: outsource; 2: audit; 3: prove; 4: verify.
    private long fileSize = 0;
    
    private String file;
    public final static int LOOP_TIMES = 2; // we run the performance
					     // evaluation for such times and
					     // then average the result.

    public Benchmark(String file)
    {
	super();
	this.file    = file;
	this.storage = 0;
	this.time    = new long[5];
	
	for (int i = 0; i < this.time.length; i++)
	    this.time[i] = 0;
	this.communication = 0;
    }

    /**
     * This is the main function to evaluate the performance.
     */
    public void run()
    {
	VerifiableStorage vs = new VerifiableStorage(file);
	this.fileSize = vs.getFileSize();

	long startTime = 0, endTime = 0;

	startTime = System.nanoTime();
	vs.generateKey();
	endTime = System.nanoTime();
	this.time[0] = endTime - startTime;

	startTime = System.nanoTime();
	vs.outsource();
	endTime = System.nanoTime();
	this.time[1] = endTime - startTime;

	this.storage = MemoryUtil.deepMemoryUsageOf(vs.getSignature());

	Boolean b;
	int count = 0;
	
	for (int i = 0; i < LOOP_TIMES; i++)
	{
	    ChallengeData c;
	    startTime = System.nanoTime();
	    c = vs.audit();
	    endTime = System.nanoTime();
	    this.time[2] = this.time[2] + (endTime - startTime);

	    ProofData proof;
	    startTime = System.nanoTime();
	    proof = vs.prove(c);
	    endTime = System.nanoTime();
	    this.time[3] = this.time[3] + (endTime - startTime);
	    
	    this.communication = this.communication + MemoryUtil.deepMemoryUsageOf(proof.aggregateSignature);
	    
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

	System.out.println("TEST CASE: " + this.file + "\n");
	System.out.println("file size is: " + this.fileSize + "Bytes");
	System.out.println("signature size is: " + this.storage + "Bytes");
	System.out.println("communication size is: " + this.communication + "Bytes");
	System.out.println("time is: (ns)");
	for (int i = 0; i < this.time.length; i++)
	    System.out.print(this.time[i] + "    ");
	System.out.println("\ncorrespoding to keygen(0), outsource (1), audit(2), prove(3), verify(4)");

	System.out.println("verification error: " + count);
    }
}
