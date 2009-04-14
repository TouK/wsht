package pl.touk.humantask.exceptions;

public class IllegalAccessException extends HumanTaskException {
    public static final long serialVersionUID = 20090414103047L;
    
    private java.lang.String illegalAccess;

    public IllegalAccessException() {
        super();
    }
    
    public IllegalAccessException(String message) {
        super(message);
    }
    
    public IllegalAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalAccessException(String message, java.lang.String illegalAccess) {
        super(message);
        this.illegalAccess = illegalAccess;
    }

    public IllegalAccessException(String message, java.lang.String illegalAccess, Throwable cause) {
        super(message, cause);
        this.illegalAccess = illegalAccess;
    }

    public java.lang.String getExceptionInfo() {
        return this.illegalAccess;
    }
}
