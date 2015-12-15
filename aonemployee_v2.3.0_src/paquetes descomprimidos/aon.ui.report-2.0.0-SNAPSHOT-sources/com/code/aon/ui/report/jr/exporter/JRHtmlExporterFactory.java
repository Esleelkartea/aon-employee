package com.code.aon.ui.report.jr.exporter;

import java.util.Map;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;

import com.code.aon.ui.report.IReportConstants;
import com.code.aon.ui.report.ReportException;

/**
 * Returns an exporter for the HTML format.
 * 
 * @author Consulting & Development. Eugenio Castellano - 05-may-2005
 * @since 1.0
 * 
 */
public class JRHtmlExporterFactory implements IJRExporterFactory {

	/* (non-Javadoc)
	 * @see com.code.aon.ui.report.jr.exporter.IJRExporterFactory#getJRExporter()
	 */
	public JRExporter getJRExporter() {
		return new JRHtmlExporter();
	}

	/* (non-Javadoc)
	 * @see com.code.aon.ui.report.jr.exporter.IJRExporterFactory#fillJRParametersMap(java.util.Map)
	 */
	public void fillJRParametersMap(Map<Object,Object> map) throws ReportException {
		map
				.put(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN,
						Boolean.FALSE);
		map.put(JRHtmlExporterParameter.HTML_HEADER, getHtmlHeader());
		map.put(JRHtmlExporterParameter.HTML_FOOTER, getHtmlFooter());
		map.put(JRHtmlExporterParameter.BETWEEN_PAGES_HTML, "");
		map.put(IReportConstants.SHOULD_PRINT_HEADERS, Boolean.FALSE);
	}

	/**
	 * Returns A string representing HTML code that will be inserted before the generated report.
	 * @return the text.
	 */
	public String getHtmlHeader() {
		return "";
	}

	/**
	 * Returns A string representing HTML code that will be inserted after the generated report.
	 * @return the text.
	 */
	public String getHtmlFooter() {
		return "";
	}
}
