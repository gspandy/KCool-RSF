package com.drive.cool.msg.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class MessageCodecFactory implements ProtocolCodecFactory {

	/**
	 * 
	 */
	public MessageCodecFactory(ProtocolDecoder protocolDecoder, ProtocolEncoder protocolEncoder) {
		setProtocolDecoder(protocolDecoder);
		setProtocolEncoder(protocolEncoder);
	}
	
	public MessageCodecFactory(){
		
	}
	/**
	 * 协议解码器
	 */
	private ProtocolDecoder protocolDecoder;

	/**
	 * 协议打包器
	 */
	private ProtocolEncoder protocolEncoder;

	@Override
	public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
		return protocolDecoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
		return protocolEncoder;
	}

	public ProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
		this.protocolDecoder = protocolDecoder;
	}

	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
		this.protocolEncoder = protocolEncoder;
	}

}
