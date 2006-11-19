package org.springframework.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import junit.framework.Assert;

/**
 * Dummy AttributesMapper for testing purposes to check that the received
 * Attributes are the expected ones.
 * 
 * @author Mattias Arthursson
 */
class AttributeCheckAttributesMapper implements AttributesMapper {
    private String[] expectedAttributes = new String[0];

    private String[] expectedValues = new String[0];;

    private String[] absentAttributes = new String[0];;

    public Object mapFromAttributes(Attributes attributes)
            throws NamingException {
        Assert.assertEquals("Values and attributes need to have the same length ",
                expectedAttributes.length, expectedValues.length);
        for (int i = 0; i < expectedAttributes.length; i++) {
            Attribute attribute = attributes.get(expectedAttributes[i]);
            Assert.assertNotNull("Attribute " + expectedAttributes[i]
                    + " was not present", attribute);
            Assert.assertEquals(expectedValues[i], attribute.get());
        }

        for (int i = 0; i < absentAttributes.length; i++) {
            Assert.assertNull(attributes.get(absentAttributes[i]));
        }

        return null;
    }

    public void setAbsentAttributes(String[] absentAttributes) {
        this.absentAttributes = absentAttributes;
    }

    public void setExpectedAttributes(String[] expectedAttributes) {
        this.expectedAttributes = expectedAttributes;
    }

    public void setExpectedValues(String[] expectedValues) {
        this.expectedValues = expectedValues;
    }
}