package dao.delivery;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Finder;

import models.delivery.Deliverable;
import models.delivery.PortfolioEntryDeliverable;
import models.pmo.PortfolioEntry;

/**
 * DAO for the {@link Deliverable} object.
 * 
 * @author Johann Kohler
 */
public class DeliverableDAO {

    public static Finder<Long, Deliverable> findDeliverable = new Finder<>(Deliverable.class);

    public static Finder<Long, PortfolioEntryDeliverable> findPortfolioEntryDeliverable = new Finder<>(PortfolioEntryDeliverable.class);

    /**
     * Default constructor.
     */
    public DeliverableDAO() {
    }

    /**
     * Get a deliverable by id.
     * 
     * @param id
     *            the deliverable id
     */
    public static Deliverable getDeliverableById(Long id) {
        return findDeliverable.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get the owner of a deliverable.
     * 
     * @param deliverableId
     *            the delivrable id
     */
    public static PortfolioEntry getDeliverableOwner(Long deliverableId) {
        return findPortfolioEntryDeliverable.where().eq("id.deliverableId", deliverableId).eq("type", PortfolioEntryDeliverable.Type.OWNER).findUnique()
                .getPortfolioEntry();

    }

    /**
     * Get all deliverable relation of a portfolio entry as an expression list.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ExpressionList<PortfolioEntryDeliverable> getPortfolioEntryDeliverableAsExprByPE(Long portfolioEntryId) {
        return findPortfolioEntryDeliverable.where().eq("id.portfolioEntryId", portfolioEntryId).eq("deliverable.deleted", false);
    }

    /**
     * Get a portfolio entry / deliverable relation.
     *
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param deliverableId
     *            the deliverable id
     */
    public static PortfolioEntryDeliverable getPortfolioEntryDeliverableById(Long portfolioEntryId, Long deliverableId) {
        return findPortfolioEntryDeliverable.where().eq("id.portfolioEntryId", portfolioEntryId).eq("id.deliverableId", deliverableId).findUnique();
    }

}
