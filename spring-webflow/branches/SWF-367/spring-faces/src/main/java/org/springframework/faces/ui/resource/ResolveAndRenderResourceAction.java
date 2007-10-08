package org.springframework.faces.ui.resource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

public class ResolveAndRenderResourceAction implements Action {

	public Event execute(RequestContext context) throws Exception {

		StringBuffer resourcePath = new StringBuffer("classpath:META-INF");
		for (String element : context.getExternalContext().getRequestPath().getElements()) {
			resourcePath.append("/" + element);
		}

		ResourcePatternResolver resolver = new ServletContextResourcePatternResolver((ServletContext) context
				.getExternalContext().getContext());

		Resource resource = resolver.getResource(resourcePath.toString());

		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		OutputStream out = ((HttpServletResponse) context.getExternalContext().getResponse()).getOutputStream();
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));

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
