package org.yk.nextail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NextailException extends RuntimeException {
    private static final Logger LOG = LoggerFactory.getLogger(NextailException.class);
    // TODO: implement custom exception handling

    public NextailException() {
    }

    public NextailException(String message) {
        super(message);
        LOG.error(message);
    }

    public NextailException(String message, Throwable cause) {
        super(message, cause);
        LOG.error(message);
    }

    public NextailException(Throwable cause) {
        super(cause);
    }

    public NextailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
