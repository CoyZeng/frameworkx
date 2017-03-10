package com.ducesoft.support.mybatis.plugin;

import java.lang.reflect.Field;
import java.util.List;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

/**
 * 
 * @author coyzeng
 *
 */
public class XmlFileOverwritePlugin extends PluginAdapter {

	@Override
	public boolean sqlMapGenerated(GeneratedXmlFile sqlMapFile, IntrospectedTable introspectedTable) {
		String overwrite = context.getProperty("overwrite");
		if ("true".equals(overwrite)) {
			// System.out.println("设置xml为overwrite:" +
			// sqlMapFile.getFileName());
			try {
				Field mergedField = GeneratedXmlFile.class.getDeclaredField("isMergeable");
				mergedField.setAccessible(true);
				mergedField.setBoolean(sqlMapFile, false);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return super.sqlMapGenerated(sqlMapFile, introspectedTable);
	}

	public boolean validate(List<String> warnings) {
		return true;
	}
}
