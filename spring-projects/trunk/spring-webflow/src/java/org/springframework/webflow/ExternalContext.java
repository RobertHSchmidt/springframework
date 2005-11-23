package org.springframework.webflow;

import java.util.Map;

public interface ExternalContext {
	public Map getRequestParameterMap();
	
	public Map getRequestMap();
	
	public Map getSessionMap();
	
	public Map getApplicationMap();
}
