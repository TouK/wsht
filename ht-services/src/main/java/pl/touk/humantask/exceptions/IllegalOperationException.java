package pl.touk.humantask.exceptions;


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
