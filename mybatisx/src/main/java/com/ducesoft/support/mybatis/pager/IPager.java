/**
 * 
 */
package com.ducesoft.support.mybatis.pager;

import java.io.Serializable;

/**
 * @author coyzeng@gmail.com
 *
 */
public interface IPager extends Serializable{
	
	Integer getIndex();

	Integer getLimit();

	void setTotal(Integer total);

	String getOrderBy();
	
	/** 首页偏移，0为起始页还是1为起始页，最终分页会减去该偏移 */
	int indexOffset();

}
