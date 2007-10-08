package org.springframework.faces.ui.resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.springframework.util.ClassUtils;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Special action for resolving and rendering resources from within a JAR file.
 * @author Jeremy Grelle
 * 
 */
public class ResolveAndRenderResourceAction implements Action {

	public Event execute(RequestContext context) throws Exception {

		String resourcePath = "META-INF" + context.getExternalContext().getRequestPath().toString();

		BufferedReader reader = new BufferedReader(new InputStreamReader(ClassUtils.getDefaultClassLoader()
				.getResourceAsStream(resourcePath)));
		PrintWriter writer = ((HttpServletResponse) context.getExternalContext().getResponse()).getWriter();

		try {
			while (reader.ready()) {
				String line = reader.readLine();
				writer.write(line);
				writer.println();
			}
		} finally {
			reader.close();
			writer.close();
		}

		return new Event(this, "success");
	}
}
