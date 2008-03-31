package org.springframework.faces.ui;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;

import org.springframework.faces.ui.resource.ResourceHelper;

public class DojoStyleRenderer extends Renderer {

	private static final String dijitThemePath = "/dijit/themes/";

	private static final String dijitTheme = "tundra";

	private static final ResourceHelper resourceHelper = new ResourceHelper();

	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

		resourceHelper.renderStyleLink(context, dijitThemePath + dijitTheme + "/" + dijitTheme + ".css");
	}
}
