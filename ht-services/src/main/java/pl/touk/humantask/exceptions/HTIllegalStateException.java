
package pl.touk.humantask.exceptions;

import pl.touk.humantask.model.Task;

/**
 * Invoking an operation that is not allowed in the current state of the
 * task results in an illegalStateFault.
 *
 * @author Warren Crossing 
 */
public class HTIllegalStateException extends HumanTaskException {
    
    private Task.Status illegalState;

    public HTIllegalStateException() {
        super();
    }
    
    public HTIllegalStateException(String message) {
        super(message);
    }
    
    public HTIllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public HTIllegalStateException(String message, Task.Status illegalState) {
        super(message);
        this.illegalState = illegalState;
    }

    public HTIllegalStateException(String message, Task.Status illegalState, Throwable cause) {
        super(message, cause);
        this.illegalState = illegalState;
    }

    public String getMessage() {
        return super.getMessage() + " " + illegalState;
    }
    
    public Task.Status getExceptionInfo() {
        return this.illegalState;
    }
}