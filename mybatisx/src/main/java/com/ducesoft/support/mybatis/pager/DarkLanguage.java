/**
 * 
 */
package com.ducesoft.support.mybatis.pager;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.session.Configuration;

/**
 * @author coyzeng@gmail.com
 *
 */
public interface DarkLanguage {

	Pattern sePattern = Pattern.compile("(\\s+and\\s+\\S+=#\\{\\S+\\})");
	Pattern inPattern = Pattern.compile("in\\s*\\(#\\{(\\w+)\\}\\)");
	Pattern upPattern = Pattern.compile("set\\s*\\(#\\{(\\w+)\\}\\)");
	String foreach = "in <foreach collection=\"$1\" item=\"_item\" open=\"(\" separator=\",\" close=\")\">#{_item}</foreach>";

	String handle(Configuration configuration, String script, Class<?> parameterType);

	DarkLanguage update = (Configuration configuration, String script, Class<?> parameterType) -> {
		Matcher matcher = upPattern.matcher(script);
		if (matcher.find()) {
			StringBuilder ss = new StringBuilder();
			ss.append("<set>");
			for (Field field : parameterType.getDeclaredFields()) {
				if (!field.isAnnotationPresent(Transient.class)) {
					ss.append("<if test=\"__field != null\">");
					ss.append(field.getName()); // 需要修正为驼峰命名法
					ss.append("=#{__field},");
					ss.append("</if>");
				}
			}
			ss.deleteCharAt(ss.lastIndexOf(","));
			ss.append("</set>");
			return matcher.replaceAll(ss.toString());
		}
		return script;
	};

	DarkLanguage inquery = (Configuration configuration, String script, Class<?> parameterType) -> {
		Matcher matcher = inPattern.matcher(script);
		if (matcher.find()) {
			String group = matcher.group();
			int start = group.indexOf("{");
			int end = group.indexOf("}");
			String name = group.substring(start + 1, end);
			return matcher.replaceAll(foreach.replaceFirst("$1", name));
		}
		return script;
	};
	
	DarkLanguage selective = (Configuration configuration, String script, Class<?> parameterType) -> {
		Matcher matcher = sePattern.matcher(script);
		if (matcher.find()) {
			int count = matcher.groupCount();
			StringBuilder bu = new StringBuilder();
			String condition = matcher.group(0);
			bu.append(script.substring(0, script.indexOf(condition)));
			for (int n = 0; n < count; n++) {
				String cd = matcher.group(n);
				bu.append("<if test=\"");
				bu.append(cd.substring(cd.indexOf("{") + 1, cd.indexOf("}")).trim());
				bu.append("!= null\">");
				bu.append(cd);
				bu.append("</if>");
			}
			String last = matcher.group(count);
			bu.append(script.substring(script.indexOf(last) + last.length()));
			return bu.toString();
		}
		return script;
	};
	
}
