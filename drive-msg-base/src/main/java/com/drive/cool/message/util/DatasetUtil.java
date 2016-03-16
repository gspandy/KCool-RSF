/**
 * kevin 2015年7月21日
 */
package com.drive.cool.message.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.drive.cool.message.dataset.IDataset;
import com.drive.cool.message.dataset.ListValueDataset;
import com.drive.cool.message.dataset.MapValueDataset;
import com.drive.cool.message.dataset.MultiObjectDataset;
import com.drive.cool.message.dataset.SimpleObjectDataset;

/**
 * @author kevin
 *
 */
public class DatasetUtil {
	/**
	 * 将对象转成IDataset
	 * @param value
	 * @return
	 */
	public static IDataset parse(Object value){
		if(null == value){
			return null;
		}
		IDataset dataset = null;
		if(value instanceof Map){
			dataset = new MapValueDataset();
		}else if(value instanceof List){
			List data = (List)value;
			if(data.size() > 0){
				Object listData = data.get(0);
				if(listData instanceof Map){
					dataset = new ListValueDataset();
				}else{
					throw new RuntimeException("不支持的数据类型");
				}
			}
		}else if(value instanceof String
				|| value instanceof Integer
				|| value instanceof Double
				|| value instanceof Float
				|| value instanceof BigDecimal
				|| value instanceof Boolean){
			dataset = new SimpleObjectDataset();
		}else{
			dataset = new MultiObjectDataset();
		}
		dataset.setValue(value);
		return dataset;
	}
	
	public static boolean isMapDataset(IDataset dataset){
		if(null == dataset) return false;
		return IDataset.MAP_VALUE == dataset.getType();
	}
	
	public static boolean isListDataset(IDataset dataset){
		if(null == dataset) return false;
		return IDataset.LIST_MAP_VALUE == dataset.getType();
	}
	
	public static boolean isSimpleDataset(IDataset dataset){
		if(null == dataset) return false;
		return IDataset.SIMPLE_OBJECT == dataset.getType();
	}
	
}
