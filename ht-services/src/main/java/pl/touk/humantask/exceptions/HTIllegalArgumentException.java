package pl.touk.humantask.exceptions;

/*
 * An operation takes a well-defined set of parameters as its input. Passing an illegal
 * parameter or an illegal number of parameters results in the illegalArgumentFault
 * being thrown.
 * @author Warren Crossing 
 */
public class HTIllegalArgumentException extends HumanTaskException {
    
    private java.lang.String illegalArgument;

    public HTIllegalArgumentException() {
        super();
    }
    
    public HTIllegalArgumentException(String message) {
        super(message);
    }
    
    public HTIllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public HTIllegalArgumentException(String message, java.lang.String illegalArgument) {
        super(message);
        this.illegalArgument = illegalArgument;
    }

    public HTIllegalArgumentException(String message, java.lang.String illegalArgument, Throwable cause) {
        super(message, cause);
        this.illegalArgument = illegalArgument;
    }

    public java.lang.String getExceptionInfo() {
        return this.illegalArgument;
    }
}
