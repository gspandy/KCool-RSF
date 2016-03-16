/**
 * kevin 2015年8月4日
 */
package com.drive.cool.message.pack;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.drive.cool.message.util.EventPackUtil;

/**
 * 路由事件编码
 * @author kevin
 *
 */
public class MessageRouteEncoder extends ProtocolEncoderAdapter {
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.filter.codec.ProtocolEncoder#encode(org.apache.mina.core
	 * .session.IoSession, java.lang.Object,
	 * org.apache.mina.filter.codec.ProtocolEncoderOutput)
	 */
	@Override
	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out){
		if (!(message instanceof byte[])) return;
		
		byte[] buffer = byte[].class.cast(message);

		//在头部添加长度信息
		if (buffer != null && buffer.length != 0) {
			byte[] content = EventPackUtil.packLengthToHead(buffer);
			IoBuffer ioBuffer = IoBuffer
					.allocate(content.length, false);
			ioBuffer.put(content);
			ioBuffer.flip();
			out.write(ioBuffer);
		}
	}
}
