 package fchen;

import java.util.*;
import java.util.Map.Entry;
import java.security.*;
import javax.crypto.*;
import java.io.*;

/**
 * This class implements the prefix-based scheme for verifiable file outsorucing with filename privacy protection. 
 * The basic idea is to separate all possible filenames into two sets: one includes all existing filenames; the other
 * contains all other possible filenames using wildcard representation of strings. Note that all these filenames are contained in one data structure.
 * When the client queires a file, he can send the MACs of all possible filenames to the cloud for search, instead of all clear filenames.
 * 
 * @author Chen, Fei
 * @author Department of Computer Science and Engineering, The Chinese University of Hong Kong.
 * @author Email: chenfeiorange@163.com
 * @author Webpage: https://sites.google.com/site/chenfeiorange/
 * @author Last Updating Date: 2013.04.17
 */

public class PrivatePrefixBasedOutsourcing 
{
	
	private String rootDirectory                 = null;
	private TreeMap<String, String> fileTree     = null;
	/**
	 * SPECIAL_PREVIOUS = "@"; It is a special symbol, denoting that a queried filename is a prefix of some existing file.
	 * SPECIAL_AFTER    = "#"; It is a special symbol, denoting that a queried filename is different from all existing files.
	 *                         Note that "#" lies behind the first place where the queried filename is different.
	 */
	public final static String SPECIAL_PREVIOUS  = "@";
	public final static String SPECIAL_AFTER     = "#";
	
	private byte[] seedOriginalFile              = null;
	private SecureRandom srOriginalFile			 = null;
	private KeyGenerator kgOriginalFile			 = null;
	private SecretKey skOriginalFile			 = null;
	private Mac macOriginalFile 				 = null;
	
	private byte[] seedMACedFile                 = null;
	private SecureRandom srMACedFile 			 = null;
	private KeyGenerator kgMACedFile 			 = null;
	private SecretKey skMACedFile 				 = null;
	private Mac macMACedFile		 			 = null;
	
	private String alphabet 					 = "abcdefghijklmnopqrstuvwxyz0123456789.";	// all possible characters in a filename
	private int flag 							 = 1;	// if flag == 0, MAC = null; else real MAC. This variable is used for test / benchmark purpose.
	
	/**
	 * It constructs an object dealing with all files in a directory.
	 * Initialization work is also done, including setting up the HMAC-SHA256 parameters.  
	 * The key for the MAC algorithm is fixed in the class. You can also change it by modifying the source codes.
	 * Also note that two different keys are used; one for the filename protection, the other for preventing possible cheats from the cloud.
	 * @param rootDirectory The directory for all the files to be outsourced.	
	 */
	public PrivatePrefixBasedOutsourcing(String rootDirectory)
	{
		this.rootDirectory = rootDirectory;
		this.fileTree      = new TreeMap<String, String>();
		
		this.seedOriginalFile          = new byte[32];
		for (int i = 0; i < this.seedOriginalFile.length; i++)
			this.seedOriginalFile[i] = (byte)0xff;
		
		this.seedMACedFile          = new byte[32];
		for (int i = 0; i < this.seedMACedFile.length; i++)
			this.seedMACedFile[i] = (byte)0xee;
		
		// initialize the MAC algorithm
		try 
		{
			this.srOriginalFile  = new SecureRandom(this.seedOriginalFile);
			this.kgOriginalFile  = KeyGenerator.getInstance("HmacSHA256");
			this.kgOriginalFile.init(this.srOriginalFile);
			this.skOriginalFile  = this.kgOriginalFile.generateKey();
			this.macOriginalFile = Mac.getInstance("HmacSHA256");
			this.macOriginalFile.init(skOriginalFile);
			
			this.srMACedFile     = new SecureRandom(this.seedMACedFile);
			this.kgMACedFile     = KeyGenerator.getInstance("HmacSHA256");
			this.kgMACedFile.init(this.srMACedFile);
			this.skMACedFile     = this.kgMACedFile.generateKey();
			this.macMACedFile    = Mac.getInstance("HmacSHA256");
			this.macMACedFile.init(skMACedFile);
		} 
		catch (Exception e)
		{
			System.out.println("Error occured when initializing the cryptographic primitives.\n" + e.toString());
		}
	}
	
	/**
	 * It prints out the information about all possible filenames and their MACs. 
	 */
	public void print() 
	{
		System.out.println("all possible files:\n");
		Iterator<Entry<String, String>> fileAndMAC = this.fileTree.entrySet().iterator();
		while (fileAndMAC.hasNext()) 
		{
			Entry<String, String> entry = fileAndMAC.next();
			String key                  = entry.getKey();
			String macValue             = entry.getValue();
			System.out.println(key + "  -  " + macValue);
		}
	}
	
	/**
	 * It helps the client outsource all possible files to the cloud, including both the filenames and their MACs.
	 * The original filename is MACed; thus, the cloud cannot figure out the real filename.
	 */		
	public void outsource()
	{
		constructPrefix();	 
	}

	/**
	 * It helps the client send a query to the cloud.
	 * @param filename The file to be queried.
	 * @return A string array storing all possible filenames in a MACed way.
	 */
	public String[] query(String filename)
	{
		int queryLength = 2 + filename.length();
		String[] queryFile = new String [queryLength];
		queryFile[0] = filename;
		queryFile[1] = filename + SPECIAL_PREVIOUS;
		for (int i = 0; i < filename.length(); i++)
			queryFile [i + 2] = filename.substring(0, i + 1) + SPECIAL_AFTER;
		
		for (int i = 0; i < queryFile.length; i++)
			//queryFile[i] = queryFile[i] + " + " + generateMACOriginal(queryFile[i]); // for correctness test
		    	queryFile[i] = generateMACOriginal(queryFile[i]);
		return queryFile;
	}

	/**
	 * It helps the cloud search the filenames which are queried by the client.
	 * @param filename The file to be queried.
	 * @return A proof data object containing the verification information.
	 * @see ProofData
	 */
	public ProofData search(String[] filename)
	{
		int result = 0;	// search failed, i.e. it is not in existing file set, nor the prefix set; this should not occur if the implementation and the algorithm is correct.
		String[] mac = new String[filename.length];
		
		for (int i = 0; i < mac.length; i++)
			mac[i] = null;
		
		for (int i = 0; i < filename.length; i++)
		{
			if (this.fileTree.containsKey(filename[i]) == true)  
			{
				result = 1;
				mac[i] = this.fileTree.get(filename[i]);
				break;
			}
		}
		
		return new ProofData(result, filename, mac);
	}
	
	/**
	 * It helps a client verify whether the returned result from the cloud is correct. 
	 * @param filename The queried file.
	 * @param proof    The proof that the cloud returns.
	 * @return An integer indicating whether search is successful and whether the cloud cheats.
	 *        '0' denotes search failure; '1' denotes a verifiable search; '2' denotes the cloud cheats.
	 */
	public int verify(String filename, ProofData proof)
	{
		int result = proof.getResult();
		String[] queryReturned = proof.getFilename();
		String[] mac = proof.getMac();
		
		if (result == 0) // The cloud failed to find a file, which means the algorithm is wrong.
		{
			System.out.println("class.verify - some error occured in cloud's search. It indicates that the implementation of the algorithm is wrong.");
			return 0;
		}			
		
		int index = -1;
		for (int i = 0; i < mac.length; i++)
		{
			if (mac[i] != null)
			{
				index = i;
				break;
			}
		}
		
		if (index == -1)
		{
			System.out.println("class.verify - The cloud search failed. It indicates that the implementation of the algorithm is wrong.");
			return 0;
		}		
	
		result = 2;	// the cloud cheats
		
		int queryLength = 2 + filename.length();
		String[] queryFile = new String [queryLength];
		queryFile[0] = filename;
		queryFile[1] = filename + SPECIAL_PREVIOUS;
		for (int i = 0; i < filename.length(); i++)
			queryFile [i + 2] = filename.substring(0, i + 1) + SPECIAL_AFTER;
		
		for (int i = 0; i < queryFile.length; i++)
		{
			//queryFile[i] = queryFile[i] + " + " + generateMACOriginal(queryFile[i]); // for correctness test
		    	queryFile[i] = generateMACOriginal(queryFile[i]);
			String macTemp = this.generateMACMACed(queryFile[i]);
			if (queryFile[i].equals(queryReturned[index]) && macTemp.equals(mac[index]))
			{	
				result = 1;  // the cloud is honest
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * It generates a random filename.
	 * This function is mainly for performance evaluation purpose.
	 * @return A random existing filename.
	 */
	public String getRandomFile()
	{
		Random r          = new Random((long)0xff);
		
		File directory    = new File(this.rootDirectory);
		String[] allFiles = directory.list();
		
		int index         = Math.abs( r.nextInt() ) % allFiles.length;
		
		return allFiles[index];		
	}
	
	/**
	 * It constructs a tree storing all possible filenames and their MACs.
	 * Note that for correctness proof purpose, we use the statement @ prefixKey = prefixKey + " + " + generateMACOriginal(prefixKey); @,
	 * i.e. adding the original filename as a tip information.
	 * In practice, we should remove this tip information for the privacy reason.
	 * This is simple, jut replace the statement with @ prefixKey = generateMACOriginal(prefixKey); @.
	 */
	private void constructPrefix()
	{
		File directory = new File(this.rootDirectory);
		String[] allFiles = directory.list();
		
		for (String file : allFiles)
		{
			// first, handle the wildcard case. Note that 'prefix' denotes a string not existing in the current files. 
			// e.g. existing files {abc, dd}; the following program tries to find {b#, c#, e#, ...}.
			for (int i = 0; i <= file.length(); i++)      // pay attention to the loop condition
			{
				String prefix = file.substring(0, i);
				
				for (int j = 0; j < this.alphabet.length(); j++)
				{
					String tempPrefix = prefix.substring(0, prefix.length()) + this.alphabet.substring(j, j + 1);
					
					if (isPrefix(tempPrefix, allFiles) == false)
					{
						String prefixKey = tempPrefix + SPECIAL_AFTER;
						//prefixKey      = prefixKey + " + " + generateMACOriginal(prefixKey); // for correctness test
						prefixKey        = generateMACOriginal(prefixKey);
						String macValue  = generateMACMACed(prefixKey);
						this.fileTree.put(prefixKey, macValue);        // add all prefixes into the prefix set using 'TreeMap'
					}		
				}
						
			}
			
			// second, handle the sub-filename case.
			// e.g. existing files {abc, dd}; the following program tries to find {a@, ab@, d@}.
			for (int i = 0; i < file.length() - 1; i++)	//pay attention to the loop condition
			{
				String prefix = file.substring(0, i + 1);
				if (isExistingFile(prefix, allFiles) == false)
				{
					String prefixKey = prefix + SPECIAL_PREVIOUS;
					//prefixKey      = prefixKey + " + " + generateMACOriginal(prefixKey); // for correctness test
					prefixKey        = generateMACOriginal(prefixKey);
					String macValue  = generateMACMACed(prefixKey);
					this.fileTree.put(prefixKey, macValue); 	// add all prefixes into the prefix set using 'TreeMap'
				}				
			}
			
			//String fileKey = file + " + " + generateMACOriginal(file); // for correctness test
			String fileKey = generateMACOriginal(file);
			String fileMac = generateMACMACed(fileKey);
			this.fileTree.put(fileKey, fileMac);        // add all filenames into the existing file set using 'TreeMap'			
		}
	}
	
	/**
	 * It checks whether a filename is a prefix of some existing file in a given file set.
	 * @param filename The filename to be checked.
	 * @param array The file set.
	 * @return If yes, it returns true; otherwise, false.
	 */
	private boolean isPrefix(String filename, String[] array)
	{
		for (String temp : array)
		{
			if (temp.startsWith(filename) == true)
				return true;
		}
		return false;
	}
	
	/**
	 * It check whether a file name exists in a given file set.
	 * @param filename The filename to be checked.
	 * @param array The file set.
	 * @return If yes, it returns true; otherwise, false.
	 */
	private boolean isExistingFile(String filename, String[] array)
	{
		for (String temp : array)
		{
			if (temp.equals(filename) == true)
				return true;
		}
		return false;
	}
	
	/**
	 * It generates the corresponding MAC for a given filename.
	 * @param filename The input filename to be MACed.
	 * @return The corresponding MAC of the filename.
	 */
	private String generateMACOriginal(String filename)
	{
		if (this.flag == 1)		
			return bytes2HexString( this.macOriginalFile.doFinal(filename.getBytes()) );
		else
			return null;
	}
	
	/**
	 * It generates the corresponding MAC for a MACed filename.
	 * @param filename The input MACed filename to be MACed again.
	 * @return The corresponding MAC of the MACed filename.
	 */
	private String generateMACMACed(String filename)
	{
		if (this.flag == 1)		
			return bytes2HexString( this.macMACedFile.doFinal(filename.getBytes()) );
		else
			return null;
	}
	
	private final static byte[] hex = "0123456789ABCDEF".getBytes();
	/**
	 * It transforms a byte array into a string in the Hexadecimal format in an entry-wise way.
	 * @param b The byte array.
	 * @return The hexadecimal string.
	 */
	public static String bytes2HexString(byte[] b) 
	{
		byte[] buff = new byte[2 * b.length];
		for (int i = 0; i < b.length; i++) {
			buff[2 * i]     = hex[(b[i] >> 4) & 0x0f];
			buff[2 * i + 1] = hex[b[i] & 0x0f];
		}
		return new String(buff);
	}

	public TreeMap<String, String> getFileTree()
	{
	    return fileTree;
	}

	public void setFileTree(TreeMap<String, String> fileTree)
	{
	    this.fileTree = fileTree;
	}	
}
