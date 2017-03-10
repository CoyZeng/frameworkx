/**
 * 
 */
package com.ducesoft.support.mybatis.pager;

import java.util.List;

/**
 * 兼容系统的各种分页
 * 
 * @author coyzeng@gmail.com
 *
 */
public final class Page<T> extends Pager implements IPage {

	private static final long serialVersionUID = -7382000053524845710L;

	private List<T> data;

	public List<T> getData() {
		return data;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setData(Object data) {
		this.data = (List<T>) data;
	}
}
