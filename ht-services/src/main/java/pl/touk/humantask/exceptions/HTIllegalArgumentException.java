/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */
package pl.touk.humantask.exceptions;

/**
 * An operation takes a well-defined set of parameters as its input. Passing an illegal
 * parameter or an illegal number of parameters results in the illegalArgumentFault
 * being thrown.
 *
 * @author Warren Crossing 
 */
public class HTIllegalArgumentException extends HTException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Information about illegal argument that was given.
     */
    private java.lang.String illegalArgument;

    /**
     * Creates empty HTIllegalArgumentException.
     */
    public HTIllegalArgumentException() {
        super();
    }
    
    /**
     * Creates HTIllegalArgumentException and sets exception message.
     * @param message Exception message to set
     */
    public HTIllegalArgumentException(String message) {
        super(message);
    }
    
    /**
     * Creates HTIllegalArgumentException and sets exception message and cause.
     * @param message Exception message to set
     * @param cause Throwable that caused current exception
     */
    public HTIllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates HTIllegalArgumentException and sets exception message and information about illegal argument given.
     * @param message Exception message to set
     * @param illegalArgument Additional information about illegal argument given 
     */
    public HTIllegalArgumentException(String message, String illegalArgument) {
        super(message);
        this.illegalArgument = illegalArgument;
    }

    /**
     * Creates HTIllegalArgumentException and sets exception message and information about illegal argument given.
     * @param message Exception message to set
     * @param illegalArgument Additional information about illegal argument given 
     * @param cause Throwable that caused current exception
     */
    public HTIllegalArgumentException(String message, String illegalArgument, Throwable cause) {
        super(message, cause);
        this.illegalArgument = illegalArgument;
    }

    /**
     * @return Information about illegal argument that was given
     */
    public java.lang.String getExceptionInfo() {
        return this.illegalArgument;
    }
}
