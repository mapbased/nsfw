/**
 *
 */
package com.mapkc.nsfw.model;

/**
 * @author chy
 */
public class IntegrityConstraintViolationException extends RuntimeException {


    /**
     * @param message
     */
    public IntegrityConstraintViolationException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public IntegrityConstraintViolationException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public IntegrityConstraintViolationException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }


}
