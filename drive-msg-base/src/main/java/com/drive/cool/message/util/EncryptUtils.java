package com.drive.cool.message.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class EncryptUtils
{
  private static final String DES = "DESede";

  public static String md5Encrypt(String src)
    throws UnsupportedEncodingException
  {
    return encrypt("MD5", src);
  }

  public static String shaEncrypt(String src)
    throws UnsupportedEncodingException
  {
    return encrypt("SHA", src);
  }

  public static String sha256Encrypt(String src)
    throws UnsupportedEncodingException
  {
    return encrypt("SHA-256", src);
  }

  public static String sha512Encrypt(String src)
    throws UnsupportedEncodingException
  {
    return encrypt("SHA-512", src);
  }

  public static SecretKey genDESKey()
  {
    try
    {
      KeyGenerator keygen = KeyGenerator.getInstance("DESede");
      keygen.init(112);
      return keygen.generateKey(); 
    } catch (Exception e) {
    	throw new SecurityException( e);
    }
  }

  public static String desEncrypt(String src, SecretKey key)
    throws UnsupportedEncodingException
  {
    return encrypt("DESede", src, key);
  }

  public static String desDecrypt(String src, SecretKey key)
    throws IOException
  {
    return decrypt("DESede", src, key);
  }

  private static byte[] encrypt(String algorithm, byte[] obj)
  {
    if (null == obj)
      return null;
    try {
      MessageDigest md = MessageDigest.getInstance(algorithm);
      return md.digest(obj); 
      } catch (Exception e) {
    throw new SecurityException(e);
      }
  }

  private static String encrypt(String algorithm, String src)
    throws UnsupportedEncodingException
  {
    if (null == src)
      return null;
    byte[] bytes = src.getBytes("utf-8");
    byte[] encodeBtyes = encrypt(algorithm, bytes);
    return EncodeUtils.encodeHex(encodeBtyes);
  }

  private static byte[] encrypt(String algorithm, byte[] obj, Key key)
  {
    if (null == obj)
      return null;
    try {
      Cipher cipher = null;
      if (algorithm.equals("DESede")) {
        cipher = Cipher.getInstance(algorithm);
      }
      else
      {
        throw new SecurityException("系统不支持" + algorithm + "加密算法");
      }
      cipher.init(1, key);
      return cipher.doFinal(obj);
    } catch (Exception e) {
      throw new SecurityException(e); } 
    
  }

  private static String encrypt(String algorithm, String src, Key key)
    throws UnsupportedEncodingException
  {
    if (null == src)
      return null;
    byte[] bytes = src.getBytes("utf-8");
    byte[] encodeBtyes = encrypt(algorithm, bytes, key);
    return EncodeUtils.encodeHex(encodeBtyes);
  }

  private static byte[] decrypt(String algorithm, byte[] obj, Key key)
  {
    if (null == obj)
      return null;
    try {
      Cipher cipher = null;
      if (algorithm.equals("DESede")) {
        cipher = Cipher.getInstance(algorithm);
      }
      else
      {
        throw new SecurityException("系统不支持" + algorithm + "加密算法");
      }
      cipher.init(2, key);
      return cipher.doFinal(obj);
    } catch (Exception e) {
      throw new SecurityException(e.getMessage());
    } 
  }

  private static String decrypt(String algorithm, String src, Key key)
    throws IOException
  {
    if (null == src)
      return null;
    byte[] bytes = EncodeUtils.decodeHex(src);
    byte[] decodeBtyes = decrypt(algorithm, bytes, key);
    return new String(decodeBtyes, "UnicodeLittleUnmarked");
  }
  
  public static void main(String[] args) {
	  try {
		System.out.println(EncryptUtils.md5Encrypt("admin201507"));
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}