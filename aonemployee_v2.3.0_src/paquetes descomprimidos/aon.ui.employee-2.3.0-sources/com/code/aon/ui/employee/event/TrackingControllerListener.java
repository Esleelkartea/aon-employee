/**
 * 
 */
package com.code.aon.ui.employee.event;

import com.code.aon.company.resources.Employee;
import com.code.aon.registry.RegistryNote;
import com.code.aon.registry.enumeration.NoteType;
import com.code.aon.ui.form.IController;
import com.code.aon.ui.form.event.ControllerAdapter;
import com.code.aon.ui.form.event.ControllerEvent;
import com.code.aon.ui.form.event.ControllerListenerException;
import com.code.aon.ui.util.AonUtil;
import com.code.aon.ui.employee.controller.EmployeeController;

/**
 * The listener class for receiving tracking events. 
 * 
 * @author Consulting & Development. I�aki Ayerbe - 24/08/2007
 *
 */
public class TrackingControllerListener extends ControllerAdapter {

	private static final long serialVersionUID = -4815046216687687708L;

	@Override
	public void beforeBeanAdded(ControllerEvent event) throws ControllerListenerException {
		EmployeeController ec = 
			(EmployeeController) AonUtil.getController( EmployeeController.MANAGER_BEAN_NAME );
		Employee employee = (Employee) ec.getTo();
		IController tracking = event.getController();
		RegistryNote rnote = (RegistryNote) tracking.getTo();
		rnote.setRegistry( employee.getRegistry() );
		rnote.setNotetype( NoteType.TRACKING );
	}

}
