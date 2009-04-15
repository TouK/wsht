package pl.touk.humantask.exceptions;

/**
 *  Invoking an operation that does not apply to the task type (e.g., invoking claim on anotification) results in an illegalOperationFault.
 * 
 * @author Warren Crossing 
 */

public class HTIllegalOperationException extends HumanTaskException {
    public static final long serialVersionUID = 20090414103047L;
    
    private java.lang.String illegalOperation;

    public HTIllegalOperationException() {
        super();
    }
    
    public HTIllegalOperationException(String message) {
        super(message);
    }
    
    public HTIllegalOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public HTIllegalOperationException(String message, java.lang.String illegalOperation) {
        super(message);
        this.illegalOperation = illegalOperation;
    }

    public HTIllegalOperationException(String message, java.lang.String illegalOperation, Throwable cause) {
        super(message, cause);
        this.illegalOperation = illegalOperation;
    }

    public java.lang.String getExceptionInfo() {
        return this.illegalOperation;
    }
}
