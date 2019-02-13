package fchen;

import java.util.*;
import java.util.Map.Entry;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;

/**
 * This class implements the DiffVFS protocol proposed in our paper which can enable
 * both verifiable file search and user access control.
 * 
 * For file search verifiability,the basic idea is to separate all possible
 * filenames into two sets: one includes all existing filenames; the other
 * contains all other possible filenames using wildcard representation of
 * strings. Note that all these filenames are contained in one data structure.
 * When the client queries a file, he can send the MACs of all possible
 * filenames to the cloud for search, instead of all clear filenames.
 * 
 * For access control, the basic is to first assign different keys for different security
 * levels, and then embed different keys into our proposed protocol in a tight-coupled fashion. 
 * 
 * @author Chen, Fei
 * @author Department of Computer Science and Engineering, Shenzhen University, China.
 * @author Email: chenfeiorange@163.com
 * @author Webpage: https://sites.google.com/site/chenfeiorange/
 * @author Last Update Date: 2015.08.03
 */

public class DiffVFS
{

    private String rootDirectory = null;
    private TreeMap<String, String> fileTree = null;

    private int securityLevels = 1;
    private byte[][] key = null;

    /**
     * SPECIAL_PREVIOUS = "@"; It is a special symbol, denoting that a queried
     * filename is a prefix of some existing file. SPECIAL_AFTER = "#"; It is a
     * special symbol, denoting that a queried filename is different from all
     * existing files. Note that "#" lies behind the first place where the
     * queried filename is different.
     */
    public final static String SPECIAL_PREVIOUS = "@";
    public final static String SPECIAL_AFTER = "#";

    private String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789.";
    // all possible characters in a filename

    private Mac mac;

    /**
     * It constructs an object dealing with all files in a directory.
     * Specifically, it constructs a tree storing all possible filenames and their MACs.
     * 
     * @param rootDirectory
     *            The directory for all the files to be outsourced.
     */
    public DiffVFS(String rootDirectory, int securityLevels)
    {
	this.rootDirectory = rootDirectory;
	this.securityLevels = securityLevels;
	try
	{
	    this.mac = Mac.getInstance("HmacSHA256");
	} catch (Exception e)
	{
	    System.out.println("Error in DiffVFS construction function: cannot generate MAC instance." + e);
	}
	this.keyGen();

	this.fileTree = new TreeMap<String, String>();

    }

    /**
     * It prints out the information about all possible filenames and their
     * MACs.
     */
    public void print()
    {
	System.out.println("all possible files:\n");
	Iterator<Entry<String, String>> fileAndMAC = this.fileTree.entrySet().iterator();
	while (fileAndMAC.hasNext())
	{
	    Entry<String, String> entry = fileAndMAC.next();
	    String key = entry.getKey();
	    String macValue = entry.getValue();
	    System.out.println(key + "  -  " + macValue);
	}
    }

    /*
     * This function generates secret keys used in our protocol.
     * this.key[0] is not used.
     * this.key[1 : securityLevels] are used to  mask the original filename to get a nickname, and to generate authentication information for access control.
     */
    public void keyGen()
    {
	this.key = new byte[this.securityLevels + 1][32];

	try
	{
	    this.mac.init(new SecretKeySpec("filename mac".getBytes(), "HmacSHA256"));
	    key[0] = this.mac.doFinal("123".getBytes());

	    this.mac.init(new SecretKeySpec("masterkey".getBytes(), "HmacSHA256"));
	    key[1] = this.mac.doFinal("123".getBytes());

	    for (int i = 2; i < this.securityLevels + 1; i++)
	    {
		this.mac.init(new SecretKeySpec(key[i - 1], "HmacSHA256"));
		key[i] = this.mac.doFinal("123".getBytes());

	    }
	} catch (Exception e)
	{
	    System.out.println("error in keyGen: " + e);
	}
    }

    /**
     * It helps the client to outsource all possible files to the cloud, including
     * both the filenames and their MACs. The original filename is MACed; thus,
     * the cloud cannot figure out the real filename.
     * 
     * Since we divide data into multiple security levels, we construct the outsourced
     * data in multiple steps, with each step handling a security level.
     * Security level starts from the index 1 and grows upwards.
     */
    public void outsource()
    {
	File directory = new File(this.rootDirectory);
	String[] allFiles = directory.list();
	String levelledFiles[][] = new String[this.securityLevels][];
	
	int blockNumber = 0;
	if (allFiles.length % this.securityLevels == 0)
	    blockNumber = allFiles.length / this.securityLevels;
	else
	    blockNumber = (int)(allFiles.length / this.securityLevels) + 1;
	
	for (int i = 1; i < this.securityLevels + 1; i++)
	{
	    int from = (i - 1) * blockNumber;
	    int to = Math.min(i * blockNumber, allFiles.length);
	    levelledFiles[i - 1] = Arrays.copyOfRange(allFiles, from, to);	    
	    
	    constructTree(levelledFiles[i - 1], i);
	}
    }

    /*
     * It separates all possible filenames at a fiven security level into two sets.
     * Details about the algorithm can be found in our paper.
     */
    private void constructTree(String[] allFiles, int securityLevel)
    {
	for (int counter = 0; counter < allFiles.length; counter++)
	{ 
	    //identify the security level of each file for evaluation purpose. In practice, this should be replaced by a security policy.
	    
	    String file = allFiles[counter];
	    
	    // first, handle the wildcard case. Note that 'prefix' denotes a  string not existing in the current files.
	    // e.g. existing files {abc, dd}; the following program tries to find {b#, c#, e#, ...}.
	    for (int i = 0; i <= file.length(); i++) // pay attention to the loop condition
	    {
		String prefix = file.substring(0, i);
		for (int j = 0; j < this.alphabet.length(); j++)
		{
		    String tempPrefix = prefix.substring(0, prefix.length()) + this.alphabet.substring(j, j + 1);

		    if (isPrefix(tempPrefix, allFiles) == false)
		    {
			String prefixKey = tempPrefix + SPECIAL_AFTER;
			// prefixKey = prefixKey + " + " + enerateMACOriginal(prefixKey); for correctness test
			prefixKey = generateMAC(prefixKey, this.key[securityLevel]);
			String macValue = generateMAC(prefixKey, this.key[securityLevel]);
			this.fileTree.put(prefixKey, macValue); // add all prefixes into the prefix set using 'TreeMap'
		    }
		}

	    }

	    // second, handle the sub-filename case. e.g. existing files {abc, dd}; the following program tries to find {a@, ab@, d@}.
	    for (int i = 0; i < file.length() - 1; i++) // pay attention to the loop condition
	    {
		String prefix = file.substring(0, i + 1);
		if (isExistingFile(prefix, allFiles) == false)
		{
		    String prefixKey = prefix + SPECIAL_PREVIOUS;
		    // prefixKey = prefixKey + " + " + generateMACOriginal(prefixKey); for correctness test
		    prefixKey = generateMAC(prefixKey, this.key[securityLevel]);
		    String macValue = generateMAC(prefixKey, this.key[securityLevel]);
		    this.fileTree.put(prefixKey, macValue); // add all prefixes into the prefix set using 'TreeMap'
		}
	    }

	    // String fileKey = file + " + " + generateMACOriginal(file); for correctness test
	    String fileKey = generateMAC(file, this.key[securityLevel]);
	    String fileMac = generateMAC(fileKey, this.key[securityLevel]);
	    this.fileTree.put(fileKey, fileMac); // add all filenames into the existing file set using 'TreeMap'
	}
    }

    /**
     * It helps the client send a query to the cloud.
     * 
     * @param filename
     *            The file to be queried.
     * @return A string array storing all possible filenames in a MACed way.
     */
    public String[] query(String filename, int securityLevel)
    {
	int queryLength = 2 + filename.length();
	String[] queryFile = new String[queryLength];
	queryFile[0] = filename;
	queryFile[1] = filename + SPECIAL_PREVIOUS;
	for (int i = 0; i < filename.length(); i++)
	    queryFile[i + 2] = filename.substring(0, i + 1) + SPECIAL_AFTER;

	for (int i = 0; i < queryFile.length; i++)
	    // queryFile[i] = queryFile[i] + " + " + generateMACOriginal(queryFile[i]); for correctness test
	    queryFile[i] = generateMAC(queryFile[i], this.key[securityLevel]);
	return queryFile;
    }

    /**
     * It helps the cloud search the filenames which are queried by the client.
     * 
     * @param filename
     *            The file to be queried.
     * @return A proof data object containing the verification information.
     * @see ProofData
     */
    public ProofData search(String[] filename)
    {
	int result = 0; // search failed, i.e. it is not in existing file set,
	// nor the prefix set; this should not occur if the
	// implementation and the algorithm is correct.
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
     * It helps a client verify whether the returned result from the cloud is
     * correct.
     * 
     * @param filename
     *            The queried file.
     * @param proof
     *            The proof that the cloud returns.
     * @return An integer indicating whether search is successful and whether
     *         the cloud cheats. '0' denotes search failure; '1' denotes a
     *         verifiable search; '2' denotes the cloud cheats.
     */
    public int verify(String filename, int securityLevel, ProofData proof)
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

	result = 2; // the cloud cheats

	int queryLength = 2 + filename.length();
	String[] queryFile = new String[queryLength];
	queryFile[0] = filename;
	queryFile[1] = filename + SPECIAL_PREVIOUS;
	for (int i = 0; i < filename.length(); i++)
	    queryFile[i + 2] = filename.substring(0, i + 1) + SPECIAL_AFTER;

	for (int i = 0; i < queryFile.length; i++)
	{
	    // queryFile[i] = queryFile[i] + " + " +
	    // generateMACOriginal(queryFile[i]); // for correctness test
	    queryFile[i] = generateMAC(queryFile[i], this.key[securityLevel]);
	    String macTemp = generateMAC(queryFile[i], this.key[securityLevel]);
	    if (queryFile[i].equals(queryReturned[index]) && macTemp.equals(mac[index]))
	    {
		result = 1; // the cloud is honest
		break;
	    }
	}

	return result;
    }

    /**
     * It generates a random filename, with its security level embedded in the filename.
     * This function is mainly for performance evaluation purpose.
     * 
     * @return A random existing filename.
     */
    public String getRandomFile()
    {
	Random r = new Random((long) 0xff);

	File directory = new File(this.rootDirectory);
	String[] allFiles = directory.list();

	int index = Math.abs(r.nextInt()) % allFiles.length;

	int securityLevel = (int) (index / allFiles.length * this.securityLevels) + 1;
	return allFiles[index] + "-" + securityLevel;
    }

    /**
     * It checks whether a filename is a prefix of some existing file in a given
     * file set.
     * 
     * @param filename
     *            The filename to be checked.
     * @param array
     *            The file set.
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
     * 
     * @param filename
     *            The filename to be checked.
     * @param array
     *            The file set.
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
     * 
     * @param filename
     *            The input filename to be MACed.
     * @return The corresponding MAC of the filename.
     */
    private String generateMAC(String filename, byte[] key)
    {
	try
	{
	    this.mac.init(new SecretKeySpec(key, "HmacSHA256"));
	    return bytes2HexString(this.mac.doFinal(filename.getBytes()));
	} catch (Exception e)
	{
	    System.out.println("Error in DiffVFS.generateMAC: " + e);
	}

	return null;
    }

    private final static byte[] hex = "0123456789ABCDEF".getBytes();

    /**
     * It transforms a byte array into a string in the Hexadecimal format in an
     * entry-wise way.
     * 
     * @param b
     *            The byte array.
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

    public TreeMap<String, String> getFileTree()
    {
	return fileTree;
    }

    public void setFileTree(TreeMap<String, String> fileTree)
    {
	this.fileTree = fileTree;
    }
    
    public int getSecurityLevels()
    {
        return securityLevels;
    }

    public void setSecurityLevels(int securityLevels)
    {
        this.securityLevels = securityLevels;
    }
}
