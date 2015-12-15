package com.code.aon.ui.report.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import com.code.aon.common.ICollectionProvider;
import com.code.aon.common.ICriteriaProvider;
import com.code.aon.common.ManagerBeanException;
import com.code.aon.common.dao.hibernate.HibernateUtil;
import com.code.aon.common.dao.sql.DAOException;
import com.code.aon.ql.Criteria;
import com.code.aon.ui.report.IReportDynamicParamsProvider;
import com.code.aon.ui.report.OutputFormat;
import com.code.aon.ui.report.ReportException;
import com.code.aon.ui.report.config.ReportConfig;
import com.code.aon.ui.report.jr.JRReport;
import com.code.aon.ui.report.jr.JRReportFactory;

/**
 * Bean Manager for running reports.
 * 
 * @author Consulting & Development. ecastellano - 14-nov-2005
 * @since 1.0
 * 
 */
public class ReportManager {

	/**
	 * Obtains a suitable <code>Logger</code>.
	 */
	private static Logger LOGGER = Logger.getLogger(ReportManager.class
			.getName());

	/**
	 * Output format of the report.
	 */
	private OutputFormat outputFormat;

	/**
	 * Report idetifier.
	 */
	private String reportKey;

	/**
	 * Returns the report identifier.
	 * 
	 * @return The report identifier.
	 */
	public String getReportKey() {
		return reportKey;
	}

	/**
	 * Sets the report idetntifier.
	 * 
	 * @param reportKey
	 *            The report idetntifier.
	 */
	public void setReportKey(String reportKey) {
		this.reportKey = reportKey;
	}

	/**
	 * Returns the output format of the report.
	 * 
	 * @return The output format of the report.
	 */
	public OutputFormat getOutputFormat() {
		return outputFormat;
	}

	/**
	 * Sets the output format of the report.
	 * 
	 * @param outputFormat
	 *            The output format of the report.
	 */
	public void setOutputFormat(OutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}

	/**
	 * Returns an array of <code>javax.faces.model.SelectItem</code> of
	 * available output formats.
	 * 
	 * @return An array of available output formats.
	 */
	public SelectItem[] getOutputFormats() {
		SelectItem[] items = {
				new SelectItem(OutputFormat.PDF, OutputFormat.PDF.getType()),
				new SelectItem(OutputFormat.HTML, OutputFormat.HTML.getType()),
				new SelectItem(OutputFormat.XLS, OutputFormat.XLS.getType()),
				// new SelectItem(OutputFormat.XML, OutputFormat.XML.getType()),
				new SelectItem(OutputFormat.CSV, OutputFormat.CSV.getType()),
				new SelectItem(OutputFormat.RTF, OutputFormat.RTF.getType()),
				new SelectItem(OutputFormat.TXT, OutputFormat.TXT.getType()) };
		return items;
	}

	/**
	 * Runs the report. Obtains a
	 * <code>com.code.aon.ui.report.jr.JRReport</code> calling the
	 * <code>JRReportFactory.getJRReport(getReportKey())</code> method. Also,
	 * finalizes the reponse calling the
	 * <code>FacesContext.getCurrentInstance().responseComplete()</code>.
	 * 
	 * @return The outcome (- null - because this method finalizes the reponse).
	 * @throws ReportException
	 *             If an error ocurred.
	 * @throws DAOException 
	 */
	public String onExecute() throws ReportException, DAOException {
		
		boolean initTransState = HibernateUtil.mustBeginTransaction();
		boolean initSessionState = HibernateUtil.mustCloseSession();
			HibernateUtil.setCloseSession( false );
			HibernateUtil.setBeginTransaction( false );
		try{
			HibernateUtil.startSession();
			HibernateUtil.beginTransaction();

			JRReport report = JRReportFactory.getJRReport(getReportKey());
			Criteria criteria = getCriteria(report);
			List nestedList = report.getReportConfig().getNestedReports(); 
			boolean force = (nestedList != null && !nestedList.isEmpty());  
			Collection collection = getCollection(report, force);

			 
			String dp = report.getReportConfig().getDynamicParamsProvider();
			LOGGER.info( dp );		
			FacesContext ctx = FacesContext.getCurrentInstance();
			if (dp!=null) {
				ValueBinding vb = ctx.getApplication().createValueBinding( "#{" + dp + "}");
				IReportDynamicParamsProvider dpp = (IReportDynamicParamsProvider) vb.getValue(ctx);
				Map<String,Object> dynParams =  dpp.getDynamicParamsMap();
				LOGGER.info( "" + dynParams.size() );
				report.setDynamicParams(dynParams);
				LOGGER.info( "Dynamic params set!" );
			}

			String out = report.run(outputFormat, getOutputStrem(), getBundle(),
					criteria, collection);
			ctx.responseComplete();
			
			HibernateUtil.commitTransaction();
			return out;
		} catch (Throwable t ){
			HibernateUtil.rollbackTransaction();
			return null;
		} finally{
			if(initTransState  != HibernateUtil.mustBeginTransaction()){
				HibernateUtil.setBeginTransaction(initTransState);
			}
			if(initSessionState  != HibernateUtil.mustCloseSession()){
				HibernateUtil.setCloseSession(initSessionState);
			}
			if ( HibernateUtil.mustCloseSession() ) {
				HibernateUtil.closeSession();	
			}
		}
	}

	/**
	 * Obtains the OutputStream where the report will be writen. <br>
	 * <code>
	 * 		FacesContext ctx = FacesContext.getCurrentInstance();<br>
	 *		ExternalContext ec = ctx.getExternalContext();<br>
	 *		HttpServletResponse res = (HttpServletResponse) ec.getResponse();<br>
	 *		return res.getOutputStream();
	 * </code>
	 * 
	 * @return The OutputStream where the report will be writen.
	 * @throws ReportException
	 *             If an error ocurred.
	 */
	private OutputStream getOutputStrem() throws ReportException {
		try {
			FacesContext ctx = FacesContext.getCurrentInstance();
			ExternalContext ec = ctx.getExternalContext();
			HttpServletResponse res = (HttpServletResponse) ec.getResponse();
			LOGGER.info("ContentType " + getOutputFormat().getMimeType());
			res.setContentType(getOutputFormat().getMimeType());
			String contentDisposition = getContentDispositionHeader();
			if (contentDisposition != null) {
				res.setHeader("Content-Disposition", contentDisposition);
			}
			return res.getOutputStream();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
			throw new ReportException(e.getMessage(), e);
		}
	}

	/**
	 * Returns the value of the "Content-Disposition" HTTP header.
	 * 
	 * @return The value of the "Content-Disposition" HTTP header.
	 */
	private String getContentDispositionHeader() {
		if (getOutputFormat() == OutputFormat.XLS) {
			return "attachment; filename=\"report.xls\";";
		} else if (getOutputFormat() == OutputFormat.TXT) {
			return "attachment; filename=\"report.txt\";";
		} else if (getOutputFormat() == OutputFormat.RTF) {
			return "attachment; filename=\"report.rtf\";";
		}
		return null;
	}

	/**
	 * Obtains the ResourceBundle needed for the report. <br>
	 * <code>
	 * 		FacesContext ctx = FacesContext.getCurrentInstance();<br>
	 *		Application app = ctx.getApplication();<br>
	 *		String baseName = app.getMessageBundle();<br>
	 *		Locale locale = ctx.getViewRoot().getLocale();<br>
	 *		return ResourceBundle.getBundle(baseName, locale);<br>
	 * </code>
	 * 
	 * @return The ResourceBundle needed for the report.
	 */
	private ResourceBundle getBundle() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		Application app = ctx.getApplication();
		String baseName = app.getMessageBundle();
		Locale locale = ctx.getViewRoot().getLocale();
		ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
		return bundle;
	}

	/**
	 * Obtains the <code>com.aon.ql.Criteria</code> that will be passed to
	 * method <code>com.code.aon.common.IFinderBean.getList()</code>. If the
	 * criteria provider declared int the report config begin with the "#"
	 * character, this method will try to recover a
	 * <code>com.code.aon.ui.AbstractController</code> from the Faces context.
	 * Otherwise the litaral must be a valid ICriteriaProvider implementation
	 * class.
	 * 
	 * @param report
	 *            The report to be executed.
	 * @return The criteria.
	 * @throws ReportException
	 *             If an error ocurred.
	 */
	private Criteria getCriteria(JRReport report) throws ReportException {
		ReportConfig config = report.getReportConfig();
		String provider = config.getCriteriaProvider();
		try {
			if (provider != null) { // Criteria provider is EL expression ina a
				// faces context.
				if (provider.startsWith("#")) {
					FacesContext ctx = FacesContext.getCurrentInstance();
					ValueBinding vb = ctx.getApplication().createValueBinding(
							provider);
					ICriteriaProvider crpr = (ICriteriaProvider) vb
							.getValue(ctx);

					return crpr.getCriteria();
				} 
				// Criteria provider is a class.
				Class criteriaProviderClass = Class.forName(provider);
				ICriteriaProvider criteriaProvider = (ICriteriaProvider) criteriaProviderClass
						.newInstance();
				return criteriaProvider.getCriteria();
				
			}
			return null;
		} catch (ReferenceSyntaxException e) {
			throw new ReportException(e.getMessage(), e);
		} catch (PropertyNotFoundException e) {
			throw new ReportException(e.getMessage(), e);
		} catch (EvaluationException e) {
			throw new ReportException(e.getMessage(), e);
		} catch (ManagerBeanException e) {
			throw new ReportException(e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			throw new ReportException(e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new ReportException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new ReportException(e.getMessage(), e);
		}
	}
	
	/**
	 * Obtains the <code>java.util.Collection</code> that will be passed to
	 * method <code>com.code.aon.common.IFinderBean.getList()</code>. If the
	 * collection provider declared int the report config begin with the "#"
	 * character, this method will try to recover a
	 * <code>com.code.aon.ui.AbstractController</code> from the Faces context.
	 * Otherwise the litaral must be a valid ICollectionProvider implementation
	 * class.
	 * 
	 * @param report
	 *            The report to be executed.
	 * @return The collection.
	 * @throws ReportException
	 *             If an error ocurred.
	 */
	private Collection getCollection(JRReport report, boolean forceRefresh) throws ReportException {
		ReportConfig config = report.getReportConfig();
		String provider = config.getCollectionProvider();
		try {
			if (provider != null) { // Criteria provider is EL expression ina a
				// faces context.
				if (provider.startsWith("#")) {
					FacesContext ctx = FacesContext.getCurrentInstance();
					ValueBinding vb = ctx.getApplication().createValueBinding(
							provider);
					ICollectionProvider crpr = (ICollectionProvider) vb
							.getValue(ctx);

					return crpr.getCollection(forceRefresh);
				} 
				// Collection provider is a class.
				Class collectionProviderClass = Class.forName(provider);
				ICollectionProvider collectionProvider = (ICollectionProvider) collectionProviderClass
						.newInstance();
				return collectionProvider.getCollection(forceRefresh);
				
			}
			return null;
		} catch (ManagerBeanException e) {
			throw new ReportException(e.getMessage(), e);
		} catch (ReferenceSyntaxException e) {
			throw new ReportException(e.getMessage(), e);
		} catch (PropertyNotFoundException e) {
			throw new ReportException(e.getMessage(), e);
		} catch (EvaluationException e) {
			throw new ReportException(e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			throw new ReportException(e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new ReportException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new ReportException(e.getMessage(), e);
		}
	}
}
