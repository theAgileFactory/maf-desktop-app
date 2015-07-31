/*! LICENSE
 *
 * Copyright (c) 2015, The Agile Factory SA and/or its affiliates. All rights
 * reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package utils.reporting;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import dao.finance.CurrencyDAO;
import dao.finance.PurchaseOrderDAO;
import dao.reporting.ReportingDao;
import framework.services.ServiceManager;
import framework.services.notification.INotificationManagerPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IPersonalStoragePlugin;
import framework.utils.Msg;
import framework.utils.SysAdminUtils;
import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import models.reporting.Reporting;
import models.reporting.Reporting.Format;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.LocalJasperReportsContext;
import net.sf.jasperreports.engine.util.SimpleFileResolver;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration;
import net.sf.jasperreports.export.SimpleDocxReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import play.Logger;
import play.Play;
import play.mvc.Http.Context;
import scala.concurrent.duration.Duration;

/**
 * Provides the methods to manage the jasper reports.
 * 
 * @author Johann Kohler
 */
public class JasperUtils {

    private static Logger.ALogger log = Logger.of(JasperUtils.class);

    public static Map<String, JasperReport> jasperReports = null;

    private static final String EXCEL_FILE_NAME = "report_%s_%s_%s.xlsx";
    private static final String PDF_FILE_NAME = "report_%s_%s_%s.pdf";
    private static final String CSV_FILE_NAME = "report_%s_%s_%s.csv";
    private static final String WORD_FILE_NAME = "report_%s_%s_%s.docx";
    private static final String POWER_POINT_FILE_NAME = "report_%s_%s_%s.pptx";

    /**
     * Generate a jasper a report and place the file in the personal space.
     * 
     * @param context
     *            the current HTTP context
     * @param report
     *            the report
     * @param language
     *            the language (en, fr, de)
     * @param format
     *            the format
     * @param reportParameters
     *            the report parameters
     */
    public static void generate(Context context, final Reporting report, final String language, final Format format, Map<String, Object> reportParameters) {

        // get the user id
        IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
        final String uid = userSessionManagerPlugin.getUserSessionId(context);

        /**
         * parameters.
         */

        final Map<String, Object> parameters = new HashMap<String, Object>();

        // set the locale
        Locale locale = new Locale(language, "");
        parameters.put(JRParameter.REPORT_LOCALE, locale);

        // set the report folder as the relative path (for the resources)
        SimpleFileResolver fileResolver = new SimpleFileResolver(getReportFolder(report));
        LocalJasperReportsContext ctx = new LocalJasperReportsContext(DefaultJasperReportsContext.getInstance());
        ctx.setFileResolver(fileResolver);
        final JasperFillManager fillManager = JasperFillManager.getInstance(ctx);
        final JasperExportManager exportManager = JasperExportManager.getInstance(ctx);

        // set the format
        parameters.put("format", format.name());

        // set the "use purchase order"
        parameters.put("use_purchase_order", PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder());

        // set the default currency code
        parameters.put("default_currency_code", CurrencyDAO.getCurrencyDefault().code);

        // set report parameters
        parameters.putAll(reportParameters);

        // prepare the notification messages
        final String successTitle = Msg.get("core.reporting.generate.process.success.title");
        final String successMessage = Msg.get("core.reporting.generate.process.success.message");
        final String failureTitle = Msg.get("core.reporting.generate.process.failure.title");
        final String failureMessage = Msg.get("core.reporting.generate.process.failure.message");

        SysAdminUtils.scheduleOnce(false, "ReportGeneration " + report.getName(), Duration.create(0, TimeUnit.MILLISECONDS), new Runnable() {
            @Override
            public void run() {

                IPersonalStoragePlugin personalStorage = ServiceManager.getService(IPersonalStoragePlugin.NAME, IPersonalStoragePlugin.class);
                INotificationManagerPlugin notificationManagerPlugin = ServiceManager.getService(INotificationManagerPlugin.NAME,
                        INotificationManagerPlugin.class);

                OutputStream out = null;
                String fileName = null;

                try {

                    JasperPrint print = fillManager.fill(jasperReports.get(report.template), parameters, getDataAdapter());

                    switch (format) {

                    case POWER_POINT:
                        fileName = String.format(POWER_POINT_FILE_NAME, report.template, language,
                                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()));
                        out = personalStorage.createNewFile(uid, fileName);
                        JRPptxExporter pptxExporter = new JRPptxExporter();
                        pptxExporter.setExporterInput(new SimpleExporterInput(print));
                        pptxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
                        pptxExporter.exportReport();
                        break;

                    case WORD:
                        fileName = String.format(WORD_FILE_NAME, report.template, language, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()));
                        out = personalStorage.createNewFile(uid, fileName);

                        JRDocxExporter docxExporter = new JRDocxExporter();
                        SimpleDocxReportConfiguration docxConfig = new SimpleDocxReportConfiguration();

                        docxExporter.setConfiguration(docxConfig);
                        docxExporter.setExporterInput(new SimpleExporterInput(print));
                        docxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
                        docxExporter.exportReport();
                        break;

                    case EXCEL:
                        fileName = String.format(EXCEL_FILE_NAME, report.template, language, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()));
                        out = personalStorage.createNewFile(uid, fileName);
                        JRXlsxExporter xlsxExporter = new JRXlsxExporter();
                        SimpleXlsxReportConfiguration xlsxConfig = new SimpleXlsxReportConfiguration();
                        xlsxConfig.setRemoveEmptySpaceBetweenColumns(true);
                        xlsxConfig.setRemoveEmptySpaceBetweenRows(true);
                        xlsxConfig.setOnePagePerSheet(false);
                        xlsxConfig.setDetectCellType(true);
                        xlsxExporter.setConfiguration(xlsxConfig);
                        xlsxExporter.setExporterInput(new SimpleExporterInput(print));
                        xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
                        xlsxExporter.exportReport();
                        break;

                    case PDF:
                        fileName = String.format(PDF_FILE_NAME, report.template, language, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()));
                        out = personalStorage.createNewFile(uid, fileName);
                        exportManager.exportToPdfStream(print, out);
                        break;

                    case CSV:
                        fileName = String.format(CSV_FILE_NAME, report.template, language, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()));
                        out = personalStorage.createNewFile(uid, fileName);
                        JRCsvExporter csvExporter = new JRCsvExporter();
                        SimpleCsvExporterConfiguration csvConfig = new SimpleCsvExporterConfiguration();
                        csvConfig.setFieldDelimiter(";");
                        csvExporter.setConfiguration(csvConfig);
                        csvExporter.setExporterInput(new SimpleExporterInput(print));
                        csvExporter.setExporterOutput(new SimpleWriterExporterOutput(out));
                        csvExporter.exportReport();
                        break;
                    }

                    notificationManagerPlugin.sendNotification(uid, NotificationCategory.getByCode(Code.DOCUMENT), successTitle, successMessage,
                            controllers.my.routes.MyPersonalStorage.index().url());

                } catch (Exception e) {

                    Logger.error(e.getMessage());

                    if (out != null) {
                        try {
                            personalStorage.deleteFile(uid, fileName);
                        } catch (Exception intE) {
                            log.error("Unable to delete the excel file", intE);
                        }
                    }

                    notificationManagerPlugin.sendNotification(uid, NotificationCategory.getByCode(Code.ISSUE), failureTitle, failureMessage,
                            controllers.core.routes.ReportingController.index().url());

                }
            }
        });

    }

    /**
     * Get the data adapter (DB connection).
     */
    public static Connection getDataAdapter() {
        try {
            return DriverManager.getConnection(play.Configuration.root().getString("db.default.url"), play.Configuration.root().getString("db.default.user"),
                    play.Configuration.root().getString("db.default.password"));
        } catch (SQLException e) {
            Logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * Load the report definitions.
     */
    public static void loadDefinitions() {

        jasperReports = new HashMap<String, JasperReport>();

        for (Reporting report : ReportingDao.getReportingAsList()) {
            File reportFile = getReportPath(report);
            if (reportFile != null && reportFile.exists()) {
                try {
                    JasperDesign jasperDesign = JRXmlLoader.load(reportFile);
                    JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
                    jasperReports.put(report.template, jasperReport);
                    Logger.debug("the jasper report " + report.template + " has been loaded");
                } catch (Exception e) {
                    Logger.error(e.getMessage());
                }

            } else {
                Logger.error("impossible to find the jasper report for: " + report.template);
            }
        }

    }

    /**
     * Clean the reports.
     */
    public static void shutdown() {
        jasperReports = null;
    }

    /**
     * Get the root folder of a report as a file.
     * 
     * @param report
     *            the report
     */
    private static File getReportFolder(Reporting report) {
        if (report.isStandard) {
            URL url = Play.application().classloader().getResource("jasper/" + report.template);
            return new File(url.getPath());
        } else {
            return new File(play.Configuration.root().getString("maf.report.custom.root") + "/" + report.template);
        }
    }

    /**
     * Get the file of the definition of a report.
     * 
     * @param report
     *            the report
     */
    private static File getReportPath(Reporting report) {
        String filePath = report.template + "/" + report.template + "_main.jrxml";
        if (report.isStandard) {
            URL url = Play.application().classloader().getResource("jasper/" + filePath);
            return new File(url.getPath());
        } else {
            return new File(play.Configuration.root().getString("maf.report.custom.root") + "/" + filePath);
        }
    }
}
