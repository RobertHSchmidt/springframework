package org.springframework.webflow.config;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.webflow.Flow;
import org.springframework.webflow.access.FlowLocator;
import org.springframework.webflow.access.NoSuchFlowDefinitionException;
import org.springframework.webflow.access.FlowArtifactLookupException;

public class FlowRegistry implements FlowLocator, InitializingBean, BeanFactoryAware {

	private Map flowDefinitions = Collections.EMPTY_MAP;

	private Resource[] definitionLocations;

	private Resource[] definitionJarLocations;

	private Resource[] definitionDirectoryLocations;

	public void setDefinitionLocations(Resource[] locations) {
		this.definitionLocations = locations;
	}

	public void setDefinitionJarLocations(Resource[] locations) {
		this.definitionJarLocations = locations;
	}

	public void setDefinitionDirectoryLocations(Resource[] locations) {
		this.definitionDirectoryLocations = locations;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		
	}
	
	public void afterPropertiesSet() throws IOException {
		if (definitionLocations != null) {
			for (int i = 0; i < definitionLocations.length; i++) {
				loadResource(definitionLocations[i]);
			}
		}
	}

	protected void loadResource(Resource resource) {
		FlowBuilder builder = new XmlFlowBuilder(resource);
		registerFlowDefinition(builder.init());
		builder.buildStates();
		builder.dispose();
	}

	public void registerFlowDefinition(Flow flow) {
		this.flowDefinitions.put(flow.getId(), flow);
	}

	public Flow getFlow(String id) throws FlowArtifactLookupException {
		Flow flow = (Flow)flowDefinitions.get(id);
		if (flow == null) {
			throw new NoSuchFlowDefinitionException(flow.getId());
		}
		return flow;
	}
}