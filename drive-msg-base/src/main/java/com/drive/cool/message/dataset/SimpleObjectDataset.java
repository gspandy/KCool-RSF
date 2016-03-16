/**
 * kevin 2015年8月1日
 */
package com.drive.cool.message.dataset;

/**
 * 基础数据类型传输数据
 * @author kevin
 *
 */
public class SimpleObjectDataset<T> extends AbstractDataset<T> {

	public SimpleObjectDataset() {
		super.datasetType = SIMPLE_OBJECT;
	}
	
	public SimpleObjectDataset(T value) {
		this();
		this.setValue(value);
	}
	
	/**
	 * dataset的值
	 */
	private T value;
	
	@Override
	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
}
