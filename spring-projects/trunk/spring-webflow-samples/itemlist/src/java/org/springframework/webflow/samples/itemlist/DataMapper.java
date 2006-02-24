package org.springframework.webflow.samples.itemlist;

import org.springframework.binding.mapping.DefaultAttributeMapper;
import org.springframework.binding.mapping.MappingBuilder;
import org.springframework.webflow.util.ExpressionUtils;

public class DataMapper extends DefaultAttributeMapper {
	public DataMapper() {
		MappingBuilder mapping = new MappingBuilder(ExpressionUtils.getDefaultExpressionParser());
		addMapping(mapping.source("requestParameters.data").target("flowScope.item").value());
	}
}