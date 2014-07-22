/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Exceptions;

import javax.ejb.ApplicationException;

/**
 *
 * @author nico
 */
@ApplicationException(rollback = false)
public class JobAlreadyRunningException extends Exception {

    public JobAlreadyRunningException() {
    }

    public JobAlreadyRunningException(String msg) {
        super(msg);
    }
}
