package fchen;

import java.util.*;
import java.util.Map.Entry;
import java.security.*;
import javax.crypto.*;
import java.io.*;

/**
 * This class implements the prefix-based scheme for verifiable file outsorucing. 
 * The basic idea is to separate all possible filenames into two sets: one includes all existing filenames; the other
 * contains all other possible filenames using wildcard representation of strings.
 * 
 * @author Chen, Fei
 * @author Department of Computer Science and Engineering, The Chinese University of Hong Kong.
 * @author Email: chenfeiorange@163.com
 * @author Webpage: https://sites.google.com/site/chenfeiorange/
 * @author Final Modifying Date: 2013.04.17
 */

public class PrefixBasedOutsourcing 
{	
    	private String rootDirectory                 = null;
	private TreeMap<String, byte[]> existingFile = null;
	private TreeMap<String, byte[]> prefix       = null;
	/**
	 * SPECIAL_PREVIOUS = "@"; It is a special symbol, denoting that a queried filename is a prefix of some existing file.
	 * SPECIAL_AFTER    = "#"; It is a special symbol, denoting that a queried filename is different from all existing files.
	 *                         Note that "#" lies behind the first place where the queried filename is different.
	 */
	public final static String SPECIAL_PREVIOUS  = "@";
	public final static String SPECIAL_AFTER     = "#";
	
	private byte[] seed                          = null;
	private SecureRandom sr 					 = null;
	private KeyGenerator kg 					 = null;
	private SecretKey sk 						 = null;
	private Mac mac 							 = null;
	
	private String alphabet 					 = "abcdefghijklmnopqrstuvwxyz0123456789.";	// all possible characters in a filename
	private int flag 							 = 1;	// if flag == 0, MAC = null; else real MAC. This variable is used for test / benchmark purpose.
	
	/**
	 * It constructs an object dealing with all files in a directory.
	 * Initialization work is also done, including setting up the HMAC-SHA256 parameters.  
	 * The key for the MAC algorithm is fixed in the class. You can also change it by modifying the source codes.
	 * @param rootDirectory The directory for all the files to be outsourced.	
	 */
	public PrefixBasedOutsourcing(String rootDirectory)
	{
		this.rootDirectory = rootDirectory;
		this.existingFile  = new TreeMap<String, byte[]>();
		this.prefix        = new TreeMap<String, byte[]>();
		
		this.seed          = new byte[32];
		for (int i = 0; i < this.seed.length; i++)
			this.seed[i] = (byte)0xff;
		
		
		// initialize the MAC algorithm
		try 
		{
			this.sr  = new SecureRandom(this.seed);
			this.kg  = KeyGenerator.getInstance("HmacSHA256");
			this.kg.init(this.sr);
			this.sk  = kg.generateKey();
			this.mac = Mac.getInstance("HmacSHA256");
			this.mac.init(sk);
		} 
		catch (Exception e)
		{
			System.out.println("Error occured when initializing the cryptographic primitives.");
		}
	}
	
	/**
	 * It prints out the information about the existing file set, the prefix set and their MACs. 
	 */
	public void print() 
	{
		System.out.println("existing files:\n");
		Iterator<Entry<String, byte[]>> existing = this.existingFile.entrySet().iterator();
		while (existing.hasNext()) 
		{
			Entry<String, byte[]> entry = existing.next();
			String key                  = entry.getKey();
			byte[] macValue             = entry.getValue();
			System.out.println(key + "  -  " + bytes2HexString(macValue));
		}
		
		System.out.println("\n\nprefix files:\n");
		Iterator<Entry<String, byte[]>> prefix = this.prefix.entrySet().iterator();
		while (prefix.hasNext()) 
		{
			Entry<String, byte[]> entry = prefix.next();
			String key                  = entry.getKey();
			byte[] macValue             = entry.getValue();
			System.out.println(key + "  -  " + bytes2HexString(macValue));
		}
	}
	
	/**
	 * It helps the client outsource all the possible files to the cloud, including both the files and their MACs.
	 */
	public void outsource()
	{
		constructPrefix();	 
	}

	/**
	 * It helps the client send a query to the cloud.
	 * @param filename The file to be queried.
	 * @return A token that is sent to the cloud.
	 *         The token depends on the protocol. It could be the original filename;
	 *         it also could be an encrypted version of the original filename.
	 */
	public String query(String filename)
	{
		return filename;
	}

	/**
	 * It helps the cloud search a filename which is queried by the client.
	 * @param filename The file to be queried.
	 * @return A proof data object which has three variables: an integer indicator, a string and a byte array. The second is the query filename and the third is the MAC of the filename.
	 *         The first means as follows:
	 *         '0' denotes failure.
	 *         '1' denotes the query file exists in the current file set;
	 *         '2' denotes the query file is a prefix of some existing file.
	 *         '3' denotes the query file is different from all existing files.
	 */
	public ProofData search(String filename)
	{
		int result = 0;	// search failed, i.e. it is not in existing file set, nor the prefix set; this should not occur if the implementation and the algorithm is correct.
		byte[] mac  = null;
		if (this.existingFile.containsKey(filename) == true)  // the file does exist.
		{
			result = 1;
			mac  = this.existingFile.get(filename);
		}
		else
		{
			String prefixBefore = filename + SPECIAL_PREVIOUS;
			if (this.prefix.containsKey(prefixBefore) == true) // the file is a prefix of some existing file.
			{
				result = 2;
				mac  = this.prefix.get(prefixBefore);
				filename = prefixBefore;
			}
			else				
				for (int i = 0; i < filename.length(); i++)
				{
					String prefixAfter  = filename.substring(0, i + 1) + SPECIAL_AFTER;
					
					if (this.prefix.containsKey(prefixAfter) == true) // the file is different from all existing files.
					{
						result = 3;
						mac  = this.prefix.get(prefixAfter);
						filename = prefixAfter;
						break;
					}				
				}
			
		}
		
		return new ProofData(result, filename, mac);
	}
	
	/**
	 * It helps a client verify whether the returned result from the cloud is correct. 
	 * @param filename The queried file.
	 * @param proof    The proof object that the cloud returns.
	 * @return An integer indicating whether search is successful and whether the cloud cheats.
	 *        '0' denotes search failure; '1' denotes a verifiable search; '2' denotes the cloud cheats.
	 */
	public int verify(String filename, ProofData proof)
	{
		int result = proof.getResult();
		byte[] mac = proof.getMac();
		
		if (result == 0) // The cloud failed to find a file, which means the algorithm is wrong.
		{
			System.out.println("some error occured in cloud's search.");
			return 0;
		}			
		else if (result == 1)  // the query file exists.
		{
			byte[] expected = generateMAC(filename);
			if (Arrays.equals(expected, mac) == true && filename.equals(proof.getFilename()))
				return 1;  // The cloud is honest.
			else
				return 2;  // The cloud cheats.
		}
		else if (result == 2)  // the query file is a prefix of some existing file.
		{
			filename = filename + SPECIAL_PREVIOUS;
			byte[] expected = generateMAC(filename);
			if (Arrays.equals(expected, mac) == true && filename.equals(proof.getFilename()))
				return 1;  // The cloud is honest.
			else
				return 2;  // The cloud cheats.
		}
		else // the query file different from all existing files.
		{
			String temp = proof.getFilename();
			temp = temp.substring(0, temp.length() - 1);
			byte[] expected = generateMAC(proof.getFilename());
			if (Arrays.equals(expected, mac) == true && filename.startsWith(temp))
				return 1;  // The cloud is honest.
			else
				return 2;  // The cloud cheats.
		}
	}
	
	/**
	 * It generates a random existing filename.
	 * This function is mainly for performance evaluation purpose.
	 * @return A random existing filename.
	 */
	public String getRandomExistingFile()
	{
		Random r  = new Random((long)0xff);
		int index = Math.abs(r.nextInt()) % this.existingFile.size();
		int temp  = 0;
		
		Iterator<Entry<String, byte[]>> existing = this.existingFile.entrySet().iterator();
		Entry<String, byte[]> entry = null;
		while (existing.hasNext() && temp <= index) 
			 entry = existing.next();
		return entry.getKey();		
	}
	
	/**
	 * It generates a random non-existing filename.
	 * This function is mainly for performance evaluation purpose.
	 * @return A random non-existing filename.
	 */
	public String getRandomNonExistingFile()
	{
		Random r  = new Random((long)0xff);
		int index = Math.abs(r.nextInt()) % this.prefix.size();
		int temp  = 0;
		
		Iterator<Entry<String, byte[]>> existing = this.prefix.entrySet().iterator();
		Entry<String, byte[]> entry = null;
		while (existing.hasNext() && temp <= index) 
			 entry = existing.next();
		String filename = entry.getKey();
		return filename.substring(0, filename.length() - 1);		
	}
	
	/**
	 * It constructs two sets denoting all possible files. One includes all existing files in the given directory.
	 * The other contains all other possible filenames using the wildcard representation of a string.
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
						byte[] macValue = generateMAC(prefixKey);
						this.prefix.put(prefixKey, macValue);        // add all prefixes into the prefix set using 'TreeMap'
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
					byte[] macValue = generateMAC(prefixKey);
					this.prefix.put(prefixKey, macValue);	// add all prefixes into the prefix set using 'TreeMap'
				}				
			}
			
			byte[] macValue = generateMAC(file);
			this.existingFile.put(file, macValue);        // add all filenames into the existing file set using 'TreeMap'			
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
	private byte[] generateMAC(String filename)
	{
		if (this.flag == 1)		
			return this.mac.doFinal(filename.getBytes());
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
	    for (int i = 0; i < b.length; i++)
	    {
		buff[2 * i] = hex[(b[i] >> 4) & 0x0f];
		buff[2 * i + 1] = hex[b[i] & 0x0f];
	    }
	    return new String(buff);
	}

	public TreeMap<String, byte[]> getExistingFile()
	{
	    return existingFile;
	}

	public void setExistingFile(TreeMap<String, byte[]> existingFile)
	{
	    this.existingFile = existingFile;
	}

	public TreeMap<String, byte[]> getPrefix()
	{
	    return prefix;
	}

	public void setPrefix(TreeMap<String, byte[]> prefix)
	{
	    this.prefix = prefix;
	}
}
