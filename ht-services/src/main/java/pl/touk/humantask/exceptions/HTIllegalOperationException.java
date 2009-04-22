/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */
package pl.touk.humantask.exceptions;

/**
 *  Invoking an operation that does not apply to the task type (e.g., invoking claim on anotification) results in an illegalOperationFault.
 *
 * @author Warren Crossing
 */

public class HTIllegalOperationException extends HumanTaskException {
    public static final long serialVersionUID = 20090414103047L;
    
    /**
     * Information about illegal operation that was invoked.
     */
    private java.lang.String illegalOperation;

    /**
     * Creates empty HTIllegalOperationException.
     */
    public HTIllegalOperationException() {
        super();
    }
    
    /**
     * Creates HTIllegalOperationException and sets exception message.
     * @param message Exception message to set
     */
    public HTIllegalOperationException(String message) {
        super(message);
    }
    
    /**
     * Creates HTIllegalOperationException and sets exception message and cause.
     * @param message Exception message to set
     * @param cause Throwable that caused current exception
     */
    public HTIllegalOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates HTIllegalOperationException and sets exception message and information about illegal operation.
     * @param message Exception message to set
     * @param illegalOperation Additional information about illegal operation that was invoked
     */
    public HTIllegalOperationException(String message, java.lang.String illegalOperation) {
        super(message);
        this.illegalOperation = illegalOperation;
    }

    /**
     * Creates HTIllegalOperationException and sets exception message and information about illegal operation.
     * @param message Exception message to set
     * @param illegalOperation Additional information about illegal operation that was invoked
     * @param cause Throwable that caused current exception
     */
    public HTIllegalOperationException(String message, java.lang.String illegalOperation, Throwable cause) {
        super(message, cause);
        this.illegalOperation = illegalOperation;
    }

    /**
     * @return Information about illegal operation that was invoked
     */
    public java.lang.String getExceptionInfo() {
        return this.illegalOperation;
    }
}
