package org.springframework.ldap.support;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

import junit.framework.TestCase;

/**
 * Unit tests that serve as regression tests for bugs that have been fixed.
 * 
 * @author Luke Taylor
 */
public class DirContextAdapterBugTest extends TestCase {

    public void testResetAttributeValuesNotReportedAsModifications() {
        BasicAttributes attrs = new BasicAttributes("myattr", "a");
        attrs.get("myattr").add("b");
        attrs.get("myattr").add("c");
        UpdateAdapter ctx = new UpdateAdapter(attrs, new DistinguishedName());

        ctx.setAttributeValues("myattr", new String[] { "a", "b" });
        ctx.setAttributeValues("myattr", new String[] { "a", "b", "c" });

        assertEquals(0, ctx.getModificationItems().length);
    }

    /**
     * This test starts with an array with a null value in it (because that's
     * how BasicAttributes will do it), changes to <code>[a]</code>, and then
     * changes to <code>null</code>. The current code interprets this as a
     * change and will replace the original array with an empty array.
     * 
     * TODO Is this correct behaviour?
     */
    public void testResetNullAttributeValuesReportedAsModifications() {
        BasicAttributes attrs = new BasicAttributes("myattr", null);
        UpdateAdapter ctx = new UpdateAdapter(attrs, new DistinguishedName());

        ctx.setAttributeValues("myattr", new String[] { "a" });
        ctx.setAttributeValues("myattr", null);

        assertEquals(1, ctx.getModificationItems().length);
    }

    public void testResetNullAttributeValueNotReportedAsModification() throws Exception {
        BasicAttributes attrs = new BasicAttributes("myattr", "b");
        UpdateAdapter ctx = new UpdateAdapter(attrs, new DistinguishedName());

        ctx.setAttributeValue("myattr", "a");
        ctx.setAttributeValue("myattr", "b");

        assertEquals(0, ctx.getModificationItems().length);
    }

    private static class UpdateAdapter extends DirContextAdapter {
        public UpdateAdapter(Attributes attrs, Name dn) {
            super(attrs, dn);
            setUpdateMode(true);
        }
    }
}
