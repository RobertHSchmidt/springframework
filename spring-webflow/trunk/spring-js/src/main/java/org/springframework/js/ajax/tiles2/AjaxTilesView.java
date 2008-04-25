package org.springframework.js.ajax.tiles2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tiles.Attribute;
import org.apache.tiles.Definition;
import org.apache.tiles.Attribute.AttributeType;
import org.apache.tiles.access.TilesAccess;
import org.apache.tiles.context.TilesRequestContext;
import org.apache.tiles.impl.BasicTilesContainer;
import org.springframework.js.ajax.AjaxHandler;
import org.springframework.js.ajax.SpringJavascriptAjaxHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.JstlUtils;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.tiles2.TilesView;

public class AjaxTilesView extends TilesView {

	private static final String FRAGMENTS_PARAM = "fragments";

	private AjaxHandler ajaxHandler = new SpringJavascriptAjaxHandler();

	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		ServletContext servletContext = getServletContext();
		if (ajaxHandler.isAjaxRequest(servletContext, request, response)) {
			BasicTilesContainer container = (BasicTilesContainer) TilesAccess.getContainer(servletContext);
			if (container == null) {
				throw new ServletException("Tiles container is not initialized. "
						+ "Have you added a TilesConfigurer to your web application context?");
			}

			exposeModelAsRequestAttributes(model, request);
			JstlUtils.exposeLocalizationContext(new RequestContext(request, servletContext));

			TilesRequestContext tilesRequestContext = container.getContextFactory().createRequestContext(
					container.getApplicationContext(), new Object[] { request, response });
			Definition compositeDefinition = container.getDefinitionsFactory().getDefinition(getUrl(),
					tilesRequestContext);
			Map flattenedAttributeMap = new HashMap();
			flattenAttributeMap(container, tilesRequestContext, flattenedAttributeMap, compositeDefinition);

			String[] attrNames = getRenderFragments(model, request, response);
			response.flushBuffer();
			for (int i = 0; i < attrNames.length; i++) {
				Attribute attributeToRender = (Attribute) flattenedAttributeMap.get(attrNames[i]);
				container.render(attributeToRender, response.getWriter(), new Object[] { request, response });
			}
		} else {
			super.renderMergedOutputModel(model, request, response);
		}
	}

	protected String[] getRenderFragments(Map model, HttpServletRequest request, HttpServletResponse response) {
		String attrName = request.getParameter(FRAGMENTS_PARAM);
		return StringUtils.commaDelimitedListToStringArray(attrName);
	}

	private void flattenAttributeMap(BasicTilesContainer container, TilesRequestContext requestContext, Map resultMap,
			Definition compositeDefinition) throws Exception {
		Iterator i = compositeDefinition.getAttributes().keySet().iterator();
		while (i.hasNext()) {
			Object key = i.next();
			Attribute attr = (Attribute) compositeDefinition.getAttributes().get(key);
			if (attr.getType() == AttributeType.DEFINITION) {
				Definition nestedDefinition = container.getDefinitionsFactory().getDefinition(
						attr.getValue().toString(), requestContext);
				flattenAttributeMap(container, requestContext, resultMap, nestedDefinition);
			} else {
				resultMap.put(key, attr);
			}
		}
	}

}
