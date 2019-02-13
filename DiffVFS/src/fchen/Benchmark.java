package fchen;

import java.io.File;

import com.javamex.classmexer.*;

/**
 * This class measure the performance of our verifiable outsourcing algorithm on a certain directory.
 * The performance metric includes the storage for storing the file set and the prefix set, the outsource time, the query time, the search time, and the verification time.
 * @author Chen, Fei
 * @author Last Updating Date: 2015.08.03
 */
public class Benchmark 
{
	private long storage = 0; //storage for the file set and the prefix set
	private long time[];  //time for outsource, query, search, verify, indexed by 0, 1, 2, 3, respectively
	private String directory;
	
	public final static int LOOP_TIMES = 40;    // we run the performance evaluation for such times and then average the result.
	
	public Benchmark(String directory) {
		super();
		this.directory = directory;
		this.storage   = 0;
		this.time      = new long[4];
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
	    	File tempDirectory = new File(this.directory);
		String[] allFiles = tempDirectory.list();
		int securityLevels =  allFiles.length / 5 + 1;
		
		DiffVFS instance = new DiffVFS(this.directory, securityLevels);
		
		long startTime = 0, endTime = 0;
		
		//Runtime r      = Runtime.getRuntime();		
		//r.gc();
		//startMemory    = r.freeMemory();
		startTime      = System.nanoTime();
		instance.outsource();
		endTime        = System.nanoTime(); 
		//endMemory      = r.freeMemory();
		
		//instance.print();
		
		this.storage   = MemoryUtil.deepMemoryUsageOf(instance.getFileTree());
		this.time[0]   = endTime - startTime;
		
		for (int i = 0; i < LOOP_TIMES; i++)
		{
			String randomFile       = instance.getRandomFile();		
			ProofData proof         = null;  
			
			//extracting the query file
			int temp = randomFile.indexOf('-');
			int securityLevel = Integer.valueOf(randomFile.substring(temp + 1));
			randomFile = randomFile.substring(0, temp);
			
			startTime        = System.nanoTime();			
			String[] query   = instance.query(randomFile, securityLevel);			
			endTime          = System.nanoTime(); 
			this.time[1]     = this.time[1] + (endTime - startTime);
			
			startTime        = System.nanoTime();			
			proof            = instance.search(query);
			endTime          = System.nanoTime(); 
			this.time[2]     = this.time[2] + (endTime - startTime);
			
			startTime        = System.nanoTime();			
			instance.verify(randomFile, securityLevel, proof);
			endTime          = System.nanoTime(); 
			this.time[3]     = this.time[3] + (endTime - startTime);			
		}
		
		this.time[1] = (long) (this.time[1] / LOOP_TIMES);
		this.time[2] = (long) (this.time[2] / LOOP_TIMES);
		this.time[3] = (long) (this.time[3] / LOOP_TIMES);
		
		System.out.println("TEST CASE: " + this.directory + "\n" + "total security levels: " + securityLevels + "\n");
		System.out.println("storage is: " + this.storage + "Bytes");
		System.out.println("time is: (ns)");
		for (int i = 0; i < this.time.length; i++)
			System.out.print(this.time[i] + "    ");
		System.out.println("\ncorrespoding to outsource, query, search, verify");
		
	}
}
