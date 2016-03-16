/**
 * kevin 2015年7月21日
 */
package com.drive.cool.message.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class EncodeUtils
{
  public static final String DEFAULT_CHARSET_NAME = "UnicodeLittleUnmarked";

  public static String parseHex(int value)
  {
    return Integer.toHexString(value);
  }

  public static String parseOctal(int value)
  {
    return Integer.toOctalString(value);
  }

  public static String parseBinary(int value)
  {
    return Integer.toBinaryString(value);
  }

  public static int valueOfHex(String value)
  {
    return Integer.parseInt(value, 16);
  }

  public static int valueOfOctal(String value)
  {
    return Integer.parseInt(value, 8);
  }

  public static int valueOfBinary(String value)
  {
    return Integer.parseInt(value, 2);
  }

  public static String encodeHex(byte[] bytes)
  {
    if (null == bytes)
      return null;
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < bytes.length; i++) {
      String hexStr = Integer.toHexString(0xFF & bytes[i]);
      if (hexStr.length() < 2) {
        sb.append("0");
      }
      sb.append(hexStr);
    }
    return sb.toString();
  }

  public static byte[] decodeHex(String str)
  {
    if (null == str)
      return null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    for (int i = 0; i < str.length(); i += 2) {
      char c1 = str.charAt(i);
      if (i + 1 >= str.length())
        throw new IllegalArgumentException();
      char c2 = str.charAt(i + 1);
      byte b = 0;
      if ((c1 >= '0') && (c1 <= '9'))
        b = (byte)(b + (c1 - '0') * 16);
      else if ((c1 >= 'a') && (c1 <= 'f'))
        b = (byte)(b + (c1 - 'a' + 10) * 16);
      else if ((c1 >= 'A') && (c1 <= 'F'))
        b = (byte)(b + (c1 - 'A' + 10) * 16);
      else
        throw new IllegalArgumentException();
      if ((c2 >= '0') && (c2 <= '9'))
        b = (byte)(b + (c2 - '0'));
      else if ((c2 >= 'a') && (c2 <= 'f'))
        b = (byte)(b + (c2 - 'a' + 10));
      else if ((c2 >= 'A') && (c2 <= 'F'))
        b = (byte)(b + (c2 - 'A' + 10));
      else
        throw new IllegalArgumentException();
      baos.write(b);
    }
    return baos.toByteArray();
  }

  public String convertUnicode(String src, String charsetName)
    throws UnsupportedEncodingException
  {
    String strRet = "";

    src = new String(src.getBytes("ISO-8859-1"), charsetName);
    for (int i = 0; i < src.length(); i++) {
      char c = src.charAt(i);
      int intAsc = c;
      if (intAsc > 128) {
        String strHex = Integer.toHexString(intAsc);
        strRet = strRet + "&#x" + strHex + ";";
      } else {
        strRet = strRet + c;
      }
    }
    return strRet;
  }
}