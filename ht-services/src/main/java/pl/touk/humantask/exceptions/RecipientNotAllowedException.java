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
public class RecipientNotAllowedException extends HumanTaskException {
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
