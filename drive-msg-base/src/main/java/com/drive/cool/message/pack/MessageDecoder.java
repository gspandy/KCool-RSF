/**
 * kevin 2015年8月4日
 */
package com.drive.cool.message.pack;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.drive.cool.message.event.IEvent;
import com.drive.cool.message.util.EventPackUtil;

/**
 * 事件解码
 * @author kevin
 *
 */
public class MessageDecoder extends CumulativeProtocolDecoder {

	@Override
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
			IEvent event = EventPackUtil.unPack(cb);
			output.write(event);
			return true;
		} else {
			return false;
		}
	}
	
	
}
