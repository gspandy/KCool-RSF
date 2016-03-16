/**
 * kevin 2015年8月3日
 */
package com.drive.cool.message.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.drive.cool.message.event.Event;
import com.drive.cool.message.event.IEvent;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * event打包解包工具类
 * 
 * @author kevin
 *
 */
public class EventPackUtil {

	/**
	 * 将调用的功能打包到头部，方便路由的时候解包处理
	 * 
	 * @param content
	 * @return
	 */
	public static byte[] packLengthToHead(byte[] content) {
		int length = content.length;
		byte[] ret = new byte[4 + length];

		// 总长度
		ret[3] = (byte) (length & 0xff);
		ret[2] = (byte) (length >> 8 & 0xff);
		ret[1] = (byte) (length >> 16 & 0xff);
		ret[0] = (byte) (ret[1] ^ ret[2] ^ ret[3]);

		System.arraycopy(content, 0, ret, 4, length);
		return ret;
	}

	public static int unPackHeader(byte[] bytes) {
		if (bytes[0] == (bytes[1] ^ bytes[2] ^ bytes[3])) {
			// 校验完成后置掉首位
			bytes[0] = 0;
			return byteArrayToInt_C(bytes);
		} else {
			throw new RuntimeException("包校验失败");
		}
	}

	/**
	 * 把4个网络字节顺序的字节转换成int值,与C服务端交互
	 * 
	 * @param value
	 *            要转换的int值
	 * @return byte[] - 网络字节值
	 */
	private static int byteArrayToInt_C(byte[] bts) {
		int value1 = 0xFF;
		int value2 = 0XFF;
		int value3 = 0XFF;
		int value4 = 0XFF;

		value1 = (value1 & bts[0]) * (0XFFFFFF + 1);
		value2 = (value2 & bts[1]) * (0XFFFF + 1);
		value3 = (value3 & bts[2]) * (0XFF + 1);
		value4 = value4 & bts[3];

		return value1 + value2 + value3 + value4;
	}

	/**
	 * 手工打包event，按字节处理 "eventId", //4个byte "eventType",//2个byte
	 * "sendType",//2个byte "errorNo",//4个byte "timeoutSecond",//4个byte
	 * "datasetType",//2个byte "userId",//最多200个byte "func",//最多200个byte
	 * "errorInfo",//不定长，最多4000个byte "messageBody" byte[]
	 * 
	 * @param event
	 * @return
	 */
	public static byte[] pack(Event event) {
		ByteArrayOutputStream outStream = null;
		Output output = null;
		byte[] eventByte = null;
		try{
			outStream = new ByteArrayOutputStream();
			String userId = event.getUserId();
			output = new Output(outStream);
			output.writeString(userId);
			output.flush();
			byte[] userByte = outStream.toByteArray();
			outStream.reset();
			String func = event.getFunc();
			output = new Output(outStream);
			output.writeString(func);
			output.flush();
			byte[] funcByte = outStream.toByteArray();
			outStream.reset();
			String errorInfo = event.getErrorInfo();
			output.writeString(errorInfo);
			output.flush();
			byte[] errorInfoByte = outStream.toByteArray();
			outStream.reset();
			byte[] eventBody = event.getMessageBody();
			int bodyLenth = null == eventBody ? 0 : eventBody.length;
			int length = 18 + userByte.length + funcByte.length
					+ errorInfoByte.length + bodyLenth;
	
			eventByte = new byte[length];
	
			// eventId
			output = new Output(4);
			output.writeInt(event.getEventId());
			System.arraycopy(output.getBuffer(), 0, eventByte, 0, 4);
	
			// eventType
			output = new Output(2);
			output.writeChar(event.getEventType());
			System.arraycopy(output.getBuffer(), 0, eventByte, 4, 2);
	
			// sendType
			output = new Output(2);
			output.writeChar(event.getSendType());
			System.arraycopy(output.getBuffer(), 0, eventByte, 6, 2);
	
			// errorNo
			output = new Output(4);
			output.writeInt(event.getErrorNo());
			System.arraycopy(output.getBuffer(), 0, eventByte, 8, 4);
	
			// timeout
			output = new Output(4);
			output.writeInt(event.getTimeout());
			System.arraycopy(output.getBuffer(), 0, eventByte, 12, 4);
	
			// datasetType
			output = new Output(2);
			output.writeChar(event.getDatasetType());
			System.arraycopy(output.getBuffer(), 0, eventByte, 16, 2);
	
			// userId
			System.arraycopy(userByte, 0, eventByte, 18, userByte.length);
	
			// func
			System.arraycopy(funcByte, 0, eventByte, 18 + userByte.length,
					funcByte.length);
	
			// errorInfo
			System.arraycopy(errorInfoByte, 0, eventByte, 18 + userByte.length
					+ funcByte.length, errorInfoByte.length);
	
			// body
			if (bodyLenth > 0) {
				System.arraycopy(eventBody, 0, eventByte, 18 + userByte.length
						+ funcByte.length + errorInfoByte.length, bodyLenth);
			}
		}finally{
			if(null != outStream){
				try {
					outStream.close();
				} catch (IOException e) {
		
				}
			}
			if(null != output){
				output.close();
			}
		}
		
		return eventByte;
	}

	/**
	 * 
	 * @param buffer
	 *            此时buffer只剩下event的内容
	 * @return
	 */
	public static IEvent unPack(byte[] buffer) {
		if (buffer == null || buffer.length < 1) {
			return null;
		}
		Event result = new Event();
		// eventId
		Input input = new Input(buffer, 0, 4);
		result.setEventId(input.readInt());
		// eventType
		input = new Input(buffer, 4, 2);
		result.setEventType(input.readChar());
		// sendType
		input = new Input(buffer, 6, 2);
		result.setSendType(input.readChar());

		// errorNo
		input = new Input(buffer, 8, 4);
		result.setErrorNo(input.readInt());

		// timeout
		input = new Input(buffer, 12, 4);
		result.setTimeout(input.readInt());

		// datasetType
		input = new Input(buffer, 16, 2);
		result.setDatasetType(input.readChar());

		// userId
		int length = buffer.length - 18;
		if (length > 200)
			length = 200;
		input = new Input(buffer, 18, length);
		result.setUserId(input.readString());

		// func
		int currPos = input.position();
		length = buffer.length - currPos;
		if (length > 200)
			length = 200;
		input = new Input(buffer, currPos, length);
		result.setFunc(input.readString());

		// errorInfo
		currPos = input.position();
		length = buffer.length - currPos;
		if (length > 4000)
			length = 4000;
		input = new Input(buffer, currPos, length);

		result.setErrorInfo(input.readString());

		int allLength = buffer.length;
		int currLength = input.position();

		if (currLength < allLength) {
			byte[] body = new byte[allLength - currLength];
			System.arraycopy(buffer, currLength, body, 0, allLength
					- currLength);
			result.setMessageBody(body);
		}
		return result;
	}

	/**
	 * 从event的buffer里获取eventId
	 * 
	 * @param buffer
	 *            buffer为整个event，第1个byte开始是eventId的内容，4个字符
	 * @return
	 */
	public static int getEventId(byte[] buffer) {
		Input input = new Input(buffer, 0, 4);
		return input.readInt();
	}

	/**
	 * 
	 * @param buffer
	 * @return buffer为整个event，第5个byte开始是eventType的内容，2个字符
	 */
	public static char getEventType(byte[] buffer) {
		Input input = new Input(buffer, 4, 2);
		return input.readChar();
	}

	/**
	 * 
	 * @param buffer
	 * @return buffer为整个event，第7个byte开始是sendType的内容，2个字符
	 */
	public static char getSendType(byte[] buffer) {
		Input input = new Input(buffer, 6, 2);
		return input.readChar();
	}

	/**
	 * 
	 * @param buffer
	 * @return buffer为整个event，第9个byte开始是errorNo的内容，4个字符
	 */
	public static int getErrorNo(byte[] buffer) {
		Input input = new Input(buffer, 8, 4);
		return input.readInt();
	}

	/**
	 * 
	 * @param buffer
	 * @return buffer为整个event，第13个byte开始是超时时间的内容，4个字符
	 */
	public static int getTimeout(byte[] buffer) {
		Input input = new Input(buffer, 12, 4);
		return input.readInt();
	}

	/**
	 * 
	 * @param buffer
	 * @return buffer为整个event，第17个byte开始是dataset类型的内容，2个字符
	 */
	public static char getDatasetType(byte[] buffer) {
		Input input = new Input(buffer, 16, 2);
		return input.readChar();
	}

	/**
	 * 从event的buffer数组里获取当前登陆的用户
	 * 
	 * @param buffer
	 *            buffer为整个event，第19个byte开始是userId的内容，最长是200个字符
	 * @return
	 */
	public static String getUserId(byte[] buffer) {
		int length = buffer.length - 18;
		if (length > 200)
			length = 200;
		Input input = new Input(buffer, 18, length);
		return input.readString();
	}

	/**
	 * 从event的buffer数组里获取调用的功能
	 * 
	 * @param buffer
	 *            buffer为整个event，第19个byte开始是userId的内容，最长是200个字符<br>
	 *            接下来是func的内容，最长200个字符
	 * @return
	 */
	public static String getFunc(byte[] buffer) {
		int length = buffer.length - 18;
		if (length > 200)
			length = 200;
		Input input = new Input(buffer, 18, length);
		input.readString();
		int currPos = input.position();
		length = buffer.length - currPos;
		if (length > 200)
			length = 200;
		input = new Input(buffer, currPos, length);
		return input.readString();
	}

	/**
	 * 从event的buffer数组里获取errorInfo
	 * 
	 * @param buffer
	 *            buffer为整个event，第start个byte开始是erorInfo的内容，最长是4000个字符
	 * @return
	 */
	public static String getErrorInfo(byte[] buffer, int start) {
		int length = buffer.length - start;
		if (length > 4000)
			length = 4000;
		Input input = new Input(buffer, start, length);
		String t = input.readString();
		return input.readString();
	}

	/**
	 * 重新设置buffer里面的eventId属性
	 * 
	 * @param buffer
	 * @param userId
	 * @return 重置后的buffer
	 */
	public static byte[] resetUserId(byte[] buffer, String userId) {
		ByteArrayOutputStream outStream = null;
		Output output = null;
		byte[] newBuffer = null;
		try {
			outStream = new ByteArrayOutputStream();
			outStream.reset();
			output = new Output(outStream);
			output.writeString(userId);
			output.flush();
			byte[] userIdByte = outStream.toByteArray();
			
			int bufferLength = buffer.length - 18;
			if (bufferLength > 200)
				bufferLength = 200;
			Input input = new Input(buffer, 18, bufferLength);
			input.readString();
			int curPos = input.position();
			int preLength = curPos - 18;;
			int length = userIdByte.length;
			if(preLength == length){
				for (int i = 0; i < length; i++) {
					buffer[i+18] = userIdByte[i];
				}
				return buffer;
			}else{
				int newLength = buffer.length + length - preLength;
				newBuffer = new byte[newLength];

				System.arraycopy(buffer, 0, newBuffer, 0, 18);
				System.arraycopy(userIdByte, 0, newBuffer, 18, length);
				System.arraycopy(buffer, 18 + preLength, newBuffer, 18 + length,
						buffer.length - preLength - 18);
				return newBuffer;
			}
		} finally {
			if (null != outStream) {
				try {
					outStream.close();
				} catch (IOException e) {

				}
			}
			if (null != output) {
				output.close();
			}
		}
		
	}

	public static void main(String[] args) {
		Event t = new Event();
		t.setFunc("大大夫来的降低时啦附件了多少的。");
		t.setUserId("1234564");
		t.setTimeout(1000);
		t.setEventId(10000);
		byte[] tt = EventPackUtil.pack(t);
		IEvent k = EventPackUtil.unPack(tt);
		String userId = "1234561";
		byte[] s = EventPackUtil.resetUserId(tt, userId);
		IEvent k1 = EventPackUtil.unPack(s);

		System.out.println(k1);

	}

	/**
	 * 重新设置buffer里面的eventId属性
	 * 
	 * @param buffer
	 * @param eventId
	 */
	public static void resetEventId(byte[] buffer, int eventId) {
		Output output = new Output(4);
		output.writeInt(eventId);
		byte[] eventIdByte = output.getBuffer();
		for (int i = 0; i < 4; i++) {
			buffer[i] = eventIdByte[i];
		}
	}

}
