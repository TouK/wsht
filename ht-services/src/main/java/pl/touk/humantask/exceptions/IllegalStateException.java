
package pl.touk.humantask.exceptions;

import pl.touk.humantask.model.Task;

public class IllegalStateException extends HumanTaskException {
    
    private Task.Status illegalState;

    public IllegalStateException() {
        super();
    }
    
    public IllegalStateException(String message) {
        super(message);
    }
    
    public IllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalStateException(String message, Task.Status illegalState) {
        super(message);
        this.illegalState = illegalState;
    }

    public IllegalStateException(String message, Task.Status illegalState, Throwable cause) {
        super(message, cause);
        this.illegalState = illegalState;
    }

    public Task.Status getExceptionInfo() {
        return this.illegalState;
    }
}