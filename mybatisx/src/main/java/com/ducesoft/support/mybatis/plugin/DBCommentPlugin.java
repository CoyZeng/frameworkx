package com.ducesoft.support.mybatis.plugin;

import java.util.Calendar;
import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * 生成Model数据库字段注释的Plugin.
 * 
 * @author coyzeng
 *
 */
public class DBCommentPlugin extends PluginAdapter {

	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		topLevelClass.addFileCommentLine(
				"/** Copyright (c) " + getYear() + ", ele and/or its affiliates. All rights reserved. */");
		return true;
	}

	@Override
	public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
			IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		generateFieldExplain(field, introspectedColumn);
		return true;
	}

	@Override
	public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass,
			IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		generateMethodExplain(method, introspectedColumn);
		return true;
	}

	@Override
	public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass,
			IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		generateMethodExplain(method, introspectedColumn);
		return true;
	}

	private void generateFieldExplain(Field field, IntrospectedColumn introspectedColumn) {
		String comment = (introspectedColumn.getRemarks() == null ? "" : introspectedColumn.getRemarks());
		field.addJavaDocLine("/** " + comment + " */");
	}

	private void generateMethodExplain(Method method, IntrospectedColumn introspectedColumn) {
		String comment = (introspectedColumn.getRemarks() == null ? "" : introspectedColumn.getRemarks());
		method.addJavaDocLine("/** " + comment + " */");
	}

	private String getYear() {
		return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
	}

}
