package com.ducesoft.support.mybatis.plugin;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * 
 */
public class CacheMethodPlugin extends PluginAdapter {

	private Config config;

	public boolean validate(List<String> warnings) {
		if (config == null)
			config = new Config(getProperties());

		return true;
	}

	private boolean addCache(XmlElement element, IntrospectedTable introspectedTable) {
		String optionString = config.getOptionsValue(introspectedTable.getFullyQualifiedTable().getDomainObjectName(),
				introspectedTable.getSelectByPrimaryKeyStatementId());
		if (optionString == null)
			return true;

		String[] pairs = optionString.split(",");

		for (String pair : pairs) {
			String[] kv = pair.split("=");
			element.addAttribute(new Attribute(kv[0], kv[1]));
		}
		return true;
	}

	@Override
	public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return addCache(element, introspectedTable);
	}

	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return addCache(element, introspectedTable);
	}

	@Override
	public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return addCache(element, introspectedTable);
	}

	@Override
	public boolean sqlMapSelectAllElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return addCache(element, introspectedTable);
	}

	private static final class Config {

		private List<OptionsConfigItem> items;

		@SuppressWarnings("rawtypes")
		private Config(Properties props) {

			this.items = new ArrayList<OptionsConfigItem>();

			Enumeration e = props.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				items.add(OptionsConfigItem.valueOf(key, props.getProperty(key)));
			}
		}

		private String getOptionsValue(String classFQN, String methodName) {

			for (OptionsConfigItem item : items) {
				if (item.classRegexp.matcher(classFQN).matches() && item.methodRegexp.matcher(methodName).matches())
					return item.optionsValue;
			}
			return null;

		}
	}

	private static final class OptionsConfigItem {

		private Pattern classRegexp;

		private Pattern methodRegexp;

		private String optionsValue;

		private OptionsConfigItem(Pattern classRegexp, Pattern methodRegexp, String optionsValue) {
			this.classRegexp = classRegexp;
			this.methodRegexp = methodRegexp;
			this.optionsValue = optionsValue;
		}

		public static final OptionsConfigItem valueOf(String key, String value) {

			if (key == null)
				throw new IllegalArgumentException("Property's key should be specified!");
			if (value == null)
				throw new IllegalArgumentException("Property's value should be specified!");

			if (!key.contains("#"))
				throw new IllegalArgumentException("Wrong format for property key '" + key + "' " + "found! Expected: name=\"classRegexp#methodRegexp\"");

			String classRegexp = key.substring(0, key.indexOf("#"));
			String methodRegexp = key.substring(key.indexOf("#") + 1);

			return new OptionsConfigItem(Pattern.compile(classRegexp), Pattern.compile(methodRegexp), value);

		}
	}
}
