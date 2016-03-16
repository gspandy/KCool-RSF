/**
 * kevin 2015年8月3日
 */
package com.drive.cool.message.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import com.drive.cool.message.dataset.ListValueDataset;
import com.drive.cool.message.dataset.MapValueDataset;
import com.drive.cool.message.dataset.SimpleObjectDataset;
import com.esotericsoftware.kryo.Kryo;

/**
 * 序列化工厂类
 * @author kevin
 *
 */

public class KryoFactory extends BasePooledObjectFactory<Kryo>{
	
	private List<String> registerClazz;
	
	/* (non-Javadoc)
	 * @see org.apache.commons.pool2.BasePooledObjectFactory#create()
	 */
	@Override
	public Kryo create() throws Exception {
		Kryo kryo = new Kryo();
		kryo.setRegistrationRequired(false);
		kryo.setReferences(false);
		kryo.register(BigDecimal.class);
		kryo.register(HashMap.class);
		kryo.register(ArrayList.class);
		kryo.register(SimpleObjectDataset.class);
		kryo.register(MapValueDataset.class);
		kryo.register(ListValueDataset.class);
		kryo.register(byte[].class);
		if(null != registerClazz){
			for(String className : registerClazz){
				kryo.register(Class.forName(className));
			}
		}
		return kryo;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.pool2.BasePooledObjectFactory#wrap(java.lang.Object)
	 */
	@Override
	public PooledObject<Kryo> wrap(Kryo arg0) {
		return new DefaultPooledObject<Kryo>(arg0);
	}

	/**
	 * @return the registerClazz
	 */
	public List getRegisterClazz() {
		return registerClazz;
	}

	/**
	 * @param registerClazz the registerClazz to set
	 */
	public void setRegisterClazz(List registerClazz) {
		this.registerClazz = registerClazz;
	}

}
