package org.springframework.webflow.manager.support;

import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;
import org.springframework.webflow.ExternalContext;

public class RequestPathFlowExecutionManagerParameterExtractor extends FlowExecutionManagerParameterExtractor {
	public String extractFlowId(ExternalContext context) {
		String pathInfo  = context.getRequestPathInfo();
		if (!StringUtils.hasText(pathInfo)) {
			return getDefaultFlowId();
		}
		return WebUtils.extractFilenameFromUrlPath(pathInfo);
	}
}
