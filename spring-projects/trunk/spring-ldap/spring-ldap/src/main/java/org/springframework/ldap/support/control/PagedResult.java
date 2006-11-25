package org.springframework.ldap.support.control;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Bean to encapsulate a result List and a {@link PagedResultsCookie} to use for
 * returning the results when using {@link PagedResultsRequestControl}.
 */
public class PagedResult {

    private List resultList;

    private PagedResultsCookie cookie;

    /**
     * Constructs a PagedResults using the supplied List and
     * {@link PagedResultsCookie}.
     * 
     * @param resultList
     *            the result list.
     * @param cookie
     *            the cookie.
     */
    public PagedResult(List resultList, PagedResultsCookie cookie) {
        this.resultList = resultList;
        this.cookie = cookie;
    }

    /**
     * Get the cookie.
     * 
     * @return the cookie.
     */
    public PagedResultsCookie getCookie() {
        return cookie;
    }

    /**
     * Get the result list.
     * 
     * @return the result list.
     */
    public List getResultList() {
        return resultList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj != null && PagedResult.class.equals(obj.getClass())) {
            PagedResult that = (PagedResult) obj;
            return new EqualsBuilder().append(this.resultList, that.resultList)
                    .append(this.cookie, that.cookie).isEquals();
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return new HashCodeBuilder().append(this.resultList)
                .append(this.cookie).toHashCode();
    }
}
