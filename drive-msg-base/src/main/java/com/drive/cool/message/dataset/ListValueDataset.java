/**
 * kevin 2015年8月1日
 */
package com.drive.cool.message.dataset;

import java.util.List;
import java.util.Map;
/**
 * 列表传输数据
 * @author kevin
 *
 */
public class ListValueDataset extends AbstractDataset<List<Map>> {

	private List<Map> value;
	
	public ListValueDataset() {
		super.datasetType = LIST_MAP_VALUE;
	}
	@Override
	public List<Map> getValue() {
		return value;
	}

	@Override
	public void setValue(List<Map> value) {
		this.value = value;
	}

}
