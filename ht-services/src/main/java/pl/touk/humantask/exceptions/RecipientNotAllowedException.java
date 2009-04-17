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
    
    private static final long serialVersionUID = 1L;
    
    /** 
     * Name of person that was not authorized for executed operation (which caused the exception).
     */
    private String recipientNotAllowed;

    /**
     * Creates empty RecipientNotAllowedException.
     */
    public RecipientNotAllowedException() {
        super();
    }
    
    /**
     * Creates RecipientNotAllowedException and sets exception message.
     * @param message Exception message to set
     */
    public RecipientNotAllowedException(String message) {
        super(message);
    }
    
    /**
     * Creates RecipientNotAllowedException and sets exception message and cause.
     * @param message Exception message to set
     * @param cause Throwable that caused current exception
     */
    public RecipientNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates RecipientNotAllowedException and sets exception message and information about person rejected.
     * @param message Exception message to set
     * @param recipientNotAllowed Name of person that was not authorized to perform operation
     */
    public RecipientNotAllowedException(String message, String recipientNotAllowed) {
        super(message);
        this.recipientNotAllowed = recipientNotAllowed;
    }

    /**
     * Creates RecipientNotAllowedException and sets exception message, information about person rejected and exception cause.
     * @param message Exception message to set
     * @param recipientNotAllowed Name of person that was not authorized to perform operation
     * @param cause Throwable that caused current exception
     */
    public RecipientNotAllowedException(String message, String recipientNotAllowed, Throwable cause) {
        super(message, cause);
        this.recipientNotAllowed = recipientNotAllowed;
    }

    /** 
     * @return Exception message, with name of not allowed recipient added
     */
    public String getMessage() {
        return super.getMessage() + " " + recipientNotAllowed;
    }
    
    /**
     * @return Name of person that was not authorized for executed operation (which caused the exception)
     */
    public java.lang.String getExceptionInfo() {
        return this.recipientNotAllowed;
    }
}
