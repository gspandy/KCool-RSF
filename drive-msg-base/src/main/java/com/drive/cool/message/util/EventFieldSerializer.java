/**
 * kevin 2015年8月3日
 */
package com.drive.cool.message.util;

import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

/**
 * 事件序列化自定义字段顺序
 * @author kevin
 *
 */
public class EventFieldSerializer<T> extends FieldSerializer<T> {

	/**
	 * @param kryo
	 * @param type
	 */
	public EventFieldSerializer(Kryo kryo, Class type) {
		super(kryo, type);
	}


	/**
	 * 
	 * @param kryo
	 * @param type
	 * @param generics
	 */
	public EventFieldSerializer(Kryo kryo, Class type, Class[] generics) {
		super(kryo, type, generics);
	}
	
	@Override
	protected void rebuildCachedFields (boolean minorRebuild){
		super.rebuildCachedFields(minorRebuild);
		
		//按指定顺序对field重新排序
		Map<String, CachedField> fieldMap = new HashMap<String, CachedField>();
		for(CachedField cached : getFields()){
			fieldMap.put(cached.getField().getName(), cached);
		}
		String[] fieldArr = {
				"eventId", //4个byte
				"eventType",//2个byte
				"sendType",//2个byte
				"errorNo",//4个byte
				"timeoutSecond",//4个byte
				"datasetType",//2个byte
				"userId",//最多100个byte
				"func",//最多100个byte
				"errorInfo",
				"messageBody"
		};
		if(fieldArr.length != fieldMap.size()){
			throw new RuntimeException("指定排序的字段和event类不一致");
		}
		int index = 0;
		for(String field : fieldArr){
			CachedField cacheField = fieldMap.get(field);
			if(null == cacheField){
				throw new RuntimeException("指定排序的字段和event类不一致");
			}
			getFields()[index++] = cacheField;
		}
	}
}
