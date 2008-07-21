package org.springframework.batch.item.file;

import java.util.Comparator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.springframework.batch.item.CommonItemStreamItemReaderTests;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.sample.Foo;
import org.springframework.batch.item.xml.EventReaderDeserializer;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class MultiResourceItemReaderXmlTests extends CommonItemStreamItemReaderTests {

	protected ItemReader<Foo> getItemReader() throws Exception {
		MultiResourceItemReader<Foo> multiReader = new MultiResourceItemReader<Foo>();

		StaxEventItemReader<Foo> reader = new StaxEventItemReader<Foo>();

		reader.setFragmentRootElementName("foo");
		reader.setFragmentDeserializer(new EventReaderDeserializer<Foo>() {
			public Foo deserializeFragment(XMLEventReader eventReader) {
				Attribute attr;
				try {
					assertTrue(eventReader.nextEvent().isStartDocument());
					StartElement event = eventReader.nextEvent().asStartElement();
					attr = (Attribute) event.getAttributes().next();
				}
				catch (XMLStreamException e) {
					throw new RuntimeException(e);
				}
				Foo foo = new Foo();
				foo.setValue(Integer.parseInt(attr.getValue()));
				return foo;
			}
		});

		reader.setSaveState(true);

		Resource r1 = new ByteArrayResource("<foos> <foo value=\"1\"/> <foo value=\"2\"/> </foos>".getBytes());
		Resource r2 = new ByteArrayResource("<foos> </foos>".getBytes());
		Resource r3 = new ByteArrayResource("<foos> <foo value=\"3\"/> </foos>".getBytes());
		Resource r4 = new ByteArrayResource("<foos> <foo value=\"4\"/> <foo value=\"5\"/> </foos>".getBytes());

		multiReader.setDelegate(reader);
		multiReader.setResources(new Resource[] { r1, r2, r3, r4 });
		multiReader.setSaveState(true);
		multiReader.setComparator(new Comparator<Resource>() {
			public int compare(Resource arg0, Resource arg1) {
				return 0; // preserve original ordering
			}
		});

		return multiReader;
	}
	
	protected void pointToEmptyInput(ItemReader<Foo> tested) throws Exception {
		MultiResourceItemReader<Foo> multiReader = (MultiResourceItemReader<Foo>) tested;
		multiReader.close(new ExecutionContext());
		multiReader.setResources(new Resource[] { new ByteArrayResource("<foos />"
				.getBytes()) });
		multiReader.open(new ExecutionContext());
	}

}
