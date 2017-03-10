/**
 * 
 */
package com.ducesoft.support.mybatis.pager;

import java.io.Serializable;

/**
 * @author coyzeng@gmail.com
 *
 */
public interface IPage extends Serializable {

	void setIndex(Integer index);

	void setLimit(Integer limit);

	void setTotal(Integer total);
	
	void setData(Object data);

	void setOrderBy(String orderBy);
}
