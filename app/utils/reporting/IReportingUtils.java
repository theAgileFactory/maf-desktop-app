package utils.reporting;

import java.sql.Connection;
import java.util.Map;

import models.reporting.Reporting;
import play.mvc.Http.Context;

/**
 * The reporting utils.
 * 
 * @author Johann Kohler
 *
 */
public interface IReportingUtils {

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
    public abstract void generate(Context context, Reporting report, String language, Reporting.Format format, Map<String, Object> reportParameters);

    /**
     * Get the data adapter (DB connection).
     */
    public abstract Connection getDataAdapter();

    /**
     * Load the report definitions.
     */
    public abstract void loadDefinitions();

}