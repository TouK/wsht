package pl.touk.humantask.exceptions;


public class IllegalArgumentException extends HumanTaskException {
    
    private java.lang.String illegalArgument;

    public IllegalArgumentException() {
        super();
    }
    
    public IllegalArgumentException(String message) {
        super(message);
    }
    
    public IllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalArgumentException(String message, java.lang.String illegalArgument) {
        super(message);
        this.illegalArgument = illegalArgument;
    }

    public IllegalArgumentException(String message, java.lang.String illegalArgument, Throwable cause) {
        super(message, cause);
        this.illegalArgument = illegalArgument;
    }

    public java.lang.String getExceptionInfo() {
        return this.illegalArgument;
    }
}
