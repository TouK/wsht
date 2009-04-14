package pl.touk.humantask.exceptions;

/**
 *  Invoking an operation that does not apply to the task type (e.g., invoking claim on anotification) results in an illegalOperationFault.
 * 
 * @author Warren Crossing 
 */

public class IllegalOperationException extends HumanTaskException {
    public static final long serialVersionUID = 20090414103047L;
    
    private java.lang.String illegalOperation;

    public IllegalOperationException() {
        super();
    }
    
    public IllegalOperationException(String message) {
        super(message);
    }
    
    public IllegalOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalOperationException(String message, java.lang.String illegalOperation) {
        super(message);
        this.illegalOperation = illegalOperation;
    }

    public IllegalOperationException(String message, java.lang.String illegalOperation, Throwable cause) {
        super(message, cause);
        this.illegalOperation = illegalOperation;
    }

    public java.lang.String getExceptionInfo() {
        return this.illegalOperation;
    }
}
