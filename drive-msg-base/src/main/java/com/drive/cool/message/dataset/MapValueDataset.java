/**
 * kevin 2015年8月1日
 */
package com.drive.cool.message.dataset;

import java.util.HashMap;
import java.util.Map;
/**
 * 键值对传输数据
 * @author kevin
 *
 */
public class MapValueDataset extends AbstractDataset<Map> {

	private Map<String, Object> value;
	
	public MapValueDataset() {
		super.datasetType = MAP_VALUE;
	}
	
	@Override
	public Map getValue() {
		return value;
	}

	@Override
	public void setValue(Map value) {
		this.value = value;
	}

	public void setValue(String key, Object value) {
		if(null == this.value){
			this.value = new HashMap<String, Object>();
		}
		this.value.put(key, value);
	}

	public Object getValueByKey(String key){
		return null == this.value ? null : this.value.get(key);
	}
}
