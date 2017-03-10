/**
 * 
 */
package com.ducesoft.support.mybatis.pager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

/**
 * @author coyzeng@gmail.com
 *
 */
public class DarkLanguageDriver extends XMLLanguageDriver implements LanguageDriver {

	Pattern pattern = Pattern.compile("([>|<|>=|<=|&|<>])");
	
	@Override
	public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
		Class<?> defDriver = configuration.getLanguageRegistry().getDefaultDriverClass();
		if (XMLLanguageDriver.class != defDriver) {
			configuration.getLanguageRegistry().setDefaultDriverClass(XMLLanguageDriver.class);
		}
		String fixed = script;
		for (DarkLanguage dark : darks) {
			fixed = dark.handle(configuration, fixed, parameterType);
		}
		if (!"<script>".startsWith(fixed) && !fixed.equals(script)) {
			script = "<script>" + fixed + "</script>";
		}
		return super.createSqlSource(configuration, script, parameterType);
	}

	static final List<DarkLanguage> darks = new ArrayList<>();

	static {
		darks.add(DarkLanguage.inquery);
		darks.add(DarkLanguage.update);
		darks.add(DarkLanguage.selective);
	}
	
	@SuppressWarnings({"unused" })
	public static void main(String[] args) {
		Pattern sePattern = Pattern.compile("([and|or]{1}\\s*\\w+=#\\{(\\S+)\\})");
		Matcher match = Pattern.compile("in\\s*\\(#\\{(\\w+)\\}\\)").matcher("in (#{aaaaaaa})");
		System.out.println(DarkLanguage.inquery.handle(null, "select from t_bms_snap_spec where food_serial=#{serial} and is_delete=0", null));
		System.out.println(DarkLanguage.selective.handle(null, "select distinct(batch) from t_bms_push_point where status&8<>8 and brand_id=#{q.brandId} and a=#{a} and time>now()", null));
		System.out.println(DarkLanguage.selective.handle(null, "select distinct(batch) from t_bms_push_point where status&8<>8 and brand_id=#{q.brandId} and time>now()", null));
	}
}
