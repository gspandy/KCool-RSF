/**
 * kevin 2015年8月1日
 */
package com.drive.cool.message.dataset;

/**
 * 数据传输对象基类
 * @author kevin
 *
 */
public abstract class AbstractDataset<T> implements IDataset<T> {
	
	
	/**
	 * dataset类型
	 */
	protected char datasetType;
	
	
	@Override
	public char getType(){
		return datasetType;
	}

}
