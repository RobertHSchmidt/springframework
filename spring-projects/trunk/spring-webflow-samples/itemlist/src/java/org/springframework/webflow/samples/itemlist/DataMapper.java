package org.springframework.webflow.samples.itemlist;

import org.springframework.binding.mapping.DefaultAttributeMapper;

public class DataMapper extends DefaultAttributeMapper {
	public DataMapper() {
		addMapping(mapping().source("requestParameters.data").target("flowScope.item").value());
	}
}