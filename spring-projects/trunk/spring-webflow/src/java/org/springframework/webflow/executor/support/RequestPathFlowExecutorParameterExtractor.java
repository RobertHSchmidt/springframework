package org.springframework.webflow.executor.support;

import java.io.Serializable;

import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;

/**
 * A parameter extractor that extracts necessary flow executor parameters from
 * the request path.
 * <p>
 * This allows for REST-style URLs to launch flows in the general format:
 * <code>http://${host}/${context}/${servlet}/${flowId}</code>
 * <p>
 * For example, the url
 * <code>http://localhost/springair/reservation/booking</code> would launch a
 * new execution of the <code>booking</code> flow, assuming a context path of
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
public class RequestPathFlowExecutorParameterExtractor extends FlowExecutorParameterExtractor {

	private static final String CONVERSATION_ID_PREFIX = "/_c";

	public String extractFlowId(ExternalContext context) {
		String requestPathInfo = context.getRequestPathInfo();
		if (requestPathInfo == null) {
			requestPathInfo = "";
		}
		String extractedFilename = WebUtils.extractFilenameFromUrlPath(requestPathInfo);
		return StringUtils.hasText(extractedFilename) ? extractedFilename : getDefaultFlowId();
	}

	public String createFlowUrl(String flowId, ExternalContext context) {
		return context.getDispatcherPath() + "/" + flowId;
	}

	public Serializable extractConversationId(ExternalContext context) {
		String requestPathInfo = context.getRequestPathInfo();
		if (requestPathInfo != null && requestPathInfo.startsWith(CONVERSATION_ID_PREFIX)) {
			return requestPathInfo.substring(CONVERSATION_ID_PREFIX.length());
		}
		return null;
	}

	public String createConversationUrl(Serializable conversationId, ExternalContext context) {
		return context.getDispatcherPath() + CONVERSATION_ID_PREFIX + conversationId;
	}
}