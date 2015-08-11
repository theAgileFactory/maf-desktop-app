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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dao.finance.CurrencyDAO;
import dao.finance.PurchaseOrderDAO;
import dao.reporting.ReportingDao;
import framework.services.database.IDatabaseDependencyService;
import framework.services.notification.INotificationManagerPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IPersonalStoragePlugin;
import framework.services.system.ISysAdminUtils;
import framework.utils.Msg;
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
import play.Configuration;
import play.Environment;
import play.Logger;
import play.Play;
import play.inject.ApplicationLifecycle;
import play.libs.F.Promise;
import play.mvc.Http.Context;
import scala.concurrent.duration.Duration;

/**
 * Provides the methods to manage the jasper reports.
 * 
 * @author Johann Kohler
 */
@Singleton
public class ReportingUtilsImpl implements IReportingUtils {
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    private IPersonalStoragePlugin personalStoragePlugin;
    private INotificationManagerPlugin notificationManagerPlugin;
    private ISysAdminUtils sysAdminUtils;
    private Configuration configuration;
    private Environment environment;
    
    private static Logger.ALogger log = Logger.of(ReportingUtilsImpl.class);

    public Map<String, JasperReport> jasperReports = null;

    private static final String EXCEL_FILE_NAME = "report_%s_%s_%s.xlsx";
    private static final String PDF_FILE_NAME = "report_%s_%s_%s.pdf";
    private static final String CSV_FILE_NAME = "report_%s_%s_%s.csv";
    private static final String WORD_FILE_NAME = "report_%s_%s_%s.docx";
    private static final String POWER_POINT_FILE_NAME = "report_%s_%s_%s.pptx";

    /**
     * Creates a new ReportingUtilsImpl
     * @param lifecycle
     *            the play application lifecycle listener
     * @param configuration
     *            the play application configuration
     * @param environment 
     *            the play environment
     * @param userSessionManagerPlugin the user session manager
     * @param personalStoragePlugin the personal storage manager
     * @param notificationManagerPlugin the notification manager
     * @param sysAdminUtils the sysadmin utilities
     * @param databaseDependencyService
     */
    @Inject
    public ReportingUtilsImpl(ApplicationLifecycle lifecycle, Configuration configuration,Environment environment,
            IUserSessionManagerPlugin userSessionManagerPlugin,
            IPersonalStoragePlugin personalStoragePlugin,
            INotificationManagerPlugin notificationManagerPlugin,
            ISysAdminUtils sysAdminUtils,
            IDatabaseDependencyService databaseDependencyService) {
        log.info("SERVICE>>> ReportingUtilsImpl starting...");
        this.configuration=configuration;
        this.environment=environment;
        this.userSessionManagerPlugin=userSessionManagerPlugin;
        this.personalStoragePlugin=personalStoragePlugin;
        this.notificationManagerPlugin=notificationManagerPlugin;
        this.sysAdminUtils=sysAdminUtils;
        loadDefinitions();
        lifecycle.addStopHook(() -> {
            log.info("SERVICE>>> ReportingUtilsImpl stopping...");
            shutdown();
            log.info("SERVICE>>> ReportingUtilsImpl stopped");
            return Promise.pure(null);
        });
        log.info("SERVICE>>> ReportingUtilsImpl started");
    }

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
    @Override
    public void generate(Context context, final Reporting report, final String language, final Format format, Map<String, Object> reportParameters) {

        // get the user id
        final String uid = getUserSessionManagerPlugin().getUserSessionId(context);

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

        getSysAdminUtils().scheduleOnce(false, "ReportGeneration " + report.getName(), Duration.create(0, TimeUnit.MILLISECONDS), new Runnable() {
            @Override
            public void run() {

                IPersonalStoragePlugin personalStorage = getPersonalStoragePlugin();
                INotificationManagerPlugin notificationManagerPlugin = getNotificationManagerPlugin();

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

                    log.error(e.getMessage());

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
    @Override
    public Connection getDataAdapter() {
        try {
            return DriverManager.getConnection(
                    getConfiguration().getString("db.default.url"), 
                    getConfiguration().getString("db.default.username"),
                    getConfiguration().getString("db.default.password"));
        } catch (SQLException e) {
            log.error("Unable to initialize the access to the database for the reports",e);
        }
        return null;
    }

    /**
     * Load the report definitions.
     */
    @Override
    public void loadDefinitions() {

        jasperReports = new HashMap<String, JasperReport>();

        for (Reporting report : ReportingDao.getReportingAsList()) {
            File reportFile = getReportPath(report);
            if (reportFile != null && reportFile.exists()) {
                try {
                    JasperDesign jasperDesign = JRXmlLoader.load(reportFile);
                    JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
                    jasperReports.put(report.template, jasperReport);
                    if(log.isDebugEnabled()){
                        log.debug("the jasper report " + report.template + " has been loaded");
                    }
                } catch (Exception e) {
                    log.error("Error while loading the report "+report.template,e.getMessage());
                }

            } else {
                log.error("Jasper report " + report.template+" not found !");
            }
        }

    }

    /**
     * Clean the reports.
     */
    private void shutdown() {
        jasperReports = null;
    }

    /**
     * Get the root folder of a report as a file.
     * 
     * @param report
     *            the report
     */
    private File getReportFolder(Reporting report) {
        if (report.isStandard) {
            return getEnvironment().getFile("conf/jasper/" + report.template);
        } else {
            return new File(getConfiguration().getString("maf.report.custom.root") + "/" + report.template);
        }
    }

    /**
     * Get the file of the definition of a report.
     * 
     * @param report
     *            the report
     */
    private File getReportPath(Reporting report) {
        String filePath = report.template + "/" + report.template + "_main.jrxml";
        if (report.isStandard) {
            return getEnvironment().getFile("conf/jasper/" + filePath);
        } else {
            return new File(getConfiguration().getString("maf.report.custom.root") + "/" + filePath);
        }
    }

    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    private IPersonalStoragePlugin getPersonalStoragePlugin() {
        return personalStoragePlugin;
    }

    private INotificationManagerPlugin getNotificationManagerPlugin() {
        return notificationManagerPlugin;
    }

    private ISysAdminUtils getSysAdminUtils() {
        return sysAdminUtils;
    }

    private Configuration getConfiguration() {
        return configuration;
    }

    private Environment getEnvironment() {
        return environment;
    }
}
