package dao.delivery;

import java.util.List;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Finder;

import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
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
     *            the deliverable id
     */
    public static PortfolioEntry getDeliverableOwner(Long deliverableId) {
        return findPortfolioEntryDeliverable.where().eq("id.deliverableId", deliverableId).eq("type", PortfolioEntryDeliverable.Type.OWNER).findUnique()
                .getPortfolioEntry();

    }

    /**
     * Get all deliverables of a portfolio entry as an expression list.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ExpressionList<Deliverable> getDeliverableAsExprByPE(Long portfolioEntryId) {
        return findDeliverable.where().eq("deleted", false).eq("portfolioEntryDeliverables.portfolioEntry.id", portfolioEntryId);
    }

    /**
     * Get all deliverables owned by a portfolio entry as an expression list.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ExpressionList<Deliverable> getDeliverableOwnerAsExprByPE(Long portfolioEntryId) {
        return findDeliverable.where().eq("deleted", false).eq("portfolioEntryDeliverables.type", PortfolioEntryDeliverable.Type.OWNER)
                .eq("portfolioEntryDeliverables.portfolioEntry.id", portfolioEntryId);
    }

    /**
     * Get the deliverables of a requirements.
     * 
     * @param requirementId
     *            the requirement id
     */
    public static List<Deliverable> getDeliverableAsListByRequirement(Long requirementId) {
        return findDeliverable.where().eq("deleted", false).eq("requirements.id", requirementId).findList();
    }

    /**
     * Get all deliverables owned by a portfolio entry as a value holder
     * collection.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ISelectableValueHolderCollection<Long> getDeliverableOwnerAsVHByPE(Long portfolioEntryId) {
        return new DefaultSelectableValueHolderCollection<>(getDeliverableOwnerAsExprByPE(portfolioEntryId).findList());
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
