package com.code.aon.ui.config.event;

import java.util.Iterator;
import java.util.List;

import com.code.aon.common.BeanManager;
import com.code.aon.common.IManagerBean;
import com.code.aon.common.ManagerBeanException;
import com.code.aon.config.User;
import com.code.aon.config.UserScope;
import com.code.aon.config.dao.IConfigAlias;
import com.code.aon.ql.Criteria;
import com.code.aon.ql.ast.Expression;
import com.code.aon.ql.util.ExpressionException;
import com.code.aon.ql.util.ExpressionUtilities;
import com.code.aon.ui.config.util.UserUtils;
import com.code.aon.ui.form.event.ControllerAdapter;
import com.code.aon.ui.form.event.ControllerEvent;
import com.code.aon.ui.form.event.ControllerListenerException;

public class ScopeFilterListener extends ControllerAdapter {

	private String aliasName;

	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void beforeModelInitialized(ControllerEvent event) throws ControllerListenerException {
		try {
			User user = UserUtils.getInstance().getLoggedUser();
			Expression exp = null;
			if (user != null) {
				Iterator iter = obtainUserScopeList(user).iterator();
				while(iter.hasNext()){
					UserScope userScope = (UserScope)iter.next();
					exp = ExpressionUtilities.getOrExpression(exp, ExpressionUtilities.getExpression(userScope.getScope().getId().toString(), event.getController().getFieldName(this.aliasName)));
				}
			}
			if(exp != null){
				event.getController().getCriteria().addExpression(exp);
			}else{
				Expression nullExp = ExpressionUtilities.getNullExpression(event.getController().getFieldName(this.aliasName));
				event.getController().getCriteria().addExpression(nullExp);
			}
		} catch (ManagerBeanException e) {
			throw new ControllerListenerException("Error adding scopeFilter",e);
		} catch (ExpressionException e) {
			throw new ControllerListenerException("Error adding scopeFilter",e);
		}
	}

	@SuppressWarnings("unchecked")
	private List obtainUserScopeList(User user) throws ManagerBeanException {
		IManagerBean userScopeBean = BeanManager.getManagerBean(UserScope.class);
		Criteria criteria = new Criteria();
		criteria.addEqualExpression(userScopeBean.getFieldName(IConfigAlias.USER_SCOPE_USER_ID), user.getId());
		return userScopeBean.getList(criteria);
	}

}