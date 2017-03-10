/**
 * 
 */
package com.ducesoft.support.mybatis.pager;

/**
 * @author coyzeng@gmail.com
 *
 */
public class PagerHolder {

	private static final ThreadLocal<Pager> pagerSesison = new ThreadLocal<>();

	public static Pager get() {
		return pagerSesison.get();
	}

	public static void init(Pager local) {
		pagerSesison.set(local);
	}

	public static void clear() {
		pagerSesison.remove();
	}

	
}
