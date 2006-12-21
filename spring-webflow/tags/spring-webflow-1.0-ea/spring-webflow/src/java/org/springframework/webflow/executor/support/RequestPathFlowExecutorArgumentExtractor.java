package org.springframework.webflow.executor.support;

import java.io.Serializable;
import java.util.Iterator;

import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.support.FlowRedirect;

/**
 * Extracts flow executor arguments from the request path.
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
public class RequestPathFlowExecutorArgumentExtractor extends FlowExecutorArgumentExtractor {

	private static final String CONVERSATION_ID_PREFIX = "/_c";

	public String extractFlowId(ExternalContext context) {
		String requestPathInfo = context.getRequestPathInfo();
		if (requestPathInfo == null) {
			requestPathInfo = "";
		}
		String extractedFilename = WebUtils.extractFilenameFromUrlPath(requestPathInfo);
		return StringUtils.hasText(extractedFilename) ? extractedFilename : super.extractFlowId(context);
	}

	public Serializable extractConversationId(ExternalContext context) {
		String requestPathInfo = context.getRequestPathInfo();
		if (requestPathInfo != null && requestPathInfo.startsWith(CONVERSATION_ID_PREFIX)) {
			return requestPathInfo.substring(CONVERSATION_ID_PREFIX.length());
		} else {
			return super.extractConversationId(context);
		}
	}

	public String createFlowUrl(FlowRedirect flowRedirect, ExternalContext context) {
		StringBuffer flowUrl = new StringBuffer();
		flowUrl.append(context.getDispatcherPath());
		flowUrl.append('/');
		flowUrl.append(flowRedirect.getFlowId());
		if (!flowRedirect.getInput().isEmpty()) {
			flowUrl.append('/');
			Iterator it = flowRedirect.getInput().values().iterator();
			while (it.hasNext()) {
				flowUrl.append(encodeValue(it.next()));
				if (it.hasNext()) {
					flowUrl.append('/');
				}
			}
		}
		return flowUrl.toString();
	}

	public String createConversationUrl(Serializable conversationId, ExternalContext context) {
		return context.getDispatcherPath() + CONVERSATION_ID_PREFIX + conversationId;
	}
	
}