/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask;

/**
 * Human Task lifecycle exception.
 * 
 * @author Witek Wołejszo
 */
public class HumanTaskException extends Exception {

    public HumanTaskException() {
        super();
    }

    public HumanTaskException(String message, Throwable cause) {
        super(message, cause);
    }

    public HumanTaskException(String message) {
        super(message);
    }

    public HumanTaskException(Throwable cause) {
        super(cause);
    }

}
