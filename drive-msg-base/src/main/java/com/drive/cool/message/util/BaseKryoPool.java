/**
 * kevin 2015年8月3日
 */
package com.drive.cool.message.util;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;

import com.esotericsoftware.kryo.Kryo;

/**
 * 序列化工厂类池
 * @author kevin
 *
 */
public class BaseKryoPool {
	private transient static ObjectPool<Kryo> KRYO_POOL = null;
	
	private static BaseKryoPool kryoPool = null;
	
	public synchronized static BaseKryoPool getInstance(){
		if(null == kryoPool) {
			PooledObjectFactory factory = new KryoFactory();
			KRYO_POOL = new GenericObjectPool<Kryo>(factory);
			kryoPool = new BaseKryoPool();
		}
		return kryoPool;
	}
	
	public ObjectPool<Kryo> getKryoPool(){
		return KRYO_POOL;
	}
	
}
