package fchen;

/**
 * It checks the correctness of the protocol.
 * 
 * @author Chen, Fei (https://sites.google.com/site/chenfeiorange/)
 * @author First draft on 12-03-2013. License: GNU GPL
 * @author Email: chenfeiorange@163.com
 */
public class CorrectnessCheck 
{
	public static void main(String[] args) 
	{
		String rootDirectory = "D:\\test\\files\\test1";  // This directory contains all files listed below.
		String[] allFiles    = {"a", "aaa", "ab1", "b23", "c"};
		
		HashTreeBasedOutsourcing test = new HashTreeBasedOutsourcing (rootDirectory, 0.2);
		test.prepareOutsource();
		test.outsource();
		
		System.out.println("The hash tree is as follows:");
		test.getHt().print();
		
		System.out.println("query and verification tests:");
		
		for (String temp : allFiles)
		{
			byte[] query    = test.query(temp);
			System.out.println("the query file is: " + temp + " ; " + HashTreeBasedOutsourcing.bytes2HexString(query));
			
			ProofData proof = null;
			proof           = test.search(query);
			System.out.println("The proof is:");
			proof.print();
			
			boolean result = test.verify(query, proof);
			if (result == true)
				System.out.println("verification succesful. (The cloud is honest.)");
			else
			    	System.out.println("verificatio failed. (The cloud cheats.)");
		}
	}

}
