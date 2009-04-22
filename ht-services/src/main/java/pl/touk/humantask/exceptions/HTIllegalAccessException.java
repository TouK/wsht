/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */
package pl.touk.humantask.exceptions;

/**
 * 
 * By default, the identity of the person on behalf of which the operation is invoked is
 * passed to the task. When the person is not authorized to perform the operation the
 * illegalAccessFault and recipientNotAllowed is thrown in the case of tasks and
 * notifications respectively.
 * 
 * @author Warren Crossing 
 */
public class HTIllegalAccessException extends HumanTaskException {
    public static final long serialVersionUID = 20090414103047L;
    
    /**
     *  Additional information about illegal access.
     */
    private java.lang.String illegalAccess;

    /**
     * Creates empty HTIllegalAccessException.
     */
    public HTIllegalAccessException() {
        super();
    }
    
    /**
     * Creates HTIllegalAccessException and sets exception message.
     * @param message Exception message to set
     */
    public HTIllegalAccessException(String message) {
        super(message);
    }
    
    /**
     * Creates HTIllegalAccessException and sets exception message and cause.
     * @param message Exception message to set
     * @param cause Throwable that caused current exception
     */
    public HTIllegalAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates HTIllegalAccessException and sets exception message and information about illegal access.
     * @param message Exception message to set
     * @param illegalAccess Additional information about illegal access 
     */
    public HTIllegalAccessException(String message, java.lang.String illegalAccess) {
        super(message);
        this.illegalAccess = illegalAccess;
    }

    /**
     * Creates HTIllegalAccessException and sets exception message and information about illegal access.
     * @param message Exception message to set
     * @param illegalAccess Additional information about illegal access 
     * @param cause Throwable that caused current exception
     */
    public HTIllegalAccessException(String message, String illegalAccess, Throwable cause) {
        super(message, cause);
        this.illegalAccess = illegalAccess;
    }

    public String getMessage(){
        return super.getMessage() + " " + getExceptionInfo();
    }
    /**
     * @return Additional information about illegal access
     */
    public java.lang.String getExceptionInfo() {
        return this.illegalAccess;
    }
}
