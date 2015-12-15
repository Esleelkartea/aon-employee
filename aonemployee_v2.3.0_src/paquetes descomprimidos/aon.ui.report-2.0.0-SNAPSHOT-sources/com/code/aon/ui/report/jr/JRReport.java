package com.code.aon.ui.report.jr;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataSourceProvider;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.util.JRLoader;

import com.code.aon.common.BeanManager;
import com.code.aon.common.IFinderBean;
import com.code.aon.common.ManagerBeanException;
import com.code.aon.ql.Criteria;
import com.code.aon.ui.report.IReportConstants;
import com.code.aon.ui.report.OutputFormat;
import com.code.aon.ui.report.ReportException;
import com.code.aon.ui.report.config.ReportConfig;
import com.code.aon.ui.report.config.ReportConfigurationManager;
import com.code.aon.ui.report.config.ReportConfigurationParser;
import com.code.aon.ui.report.config.ReportFetchMode;
import com.code.aon.ui.report.jr.exporter.IJRExporterFactory;
import com.code.aon.ui.report.jr.exporter.JRExporterFactoryManager;
import com.code.aon.ui.util.AonUtil;

/**
 * Logic class representing a report.
 * 
 * @author Consulting & Development. ecastellano - 14-nov-2005
 * 
 */
public class JRReport {

	/**
	 * Obtains a suitable <code>Logger</code>.
	 */
	private static Logger LOGGER = Logger.getLogger(JRReport.class.getName());

	/**
	 * The report configuration of this report.
	 */
	private ReportConfig config;
	
	private Map<String, Object> dynParams = null;

	/**
	 * Constructs a report based on this report configuration.
	 * 
	 * @param config
	 *            The Report configuration.
	 * @throws ReportException
	 *             If an error ocurred.
	 */
	public JRReport(ReportConfig config) throws ReportException {
		this.config = config;
	}

	/**
	 * Returns the report configuration of this report.
	 * 
	 * @return The report configuration of this report.
	 */
	public ReportConfig getReportConfig() {
		return this.config;
	}

	/**
	 * Returns a Paged DataSource Provider.
	 * 
	 * @param criteria
	 *            The criteria of th query.
	 * @return A Paged DataSource Provider.
	 * @throws ReportException
	 *             If an error ocurred.
	 * 
	 */
	public JRDataSourceProvider getJRPagedDataSourceProvider(Criteria criteria)
			throws ReportException {
		IFinderBean bean = getFinderBean();
		int count = 500;
		Class clazz = bean.getPOJOClass();
		JRPagedBeanDataSourceProvider dsp = new JRPagedBeanDataSourceProvider(
				clazz, bean, criteria, count);
		return dsp;
	}

	/**
	 * Returns a DataSource Provider.
	 * 
	 * @param criteria
	 *            The criteria of th query.
	 * @return A DataSource Provider.
	 * @throws ReportException
	 *             If an error ocurred.
	 * 
	 */
	public JRDataSourceProvider getJRDataSourceProvider(Criteria criteria)
			throws ReportException {
		IFinderBean bean = getFinderBean();
		Class clazz = bean.getPOJOClass();
		JRBeanCollectionDataSourceProvider dsp = new JRBeanCollectionDataSourceProvider(
				clazz, bean, criteria);
		return dsp;
	}

	/**
	 * Returns the JasperReport object that this object represents.
	 * 
	 * @return The JasperReport object that this object represents.
	 * @throws ReportException
	 *             If an error ocurred.
	 */
	public JasperReport getJasperReport() throws ReportException {
		String template = config.getTemplate();
		InputStream input = JRReport.class.getResourceAsStream(template);
		if (input == null) {
			throw new ReportException("Can not load report template!"); //$NON-NLS-1$
		}
		try {
			Object o = JRLoader.loadObject(input);
			return (JasperReport) o;
		} catch (JRException e) {
			throw new ReportException(e.getMessage(), e);
		}
	}

	/**
	 * Returns the IFinderBean that provides the data of this report.
	 * 
	 * @return the IFinderBean that provides the data of this report.
	 * @throws ReportException
	 *             If an error ocurred.
	 */
	private IFinderBean getFinderBean() throws ReportException {
		try {
			return BeanManager.getManagerBean(config.getBeanKey());
		} catch (ManagerBeanException e) {
			throw new ReportException(e.getMessage(), e);
		}
	}

	/**
	 * Runs this report.
	 * 
	 * @param outputFormat
	 *            Tht output format.
	 * @param out
	 *            The output stream.
	 * @param bundle
	 *            The resource Bundle used in i18n.
	 * @param criteria
	 *            The criteria of the data.
	 * 
	 * @return The outcome.
	 * @throws ReportException
	 *             Si se produce alg�n error. If an error ocurred.
	 */
	public String run(OutputFormat outputFormat, OutputStream out,
			ResourceBundle bundle, Criteria criteria, Collection collection) throws ReportException {
		try {

			if (JRExporterFactoryManager.accept(outputFormat)) {
				IJRExporterFactory factory;
				factory = JRExporterFactoryManager
						.getJRExporterFactory(outputFormat);
				Map<Object, Object> map = new HashMap<Object, Object>();

				factory.fillJRParametersMap(map);

				passDefaultParameters(map);
				passCustomParameters(map);
				passDynamicParameters( map );
				boolean hasCache = passFetchModeParameters(map);
				passNestedReports(map);
				map.put(JRExporterParameter.OUTPUT_STREAM, out);

				if ( config.getParams() != null && config.getParams().containsKey(
						JRParameter.REPORT_RESOURCE_BUNDLE)) {
					String baseName = (String) config.getParams().get(
							JRParameter.REPORT_RESOURCE_BUNDLE);
					map.put(JRParameter.REPORT_RESOURCE_BUNDLE, ResourceBundle
							.getBundle(baseName));
				} else {
					map.put(JRParameter.REPORT_RESOURCE_BUNDLE, bundle);
				}

				Connection c = AonUtil.getSQLConnection();
				map.put(JRParameter.REPORT_CONNECTION, c);
					
				JRDataSource ds = null;
				if(config.getCollectionProvider() == null){
					JRDataSourceProvider jrdsp = (hasCache) ? getJRPagedDataSourceProvider(criteria)
							: getJRDataSourceProvider(criteria);
					ds = jrdsp.create(null);
				}else{
					ds = new JRBeanCollectionDataSource(collection);
				}
				
				JasperReport jr = getJasperReport();
				JasperPrint print = JasperFillManager.fillReport(jr, map, ds);

				map.put(JRExporterParameter.JASPER_PRINT, print);
				debugParameters( map );
				JRExporter exporter = factory.getJRExporter();
				exporter.setParameters(map);
				exporter.exportReport();
				if (hasCache) {
					cleanCache(map);
				}
			} else {
				throw new ReportException("No suitable Exporter for format "
						+ outputFormat);
			}
			return null;
		} catch (JRException e) {
			throw new ReportException(e.getMessage(), e);
		}
	}

	private void debugParameters(Map<Object, Object> map) {
		LOGGER.finest( "Begin Parameters:" );
		Set<Object> keys = map.keySet();
		for (Object key: keys){
			Object value  = map.get(key);
			LOGGER.finest( "\tParameter: "+ key + " ---> " + value );	
		}
		LOGGER.finest( "End Parameters:" );
	}

	private void passDynamicParameters(Map<Object, Object> map) {
		if (dynParams != null) {
			LOGGER.fine("Passing Dynamic Parameters");
			map.putAll(dynParams);
		}
	}

	/**
	 * Pass the virtualizer parameter to fill manager, if needed.
	 * 
	 * @param map
	 *            The parameters map.
	 * @return True if the record is virtualized.
	 * @throws ReportException
	 *             If an error ocurred.
	 */
	private boolean passFetchModeParameters(Map<Object, Object> map)
			throws ReportException {
		ReportFetchMode fetchMode = null;
		ReportConfig defaultConfig = getDefaultConfig();
		ReportFetchMode defaultFetchMode = (defaultConfig == null) ? null
				: defaultConfig.getFetchMode();
		ReportFetchMode reportFetchMode = config.getFetchMode();
		fetchMode = (reportFetchMode == null) ? defaultFetchMode
				: reportFetchMode;
		if (fetchMode != null && fetchMode.isPaginated()) {
			JRFileVirtualizer virt;
			String path = System.getProperty("java.io.tmpdir");
			int vms = fetchMode.getVirtualizerPageMax();
			LOGGER.info("Report Virtualizer. Page Max: " + vms + " Path: "
					+ path);
			virt = new JRFileVirtualizer(vms, path);
			map.put(JRParameter.REPORT_VIRTUALIZER, virt);
		}
		return (fetchMode != null && fetchMode.isPaginated());
	}

	/**
	 * Cleans the temporary generated files.
	 * 
	 * @param map
	 *            The parameters map.
	 */
	private void cleanCache(Map map) {
		JRFileVirtualizer virt;
		virt = (JRFileVirtualizer) map.get(JRParameter.REPORT_VIRTUALIZER);
		virt.cleanup();
	}

	/**
	 * Pass the nested reports list to the fill manager.
	 * 
	 * @param map
	 *            The parameters map.
	 * @throws ReportException
	 *             If an error ocurred.
	 */
	private void passNestedReports(Map<Object, Object> map)
			throws ReportException {
		if (config.getNestedReports() != null) {
			Map<String, JasperReport> nested = new HashMap<String, JasperReport>();
			Iterator<String> iter = config.getNestedReports().iterator();
			while (iter.hasNext()) {
				String nestedReportKey = iter.next();
				JRReport nestedReport = JRReportFactory
						.getJRReport(nestedReportKey);
				JasperReport jnr = nestedReport.getJasperReport();
				LOGGER.info("Nested Report " + nestedReportKey);
				nested.put(nestedReportKey, jnr);
			}
			map.put(IReportConstants.NESTED_REPORTS, nested);
		}
	}

	/**
	 * Pass the declared parameter to the fill manager.
	 * 
	 * @param map
	 *            The parameters map.
	 * @throws ReportException
	 *             If an error ocurred.
	 */
	private void passCustomParameters(Map<Object, Object> map)
			throws ReportException {
		if (config.getParams() != null) {
			LOGGER.fine("Passing Custom Parameters");
			map.putAll(config.getParams());
			Iterator iter = config.getParams().keySet().iterator();
			while(iter.hasNext()){
				String key = (String)iter.next();
				String value = (String)map.get(key);
				if(value.startsWith("#")){
					String controllerName = value.substring(value.indexOf("{") + 1, value.indexOf("."));
					String methodName = value.substring(value.indexOf(".") + 1, value.indexOf("}"));
					FacesContext ctx = FacesContext.getCurrentInstance();
					ValueBinding vb = ctx.getApplication().createValueBinding("#{" + controllerName + "}");
					Object o = vb.getValue(ctx);
					try {
						Method m;
						m = o.getClass().getMethod(methodName, new Class[0]);
						Object obj = m.invoke(o, new Object[0]);
						map.put(key, obj);
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Pass the default parameters to the fill manager.
	 * 
	 * @param map
	 *            The parameters map.
	 * @throws ReportException
	 *             If an error ocurred.
	 */
	private void passDefaultParameters(Map<Object, Object> map)
			throws ReportException {
		ReportConfig defaultConfig = getDefaultConfig();
		if (defaultConfig != null && defaultConfig.getParams() != null) {
			LOGGER.fine("Passing Default Parameters");
			map.putAll(defaultConfig.getParams());
		}
	}

	/**
	 * Returns the declared default config.
	 * 
	 * @return The declared default config.
	 * @throws ReportException
	 *             If an error ocurred.
	 */
	private ReportConfig getDefaultConfig() throws ReportException {
		ReportConfigurationParser parser = ReportConfigurationParser
				.getInstance();
		ReportConfigurationManager configuration = parser
				.getConfigurationManager();
		return configuration.getDefaultConfig();

	}

	public void setDynamicParams(Map<String, Object> dynParams) {
		// TODO Auto-generated method stub
		if(this.dynParams == null){
			this.dynParams = new HashMap<String,Object>();
		}
		this.dynParams = dynParams;
	}

}
