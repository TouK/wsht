package pl.touk.humantask.exceptions;

public class RecipientNotAllowedException extends Exception {
   private java.lang.String RecipientNotAllowedException;

    public RecipientNotAllowedException() {
        super();
    }
    
    public RecipientNotAllowedException(String message) {
        super(message);
    }
    
    public RecipientNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecipientNotAllowedException(String message, java.lang.String RecipientNotAllowedException) {
        super(message);
        this.RecipientNotAllowedException = RecipientNotAllowedException;
    }

    public RecipientNotAllowedException(String message, java.lang.String RecipientNotAllowedException, Throwable cause) {
        super(message, cause);
        this.RecipientNotAllowedException = RecipientNotAllowedException;
    }

    public java.lang.String getExceptionInfo() {
        return this.RecipientNotAllowedException;
    }
}
