package services.tableprovider;

import framework.services.kpi.IKpiService;
import utils.table.PortfolioEntryListView;

/**
 * The table definitions.
 * 
 * @author Johann Kohler
 *
 */
public class TableDefinitions {

    public PortfolioEntryListView.TableDefinition portfolioEntry;

    public TableDefinitions(IKpiService kpiService) {
        this.portfolioEntry = new PortfolioEntryListView.TableDefinition(kpiService);
    }

}
