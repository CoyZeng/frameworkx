/**
 * 
 */
package com.ducesoft.support.mybatis.pager;

import java.io.Serializable;

/**
 * 兼容系统的各种分页
 * 
 * @author coyzeng@gmail.com
 */
public class Pager implements Serializable, IPager {

	public static Pager MAX()

	{
		return new Pager(1, Integer.MAX_VALUE);
	}

	public static Pager ONE()

	{
		return new Pager(1, 1);
	}

	public static Pager TEN() 
	
	{
		return new Pager(1, 10);
	}

	public static Pager HUNDRED() 
	
	{
		return new Pager(1, 100);
	}

	public static Pager FIVE_HUNDRED() 
	
	{
		return new Pager(1, 500);
	}

	private static final long serialVersionUID = -4741413203022052497L;

	private Integer index = 1;
	private Integer limit = 10;
	private Integer total = 0;
	private String orderBy = "";

	@Override
	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	@Override
	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getTotal() {
		return total;
	}

	@Override
	public void setTotal(Integer total) {
		this.total = total;
	}

	@Override
	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public Pager() {
	}

	public Pager(int index, int limit) {
		this.index = index;
		this.limit = limit;
	}

	@Override
	public int indexOffset() {
		return 1;
	}

}
