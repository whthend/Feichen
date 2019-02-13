package fchen;

import java.util.Arrays;

/**
 * It checks the correctness of the core class 'PrefixBasedOutsourcing'.
 * @author Chen, Fei
 * @author Final Modifying Date: 2013.04.17
 */
public class CorrectnessCheck 
{
	public static void main(String[] args) 
	{
		String rootDirectory = "D:\\test\\files\\test1";  // This directory contains all files listed below.
		String[] allFiles    = {"a", "aaa", "ab1", "b23", "c", "aa", "aaa111", "ab", "b2", "b234", "d123", "58"};  //It contains both existing files and non-existing files.
		
		PrefixBasedOutsourcing test = new PrefixBasedOutsourcing (rootDirectory);
		test.outsource();
		test.print();
		
		//File directory = new File(rootDirectory);
		//String[] allFiles = directory.list();
		
		System.out.println("query and verification tests:\n");
				
		for (String temp : allFiles)
		{
			String query    = test.query(temp);
			ProofData proof = null;
			System.out.println("the query file is: " + query);
			
			proof      = test.search(query);
			int result = proof.getResult();
			byte[] mac = proof.getMac();
			if (result == 0)
				System.out.println("search failed.");
			else
			{
				System.out.println("search successful.");
				System.out.println("the proof is: " + Arrays.toString(mac));
				if (result == 1)
					System.out.println("the query file is an existing file.");
				else if (result == 2)
					System.out.println("the query file is a prefix of some existing file.");
				else
					System.out.println("the query file is different from all existing file.");
				
				result = test.verify(query, proof);
				
				if (result == 1)
					System.out.println("verification succesful. (The cloud is honest.)");
				else
					System.out.println("verificatio failed. (The cloud cheats.)");
				
				System.out.println("\n");
			}
		}
	}

}
