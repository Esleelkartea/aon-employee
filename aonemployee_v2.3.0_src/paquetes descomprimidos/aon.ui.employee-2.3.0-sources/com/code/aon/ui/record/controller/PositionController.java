package com.code.aon.ui.record.controller;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import com.code.aon.common.BeanManager;
import com.code.aon.common.IManagerBean;
import com.code.aon.common.ManagerBeanException;
import com.code.aon.company.dao.ICompanyAlias;
import com.code.aon.company.resources.Employee;
import com.code.aon.ql.Criteria;
import com.code.aon.record.Contract;
import com.code.aon.record.LHCourse;
import com.code.aon.record.Work;
import com.code.aon.record.dao.IRecordAlias;
import com.code.aon.registry.Registry;
import com.code.aon.ui.company.controller.CompanyUtil;
import com.code.aon.ui.employee.controller.EmployeeController;
import com.code.aon.ui.form.BasicController;
import com.code.aon.ui.util.AonUtil;

public class PositionController extends BasicController {
	
	private static final long serialVersionUID = 7098665416912294857L;

	public static final String MANAGER_BEAN_NAME = "position";

	private Employee employee;
	
    private List<SelectItem> activities;

    private Logger LOGGER = Logger.getLogger(PositionController.class.getName());
	
	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

    /**
	 * @return the activities
	 */
	public List<SelectItem> getWorkActivities() {
		return this.activities;
	}

	/* (non-Javadoc)
	 * @see com.code.aon.ui.form.BasicController#onReset(javax.faces.event.ActionEvent)
	 */
	@Override
	public void onReset(ActionEvent event) {
		super.onReset(event);
		EmployeeController ec = 
			(EmployeeController) AonUtil.getController( EmployeeController.MANAGER_BEAN_NAME );
		if ( ec.getResource() != null ) {
			Integer workPlaceId = ec.getResource().getWorkPlace().getId();
			try {
				// Find working place active activities, otherwise inactive.
				int active = ( ec.getResource().getWorkPlace().isActive() )? 1: -1;
				this.activities = CompanyUtil.findActivities( workPlaceId, active );
			} catch (ManagerBeanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void employeeChanged(ValueChangeEvent event) throws ManagerBeanException {
		if(event.getNewValue() != null){
			this.setEmployee(obtainEmployee(event.getNewValue()));
		}
		if(this.getEmployee().getId() != null){
			Criteria criteria = new Criteria();
			criteria.addEqualExpression(getManagerBean().getFieldName(IRecordAlias.POSITION_EMPLOYEE_ID), getEmployee().getId());
			criteria.addOrder( getManagerBean().getFieldName(IRecordAlias.POSITION_STARTING_DATE) );
			setCriteria(criteria);
			this.onSearch(null);
		}
	}

	public void workingPlaceChanged(ValueChangeEvent event) throws ManagerBeanException {
		Integer workPlaceId = (Integer) event.getNewValue();
		// Find working place active and inactive activities.
		this.activities = CompanyUtil.findActivities( workPlaceId, 0 );
	}

	@SuppressWarnings("unchecked")
	private Employee obtainEmployee(Object value) {
		Employee employee;
		try {
			IManagerBean employeeBean = BeanManager.getManagerBean(Employee.class);
			Criteria criteria = new Criteria();
			String identifier = employeeBean.getFieldName( ICompanyAlias.EMPLOYEE_ID );
			criteria.addEqualExpression( identifier, value );
			Iterator iter = employeeBean.getList(criteria).iterator();
			if(iter.hasNext()){
				employee = (Employee)iter.next();
				return employee;
			}
		} catch (ManagerBeanException e) {
			LOGGER.log(Level.SEVERE, "Error obtaining employee with id= " + value, e);
		}
		employee = new Employee();
		employee.setRegistry(new Registry());
		return employee;
	}
	
	@SuppressWarnings("unused")
	public void onCourse(ActionEvent event) throws ManagerBeanException{
		CourseController courseController = 
			(CourseController) AonUtil.getController( CourseController.MANAGER_BEAN_NAME );
		courseController.setEmployee(getEmployee());
		if(getEmployee().getId() != null){
			IManagerBean courseBean = BeanManager.getManagerBean(LHCourse.class);
			Criteria criteria = new Criteria();
			criteria.addEqualExpression(courseBean.getFieldName(IRecordAlias.LHCOURSE_EMPLOYEE_ID), getEmployee().getId());
			courseController.setCriteria(criteria);
			courseController.onSearch(null);
		}
	}
	
	@SuppressWarnings("unused")
	public void onWork(ActionEvent event) throws ManagerBeanException{
		WorkController workController = 
			(WorkController) AonUtil.getController( WorkController.MANAGER_BEAN_NAME );
		workController.setEmployee(getEmployee());
		if(getEmployee().getId() != null){
			IManagerBean workBean = BeanManager.getManagerBean(Work.class);
			Criteria criteria = new Criteria();
			criteria.addEqualExpression(workBean.getFieldName(IRecordAlias.WORK_EMPLOYEE_ID), getEmployee().getId());
			workController.setCriteria(criteria);
			workController.onSearch(null);
		}
	}
	
	@SuppressWarnings("unused")
	public void onContract(ActionEvent event) throws ManagerBeanException{
		ContractController contractController = 
			(ContractController) AonUtil.getController( ContractController.MANAGER_BEAN_NAME );
		contractController.setEmployee(getEmployee());
		if(getEmployee().getId() != null){
			IManagerBean contractBean = BeanManager.getManagerBean(Contract.class);
			Criteria criteria = new Criteria();
			criteria.addEqualExpression(contractBean.getFieldName(IRecordAlias.CONTRACT_EMPLOYEE_ID), getEmployee().getId());
			contractController.setCriteria(criteria);
			contractController.onSearch(null);
		}
	}
}