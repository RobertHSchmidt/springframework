/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.filter;

/**
 * A filter to compare <=. LDAP RFC does not allow < comparison. The following
 * code:
 * 
 * <pre>
 * LessThanOrEqualsFilter filter = new LessThanOrEqualsFilter(&quot;cn&quot;, &quot;Some CN&quot;);
 * System.out.println(filter.ecode());
 * </pre>
 * 
 * would result in:
 * 
 * <pre>
 * (cn&lt;=Some CN)
 * </pre>
 * 
 * @author Mattias Arthursson
 */
public class LessThanOrEqualsFilter extends CompareFilter {

    private static final String LESS_THAN_OR_EQUALS = "<=";

    public LessThanOrEqualsFilter(String attribute, String value) {
        super(attribute, value);
    }

    public LessThanOrEqualsFilter(String attribute, int value) {
        super(attribute, value);
    }

    protected String getCompareString() {
        return LESS_THAN_OR_EQUALS;
    }
}
