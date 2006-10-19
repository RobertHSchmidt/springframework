package org.springframework.ldap.support.parser;

import java.io.StringReader;

/**
 * 
 * @author Mattias Arthursson
 * 
 */
public class DefaultDnParserFactory {
    public static DnParser createDnParser(String string) {
        return new DnParserImpl(new StringReader(string));
    }
}
