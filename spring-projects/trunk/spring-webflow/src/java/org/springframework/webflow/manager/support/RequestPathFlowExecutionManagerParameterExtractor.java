package org.springframework.webflow.manager.support;

import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;

/**
 * A parameter extractor that extracts necessary flow execution management
 * parameters from the request path.
 * <p>
 * This allows for REST-style URLs to launch flows in the general format:
 * <code>http://${host}/${context}/${servlet}/${flowId}</code>
 * <p>
 * For example, the url
 * <code>http://localhost/springair/reservation/booking</code> would launch
 * the flow <code>booking</code>, assuming a context path of
 * <code>/springair</code> and a servlet mapping of
 * <code>/reservation/*</code>.
 * <p>
 * Note: this implementation only works with <code>ExternalContext</code>
 * implementations that return a valid
 * {@link ExternalContext#getRequestPathInfo()} such as the
 * {@link ServletExternalContext}.
 * 
 * @author Keith Donald
 */
public class RequestPathFlowExecutionManagerParameterExtractor extends FlowExecutionManagerParameterExtractor {
	public String extractFlowId(ExternalContext context) {
		String pathInfo = context.getRequestPathInfo();
		if (!StringUtils.hasText(pathInfo)) {
			return getDefaultFlowId();
		}
		return WebUtils.extractFilenameFromUrlPath(pathInfo);
	}
}