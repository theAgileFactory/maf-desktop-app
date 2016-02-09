package services.tableprovider;

/**
 * The service that provides the tables.
 * 
 * @author Johann Kohler
 */
public interface ITableProvider {

    /**
     * Get the table definitions.
     */
    TableDefinitions get();

    /**
     * Flush and recompute the tables.
     */
    void flushTables();

    /**
     * Flush and recompute the filter config.
     */
    void flushFilterConfig();

}
