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
public class JobAlreadyKilledException extends Exception {

    public JobAlreadyKilledException() {
    }

    public JobAlreadyKilledException(String msg) {
        super(msg);
    }
}
