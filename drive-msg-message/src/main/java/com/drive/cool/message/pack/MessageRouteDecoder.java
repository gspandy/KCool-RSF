/**
 * kevin 2015年8月4日
 */
package com.drive.cool.message.pack;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.drive.cool.message.util.EventPackUtil;

/**
 * 路由事件解码
 * @author kevin
 *
 */
public class MessageRouteDecoder extends CumulativeProtocolDecoder {

	@Override
	/**
	 * 只接收完数据，不做其他解码操作
	 */
	protected boolean doDecode(IoSession session, IoBuffer ioBuffer, ProtocolDecoderOutput output) throws Exception {
		int remaining = ioBuffer.remaining();
		int position = ioBuffer.position();
		if (remaining > 4) {
			byte[] bytes = new byte[4];
			ioBuffer.get(bytes);
			int length = EventPackUtil.unPackHeader(bytes);
			if (remaining < length + 4) {
				ioBuffer.position(position);
				return false;
			}
			byte[] cb = new byte[length];
			ioBuffer.get(cb);
			output.write(cb);
			return true;
		} else {
			return false;
		}
	}
	
	
}
