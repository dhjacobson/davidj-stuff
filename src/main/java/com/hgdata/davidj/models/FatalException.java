/*
 * Author: Alex Flury
 * Date: 10/09/2015
 * Copyright HG Data 2015
 * www.hgdata.com
 */

package com.hgdata.davidj.models;

/**
 * An exception for fatal errors that should cause the application to terminate.
 */
public class FatalException extends Exception {

    /**
     * @param cause the cause of the fatal error
     */
    public FatalException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message a string explaning the cause of the fatal error
     */
    public FatalException(String message) {
        super(message);
    }

    /**
     * @param message a string explaining the cause of the fatal error
     * @param cause   a {@code Throwable} object that caused the fatal error
     */
    public FatalException(String message, Throwable cause) {
        super(message, cause);
    }

}
