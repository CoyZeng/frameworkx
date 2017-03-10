/**
 * 
 */
package com.ducesoft.support.mybatis.pager;

/**
 * @author coyzeng@gmail.com
 *
 */
public class PagerCountSupport {

	private static final String CQL_PREFIX = "select count(";
	private static final String CQL_SUFFIX = ") from ";

	public static String dealCount(String sql) {
		StringBuilder cql = new StringBuilder(CQL_PREFIX);
		String temp = sql.toLowerCase();
		int from = temp.indexOf("distinct");
		if (from > -1) {
			int to = temp.indexOf(")", from + 8);
			cql.append(temp.substring(from, to + 1));
		} else {
			cql.append(1);
		}
		return cql.append(CQL_SUFFIX).append(sql.substring(sql.indexOf("from") + 4)).toString();
	}
}
