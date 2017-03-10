/**
 * 
 */
package com.ducesoft.support.mybatis.pager;

/**
 * @author coyzeng@gmail.com
 *
 */
public interface PagerDialect {

	String forDB(String sql, String orderBy, int index, int limit);

	static String to(String sql, String orderBy, int index, int limit) {
		if (PagerDialectSupport.currentDB().name().startsWith("mysql")) {
			return mysql.forDB(sql, orderBy, index, limit);
		}
		throw new RuntimeException("Unsuppurted for current db");
	}

	PagerDialect mysql = (sql, orderBy, index, limit) -> {
		String temp = sql.toLowerCase();
		StringBuilder my = new StringBuilder(sql);
		if (temp.indexOf("limit") < 0) {
			if (!DPattern.BLANK.matcher(orderBy).matches() && (temp.indexOf("order") < 0 || temp.indexOf("by") < 0)) {
				my.append(" order by ").append(orderBy);
			}
			my.append(" limit ").append(index * limit).append(",").append(limit);
		}
		return my.toString();
	};

}
