package org.springframework.webflow.executor.support;

import java.util.Iterator;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
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

	private static final char PATH_SEPARATOR_CHARACTER = '/';

	/**
	 * Flag indicating if flow input attributes should be appended to the
	 * request path on a flow redirect request, instead of being appended as
	 * standard URL query parameters.
	 */
	private boolean appendFlowInputAttributesToRequestPath = false;

	/**
	 * Returns the flag indicating if flow input attributes should be appended
	 * to the request path to build a flow redirect request, instead of being
	 * appended as standard URL query parameters.
	 */
	public boolean isAppendFlowInputAttributesToRequestPath() {
		return appendFlowInputAttributesToRequestPath;
	}

	/**
	 * Sets a flag indicating if flow input attributes should be appended to the
	 * request path to build a flow redirect request, instead of being appended
	 * as standard URL query parameters.
	 * 
	 * For example:
	 * <ul>
	 * <li>With request path appending turned on:
	 * 
	 * <pre>
	 *       /booking/12345
	 * </pre>
	 * 
	 * <li>With request path appending turned off:
	 * 
	 * <pre>
	 *       /booking?bookingId=12345
	 * </pre>
	 * 
	 * </ul>
	 * 
	 * @param appendFlowInputAttributesToRequestPath the boolean flag value
	 */
	public void setAppendFlowInputAttributesToRequestPath(boolean appendFlowInputAttributesToRequestPath) {
		this.appendFlowInputAttributesToRequestPath = appendFlowInputAttributesToRequestPath;
	}

	public boolean isFlowIdPresent(ExternalContext context) {
		String requestPathInfo = getRequestPathInfo(context);
		boolean hasFileName = StringUtils.hasText(WebUtils.extractFilenameFromUrlPath(requestPathInfo));
		return hasFileName || super.isFlowIdPresent(context);
	}

	public String extractFlowId(ExternalContext context) {
		String requestPathInfo = getRequestPathInfo(context);
		String extractedFilename = WebUtils.extractFilenameFromUrlPath(requestPathInfo);
		return StringUtils.hasText(extractedFilename) ? extractedFilename : super.extractFlowId(context);
	}

	public String createFlowUrl(FlowRedirect flowRedirect, ExternalContext context) {
		StringBuffer flowUrl = new StringBuffer();
		flowUrl.append(context.getContextPath());
		flowUrl.append(context.getDispatcherPath());
		flowUrl.append(PATH_SEPARATOR_CHARACTER);
		flowUrl.append(flowRedirect.getFlowId());
		if (appendFlowInputAttributesToRequestPath) {
			appendRequestPathElements(flowRedirect.getInput(), flowUrl);
		}
		else {
			appendQueryParameters(flowRedirect.getInput(), flowUrl);
		}
		return flowUrl.toString();
	}

	protected void appendRequestPathElements(Map map, StringBuffer url) {
		if (!map.isEmpty()) {
			url.append(PATH_SEPARATOR_CHARACTER);
			Iterator it = map.values().iterator();
			while (it.hasNext()) {
				url.append(encodeValue(it.next()));
				if (it.hasNext()) {
					url.append(PATH_SEPARATOR_CHARACTER);
				}
			}
		}
	}

	public String createFlowExecutionUrl(FlowExecutionKey key, FlowExecutionContext flowExecution, ExternalContext context) {
		StringBuffer flowExecutionUrl = new StringBuffer();
		flowExecutionUrl.append(context.getContextPath());
		flowExecutionUrl.append(context.getDispatcherPath());
		flowExecutionUrl.append(PATH_SEPARATOR_CHARACTER);
		flowExecutionUrl.append(flowExecution.getActiveSession().getFlow().getId());
		flowExecutionUrl.append('?');
		appendQueryParameter(getFlowExecutionKeyParameterName(), format(key), flowExecutionUrl);
		return flowExecutionUrl.toString();
	}

	/**
	 * Create a URL path that when redirected to renders the <i>current</i> (or
	 * last) view selection</i> made by the conversation identified by the
	 * provided conversationId. Used to support the <i>conversation redirect</i>
	 * use case.
	 * @param key the flow execution key
	 * @param context the flow execution context
	 * @return the relative conversation URL path
	 */
	public String createConversationUrl(FlowExecutionKey key, FlowExecutionContext flowExecution, ExternalContext context) {
		StringBuffer conversationUrl = new StringBuffer();
		conversationUrl.append(context.getContextPath());
		conversationUrl.append(context.getDispatcherPath());
		conversationUrl.append(PATH_SEPARATOR_CHARACTER);
		conversationUrl.append(flowExecution.getActiveSession().getFlow().getId());
		conversationUrl.append('?');
		appendQueryParameter(getConversationIdParameterName(), key.getConversationId(),
				conversationUrl);
		return conversationUrl.toString();
	}

	private String getRequestPathInfo(ExternalContext context) {
		String requestPathInfo = context.getRequestPathInfo();
		return requestPathInfo != null ? requestPathInfo : "";
	}
}