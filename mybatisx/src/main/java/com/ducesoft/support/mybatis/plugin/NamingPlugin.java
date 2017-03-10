/**
 * 
 */
package com.ducesoft.support.mybatis.plugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

/**
 * @author coyzeng@gmail.com
 *
 */
public class NamingPlugin extends PluginAdapter {

	private static final String dao = "Dao";
	private static final String xml = "Mapper";

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public void initialized(IntrospectedTable introspectedTable) {
		String tableName = getModelName(introspectedTable.getFullyQualifiedTableNameAtRuntime());
		String daopack = introspectedTable.getContext().getJavaClientGeneratorConfiguration().getTargetPackage();
		String modelpack = introspectedTable.getContext().getJavaModelGeneratorConfiguration().getTargetPackage();
		introspectedTable.setBaseRecordType(modelpack.concat(".").concat(tableName));
		introspectedTable.setMyBatis3JavaMapperType(daopack.concat(".").concat(tableName).concat(dao));
		introspectedTable.setMyBatis3XmlMapperFileName(tableName.concat(xml).concat(".xml"));
		super.initialized(introspectedTable);
	}

	public String getModelName(String tableName) {
		StringBuilder name = new StringBuilder();
		String[] namearray = tableName.split("_");
		for (String n : namearray) {
			if (n.equalsIgnoreCase("t")) {
				continue;
			}
			name.append(n.substring(0, 1).toUpperCase()).append(n.substring(1));
		}
		return name.toString();
	}

}
