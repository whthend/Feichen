package fchen;

import java.security.MessageDigest;
import java.util.Arrays;

/**
 * This class encapsulates the tuple (index, filename, authentication path) and
 * some corresponding methods. The cloud could return a few of such tuples
 * because of the hash collisions.
 * 
 * @author Chen, Fei (https://sites.google.com/site/chenfeiorange/)
 * @author First draft on 12-03-2013. License: GNU GPL
 * @author Email: chenfeiorange@163.com
 * 
 */
public class MetaProofData
{
    private int index                   = -1; // index of the leafnode
    private byte[] filename             = null; // filename of the leafnode
    private byte[][] authenticationPath = null; // authentication path from the
						// bottom to the root; the
						// bottom node lies at the
						// beginning of the array

    public MetaProofData(int index, byte[] filename, byte[][] authenticationPath)
    {
	super();
	this.index 		= index;
	this.filename 		= filename;
	this.authenticationPath = authenticationPath;
    }

    /**
     * This function check whether an authentication path is legal. A legal path
     * has two properties: one is that the leaf node value is equal to
     * hash(index, filename); the other is that the authentication path is
     * correct.
     * 
     * @param root
     *            - the root value of the hash authentication tree
     */
    public boolean validate(byte[] root)
    {
	int cheatFlag = 0;

	MessageDigest md = null;
	try
	{
	    md = MessageDigest.getInstance("SHA-256");
	} catch (Exception e)
	{
	    System.out.println("get SHA-256 instance error - static verify");
	    System.out.println(e);
	}

	md.update(int2byteArray(this.index));
	byte[] tempMac = md.digest(this.filename);

	// check the leaf node value
	if (Arrays.equals(tempMac, this.authenticationPath[0]) == false
		&& Arrays.equals(tempMac, this.authenticationPath[1]) == false)
	    cheatFlag = cheatFlag + 1;
	
	//System.out.println("meta data validate cheat flag" + cheatFlag);
	
	// check the authentication path
	if (HashTree.verify(authenticationPath, root) == false)
	    cheatFlag = cheatFlag + 1;

	//System.out.println("meta data validate cheat flag" + cheatFlag);
	
	if (cheatFlag == 0)
	    return true;
	else
	    return false;
    }

    private byte[] int2byteArray(int num)
    {
	byte[] result = new byte[4];

	result[3] = (byte) (num >>> 24);
	result[2] = (byte) (num >>> 16);
	result[1] = (byte) (num >>> 8);
	result[0] = (byte) (num);

	return result;
    }

    /**
     * This function prints out the proof data object.
     */
    public void print()
    {
	String temp = "[ ";
	temp = temp + String.valueOf(this.index) + "; ";
	temp = temp + HashTreeBasedOutsourcing.bytes2HexString(this.filename) + "; ";
	
	for (int i = 0; i < this.authenticationPath.length; i++)
	    temp = temp + HashTreeBasedOutsourcing.bytes2HexString(this.authenticationPath[i]) + " ";
	temp = temp + "]";
	System.out.println(temp);
    }    
        
    public int getIndex()
    {
	return index;
    }

    public void setIndex(int index)
    {
	this.index = index;
    }

    public byte[] getFilename()
    {
	return filename;
    }

    public void setFilename(byte[] filename)
    {
	this.filename = filename;
    }

    public byte[][] getAuthenticationPath()
    {
	return authenticationPath;
    }

    public void setAuthenticationPath(byte[][] authenticationPath)
    {
	this.authenticationPath = authenticationPath;
    }

}
