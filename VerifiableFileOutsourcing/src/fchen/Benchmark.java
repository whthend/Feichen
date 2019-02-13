package fchen;

import com.javamex.classmexer.*;

/**
 * This class measure the performance of our verifiable outsourcing algorithm on a certain directory.
 * The performance metric includes the storage for storing the file set and the prefix set, the outsource time, the query time, the search time, and the verification time.
 * We divide the search and verification time into two cases: one for existing files and the other for non-existing files. 
 * @author Chen, Fei
 * @author Final Modifying Date: 2013.04.17
 */
public class Benchmark 
{
	private long storage = 0; //storage for the file set and the prefix set
	private long time[];  //time for outsource, query, search existing, search nonexisting, verify existing, verify nonexisting, indexed by 0, 1, 2, 3, 4, 5, respectively
	private String directory;
	
	public final static int LOOP_TIMES = 40;    // we run the performance evaluation for such times and then average the result.
	
	public Benchmark(String directory) {
		super();
		this.directory = directory;
		this.storage   = 0;
		this.time      = new long[6];
		for (int i = 0; i < this.time.length; i++)
			this.time[i] = 0;
	}
	
	public long getStorage() {
		return storage;
	}

	public void setStorage(long storage) {
		this.storage = storage;
	}

	public long[] getTime() {
		return time;
	}

	public void setTime(long[] time) {
		this.time = time;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}
	
	/**
	 * This is the main function to evaluate the performance.	
	 */
	public void run()
	{
		PrefixBasedOutsourcing instance = new PrefixBasedOutsourcing(this.directory);
		
		long startTime = 0, endTime = 0;
		
		//Runtime r      = Runtime.getRuntime();		
		//r.gc();
		//startMemory    = r.freeMemory();  // such mesurement is not acurate; instead, we use the "MemoryUtil" class to measure memory cost. 
		startTime      = System.nanoTime();		
		instance.outsource();		
		endTime        = System.nanoTime(); 
		//endMemory      = r.freeMemory();
		
		//instance.print();
		
		this.storage   =  MemoryUtil.deepMemoryUsageOf(instance.getExistingFile()) +  MemoryUtil.deepMemoryUsageOf(instance.getPrefix());
		this.time[0]   = endTime - startTime;
		
		for (int i = 0; i < LOOP_TIMES; i++)
		{
			String queryExisisting  = instance.getRandomExistingFile();		
			ProofData proof         = null;  
			
			startTime        = System.nanoTime();			
			queryExisisting  = instance.query(queryExisisting);			
			endTime          = System.nanoTime(); 
			this.time[1]     = this.time[1] + (endTime - startTime);
			
			startTime        = System.nanoTime();			
			proof            = instance.search(queryExisisting);
			endTime          = System.nanoTime(); 
			this.time[2]     = this.time[2] + (endTime - startTime);
			
			startTime        = System.nanoTime();			
			instance.verify(queryExisisting, proof);
			endTime          = System.nanoTime(); 
			this.time[4]     = this.time[4] + (endTime - startTime);			
		}
		
		this.time[1] = (long) (this.time[1] / LOOP_TIMES);
		this.time[2] = (long) (this.time[2] / LOOP_TIMES);
		this.time[4] = (long) (this.time[4] / LOOP_TIMES);
		
		for (int i = 0; i < LOOP_TIMES; i++)
		{
			String queryNonExisisting  = instance.getRandomNonExistingFile();		
			ProofData proof            = null;
			queryNonExisisting         = instance.query(queryNonExisisting);			
			
			startTime        = System.nanoTime();			
			proof            = instance.search(queryNonExisisting);
			endTime          = System.nanoTime(); 
			this.time[3]     = this.time[3] + (endTime - startTime);
			
			startTime        = System.nanoTime();			
			instance.verify(queryNonExisisting, proof);
			endTime          = System.nanoTime(); 
			this.time[5]     = this.time[5] + (endTime - startTime);			
		}
		
		this.time[3] = (long) (this.time[3] / LOOP_TIMES);
		this.time[5] = (long) (this.time[5] / LOOP_TIMES);		
		
		System.out.println("TEST CASE: " + this.directory + "\n");
		System.out.println("storage is: " + this.storage + "Bytes");
		System.out.println("time is: (ns)");
		System.out.print(this.time[0] + "    ");
		System.out.print(this.time[1] + "    ");
		System.out.print((this.time[2] + this.time[3]) / 2 + "    ");
		System.out.print((this.time[4] + this.time[5]) / 2);
		
		System.out.println("\ncorrespoding to outsource, query, search, verify");
		
	}
}
