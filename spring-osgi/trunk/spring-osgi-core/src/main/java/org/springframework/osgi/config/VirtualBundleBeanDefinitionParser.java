package org.springframework.osgi.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.osgi.bundle.support.PackageSpecification;
import org.springframework.osgi.bundle.support.VirtualBundleFactoryBean;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Parser for VirtualBundleFactoryBean
 *
 * @author Andy Piper
 */
class VirtualBundleBeanDefinitionParser extends AbstractBeanDefinitionParser
{
	public static final String PACKAGE_ELEMENT = "package";
	public static final String ID_NAME = "name";
	public static final String ID_VERSION = "version";

	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(VirtualBundleFactoryBean.class);
		NamedNodeMap attributes = element.getAttributes();
		for (int x = 0; x < attributes.getLength(); x++) {
			Attr attribute = (Attr) attributes.item(x);
			String name = attribute.getLocalName();

			if (ID_ATTRIBUTE.equals(name)) {
				continue;
			} else if (ParserUtils.DEPENDS_ON.equals(name)) {
				ParserUtils.parseDependsOn(attribute,  builder);
			} else {
				builder.addPropertyValue(Conventions.attributeNameToPropertyName(name), attribute.getValue());
			}
		}
		Element e = DomUtils.getChildElementByTagName(element, "exports");
		if (e != null) {
			builder.addPropertyValue("exports", extractPackageSet(e));
		}
		e = DomUtils.getChildElementByTagName(element, "imports");
		if (e != null) {
			builder.addPropertyValue("imports", extractPackageSet(e));
		}
		e = DomUtils.getChildElementByTagName(element, "dynamic-imports");
		if (e != null) {
			// builder.addPropertyValue("dynamicImports", extractPackageSet(e));
		}

		return builder.getBeanDefinition();
	}

	private Set extractPackageSet(Element e) {
		List propEles = DomUtils.getChildElementsByTagName(e, PACKAGE_ELEMENT);
		HashSet packages = new HashSet();
		for (Iterator it = propEles.iterator(); it.hasNext();) {
			Element propEle = (Element) it.next();

			NamedNodeMap attributes = propEle.getAttributes();
			PackageSpecification p = new PackageSpecification();
			for (int x = 0; x < attributes.getLength(); x++) {
				Attr attribute = (Attr) attributes.item(x);
				String name = attribute.getLocalName();
				if (ID_NAME.equals(name)) {
					p.setName(attribute.getValue());
				} else if (ID_VERSION.equals(name)) {
					p.setVersion(attribute.getValue());
				}
			}
			packages.add(p);
		}
		return packages;
	}

}
