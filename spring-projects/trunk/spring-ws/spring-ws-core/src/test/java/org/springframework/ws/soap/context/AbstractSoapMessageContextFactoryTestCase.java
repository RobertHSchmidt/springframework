package org.springframework.ws.soap.context;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.transport.TransportRequest;

/**
 * @author Arjen Poutsma
 */
public abstract class AbstractSoapMessageContextFactoryTestCase extends TestCase {

    protected MessageContextFactory contextFactory;

    protected final void setUp() throws Exception {
        contextFactory = createSoapMessageContextFactory();
        if (contextFactory instanceof InitializingBean) {
            ((InitializingBean) contextFactory).afterPropertiesSet();
        }
    }

    protected abstract MessageContextFactory createSoapMessageContextFactory() throws Exception;

    protected static class MockTransportRequest implements TransportRequest {

        private Properties headers;

        private byte[] contents;

        protected MockTransportRequest(Properties headers, String fileName) throws IOException {
            this.headers = headers;
            contents = FileCopyUtils
                    .copyToByteArray(AbstractSoap11MessageContextFactoryTestCase.class.getResourceAsStream(fileName));
        }

        public Iterator getHeaderNames() {
            return headers.keySet().iterator();
        }

        public Iterator getHeaders(String name) {
            String value = headers.getProperty(name);
            return value != null ? Collections.singletonList(value).iterator() : Collections.EMPTY_LIST.iterator();
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(contents);
        }
    }
}
