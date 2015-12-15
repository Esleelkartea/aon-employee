package com.code.aon.common.event;

import java.io.Serializable;

/**
 * An "ManagerBean" event gets fired whenever an operation is performed  
 * over a bean. You can register a IManagerBeanVetoListener with a source
 * bean so as to be notified of any bound operations.
 * 
 * @author 	Consulting & Development.
 *
 */

public interface IManagerBeanVetoListener extends Serializable {

	/**
     * This method gets called before a bean is inserted.
     * 
     * @param evt A ManagerBeanEvent object describing the event source.
	 * @throws ManagerBeanVetoListenerException
	 */
	void vetoableBeanInserted(ManagerBeanEvent evt) throws ManagerBeanVetoListenerException;

	/**
     * This method gets called before a bean is updated.
     * 
     * @param evt A ManagerBeanEvent object describing the event source.
	 * @throws ManagerBeanVetoListenerException
	 */
	void vetoableBeanUpdated(ManagerBeanEvent evt) throws ManagerBeanVetoListenerException;

	/**
     * This method gets called before a bean is removed.
     * 
     * @param evt A ManagerBeanEvent object describing the event source.
	 * @throws ManagerBeanVetoListenerException
	 */
	void vetoableBeanRemoved(ManagerBeanEvent evt) throws ManagerBeanVetoListenerException;
}
