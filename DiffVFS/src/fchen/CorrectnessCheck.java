package fchen;

import java.util.Arrays;

/**
 * It checks the correctness of the core class 'DiffVFS'.
 * @author Chen, Fei
 * @author Last Update Date: 2015.08.03
 */
public class CorrectnessCheck 
{
	public static void main(String[] args) 
	{
		String rootDirectory = "D:\\test\\files\\test1";  // This directory contains the files listed below.
		String[] allFiles    = {"a", "aaa", "ab1", "b23", "c", "aa", "aaa111", "ab", "b2", "b234", "d123", "58"};  // It contains both existing files and non-existing files.
		
		DiffVFS test = new DiffVFS (rootDirectory, 3);
		test.outsource();
		//test.print();
		
		//File directory = new File(rootDirectory);
		//String[] allFiles = directory.list();
		
		System.out.println("query and verification tests:\n");
				
		for (int i = 0; i < allFiles.length; i++)
		{
		    	String temp = allFiles[i];
		    	int securityLevel = (int) (i / (allFiles.length / test.getSecurityLevels())) + 1;
			String[] query    = test.query(temp, securityLevel);
			ProofData proof   = null;
			System.out.println("the query file is: " +  Arrays.toString(query));
			
			proof      = test.search(query);
			int result = proof.getResult();
			
			if (result == 0)
				System.out.println("search failed.");
			else
			{
				System.out.println("search successful.");
				
				result = test.verify(temp, securityLevel, proof);
				if (result == 2)
					System.out.println("verification failed. (The cloud cheats.)");
				else
					System.out.println("verification succesful. (The cloud is honest.)");
				
				System.out.println("\n");
			}
		}
	}

}
