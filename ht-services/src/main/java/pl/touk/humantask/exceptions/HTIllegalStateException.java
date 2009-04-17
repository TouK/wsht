
package pl.touk.humantask.exceptions;

import pl.touk.humantask.model.Task;

/**
 * Invoking an operation that is not allowed in the current state of the
 * task results in an illegalStateFault.
 *
 * @author Warren Crossing 
 */
public class HTIllegalStateException extends HumanTaskException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Current task state.
     */
    private Task.Status illegalState;

    /**
     * Creates empty HTIllegalStateException.
     */
    public HTIllegalStateException() {
        super();
    }
    
    /**
     * Creates HTIllegalStateException and sets exception message.
     * @param message Exception message to set
     */
    public HTIllegalStateException(String message) {
        super(message);
    }
    
    /**
     * Creates HTIllegalStateException and sets exception message and cause.
     * @param message Exception message to set
     * @param cause Throwable that caused current exception
     */
    public HTIllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates HTIllegalStateException and sets exception message and information about illegal task state.
     * @param message Exception message to set
     * @param illegalState Current state of the task
     */
    public HTIllegalStateException(String message, Task.Status illegalState) {
        super(message);
        this.illegalState = illegalState;
    }

    /**
     * Creates HTIllegalStateException and sets exception message and information about illegal task state.
     * @param message Exception message to set
     * @param illegalState Current state of the task
     * @param cause Throwable that caused current exception
     */
    public HTIllegalStateException(String message, Task.Status illegalState, Throwable cause) {
        super(message, cause);
        this.illegalState = illegalState;
    }

    /** 
     * @return Exception message, with name of illegal task state
     */
    public String getMessage() {
        return super.getMessage() + " " + illegalState;
    }
    
    /**
     * @return Current task state
     */
    public Task.Status getExceptionInfo() {
        return this.illegalState;
    }
}