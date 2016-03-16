/**
 * kevin 2015年8月1日
 */
package com.drive.cool.message.dataset;

/**
 * 复杂自定义数据类型传输数据
 * @author kevin
 *
 */
public class MultiObjectDataset<T> extends AbstractDataset<T> {

	/**
	 * dataset的值
	 */
	private T value;
	
	/**
	 * 
	 */
	public MultiObjectDataset() {
		super.datasetType = MULTI_OBJECT;
	}
	
	@Override
	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
	
	
}
