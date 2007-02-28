/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.dao.exception;

public class DaoException extends RuntimeException
{
    public DaoException(String message)
    {
        super(message);
    }

    public DaoException(String message, Throwable cause)
    {
        super(message, cause);  
    }
}
