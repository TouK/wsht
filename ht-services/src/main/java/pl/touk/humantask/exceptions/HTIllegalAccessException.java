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
    
    private java.lang.String illegalAccess;

    public HTIllegalAccessException() {
        super();
    }
    
    public HTIllegalAccessException(String message) {
        super(message);
    }
    
    public HTIllegalAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public HTIllegalAccessException(String message, java.lang.String illegalAccess) {
        super(message);
        this.illegalAccess = illegalAccess;
    }

    public HTIllegalAccessException(String message, java.lang.String illegalAccess, Throwable cause) {
        super(message, cause);
        this.illegalAccess = illegalAccess;
    }

    public java.lang.String getExceptionInfo() {
        return this.illegalAccess;
    }
}
