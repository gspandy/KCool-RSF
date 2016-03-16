/**
 * kevin 2015年8月1日
 */
package com.drive.cool.message.dataset;

/**
 * 数据传输的接口
 * @author kevin
 *
 */
public interface IDataset<T> {
	public static final char NULL_VALUE = 0x01;
	/**
	 * 单个值，可能是各种简单类型的值
	 */
	public static final char SIMPLE_OBJECT = 0x02;
	/**
	 * map存储，每个key对应的是一个SINGLE_VALUE
	 */
	public static final char MAP_VALUE = 0x03;
	/**
	 * list<Map>存储，每一条记录对应的是一个 MAP_VALUE
	 */
	public static final char LIST_MAP_VALUE = 0x04;
	
	/**
	 * map存储，每一个key对应的是一个LIST_MAP_VALUE
	 */
	public static final char MAP_LIST_VALUE = 0x05;
	
	/**
	 * json格式存储
	 */
	public static final char JSON_VALUE = 0x06;
	
	/**
	 * 复杂object，这种类型的序列化的时候要带上class的值。<br>
	 * 具体寸的值是 Object[] 类型的，每一个里面都是object值。<br>
	 */
	public static final char MULTI_OBJECT = 0x07;
	
	
	/**
	 * 获取dataset类型
	 * @return
	 */
	public char getType();
	
	/**
	 * 获取dataset值
	 * @return
	 */
	public T getValue();
	
	/**
	 * 设置dataset的值
	 * @param value
	 */
	public void setValue(T value);
	
}
