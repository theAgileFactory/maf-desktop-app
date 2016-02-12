package services.picker;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;

import dao.delivery.DeliverableDAO;
import dao.finance.BudgetBucketDAO;
import dao.finance.CostCenterDAO;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.PickerHandler;
import framework.utils.PickerHandler.Handle;
import framework.utils.PickerHandler.Parameters;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F.Promise;

/**
 * The service that provides the pickers.
 * 
 * @author Johann Kohler
 */
@Singleton
public class PickerServiceImpl implements IPickerService {

    private PickerHandler<String> principal;
    private PickerHandler<Long> actor;
    private PickerHandler<Long> actorWithoutUid;
    private PickerHandler<Long> actorByOrgUnit;
    private PickerHandler<Long> actorByCompetency;
    private PickerHandler<Long> budgetBucket;
    private PickerHandler<Long> costCenter;
    private PickerHandler<Long> deliverableByPortfolioEntry;
    private PickerHandler<Long> orgUnit;
    private PickerHandler<Long> deliveryUnit;
    private PickerHandler<Long> sponsoringUnit;
    private PickerHandler<Long> planningPackage;
    private PickerHandler<Long> portfolioEntryType;
    private PickerHandler<Long> portfolio;
    private PickerHandler<Long> portfolioType;

    private IAccountManagerPlugin accountManagerPlugin;
    private IAttachmentManagerPlugin attachmentManagerPlugin;

    /**
     * Initialize the service.
     * 
     * @param lifecycle
     *            the Play life cycle service
     * @param configuration
     *            the Play configuration service
     * @param accountManagerPlugin
     *            the account manager service
     * @param attachmentManagerPlugin
     *            the attachment manager service
     */
    @Inject
    public PickerServiceImpl(ApplicationLifecycle lifecycle, Configuration configuration, IAccountManagerPlugin accountManagerPlugin,
            IAttachmentManagerPlugin attachmentManagerPlugin) {

        Logger.info("SERVICE>>> PickerServiceImpl starting...");

        this.accountManagerPlugin = accountManagerPlugin;
        this.attachmentManagerPlugin = attachmentManagerPlugin;

        this.init();

        lifecycle.addStopHook(() -> {
            Logger.info("SERVICE>>> PickerServiceImpl stopping...");
            Logger.info("SERVICE>>> PickerServiceImpl stopped");
            return Promise.pure(null);
        });

        Logger.info("SERVICE>>> PickerServiceImpl started");
    }

    @Override
    public void init() {

        this.principal = new PickerHandler<String>(this.getAttachmentManagerPlugin(), String.class, new Handle<String>() {

            @Override
            public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
                defaultParameters.put(Parameters.SEARCH_ENABLED, "true");
                return defaultParameters;
            }

            @Override
            public ISelectableValueHolderCollection<String> getFoundValueHolders(String searchString, Map<String, String> context) {
                return getSelectableValuesList(searchString);
            }

            @Override
            public ISelectableValueHolderCollection<String> getInitialValueHolders(List<String> values, Map<String, String> context) {
                return getSelectableValuesList(null);
            }

            /**
             * Get the list of selectable principals.
             * 
             * @param searchString
             *            a search string (selection of principal names)
             * 
             */
            private ISelectableValueHolderCollection<String> getSelectableValuesList(String searchString) {
                if (searchString == null) {
                    searchString = "*";
                }
                ISelectableValueHolderCollection<String> selectableValues = new DefaultSelectableValueHolderCollection<String>();
                if (StringUtils.isNotBlank(searchString)) {
                    if (!searchString.equals("*")) {
                        searchString = "*" + searchString + "*";
                    }
                    try {
                        List<IUserAccount> userAccounts = getAccountManagerPlugin().getUserAccountsFromName(searchString);
                        if (userAccounts != null) {
                            for (IUserAccount userAccount : userAccounts) {
                                if (userAccount.isActive() && userAccount.isDisplayed()) {
                                    selectableValues.add(new DefaultSelectableValueHolder<String>(userAccount.getUid(),
                                            String.format("%s %s", userAccount.getFirstName(), userAccount.getLastName())));
                                }
                            }
                        }
                    } catch (AccountManagementException e) {
                        Logger.error("Unable to get a list of users using the specified searchString", e);
                    }
                }
                return selectableValues;
            }

        });

        this.actor = new PickerHandler<Long>(this.getAttachmentManagerPlugin(), Long.class, new Handle<Long>() {

            @Override
            public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
                defaultParameters.put(Parameters.SEARCH_ENABLED, "true");
                return defaultParameters;
            }

            @Override
            public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
                return ActorDao.getActorActiveAsVH();
            }

            @Override
            public ISelectableValueHolderCollection<Long> getFoundValueHolders(String searchString, Map<String, String> context) {
                searchString = searchString.replaceAll("\\*", "%");
                return ActorDao.getActorActiveAsVHByKeywords(searchString);
            }

        });

        this.actorWithoutUid = new PickerHandler<Long>(this.getAttachmentManagerPlugin(), Long.class, new Handle<Long>() {

            @Override
            public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
                defaultParameters.put(Parameters.SEARCH_ENABLED, "true");
                return defaultParameters;
            }

            @Override
            public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
                return ActorDao.getActorActiveWithoutUidAsVH();
            }

            @Override
            public ISelectableValueHolderCollection<Long> getFoundValueHolders(String searchString, Map<String, String> context) {
                searchString = searchString.replaceAll("\\*", "%");
                return ActorDao.getActorActiveWithoutUidAsVHByKeywords(searchString);
            }

        });

        this.actorByOrgUnit = new PickerHandler<Long>(this.getAttachmentManagerPlugin(), Long.class, new Handle<Long>() {

            @Override
            public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
                defaultParameters.put(Parameters.SEARCH_ENABLED, "false");
                return defaultParameters;
            }

            @Override
            public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
                Long orgUnitId = Long.valueOf(context.get("orgUnitId"));
                return ActorDao.getActorActiveAsVHByOrgUnit(orgUnitId);
            }

        });

        this.actorByCompetency = new PickerHandler<Long>(this.getAttachmentManagerPlugin(), Long.class, new Handle<Long>() {

            @Override
            public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
                defaultParameters.put(Parameters.SEARCH_ENABLED, "false");
                return defaultParameters;
            }

            @Override
            public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
                Long competencyId = Long.valueOf(context.get("competencyId"));
                return ActorDao.getActorActiveAsVHByCompetency(competencyId);
            }

        });

        this.budgetBucket = new PickerHandler<Long>(this.getAttachmentManagerPlugin(), Long.class, new Handle<Long>() {

            @Override
            public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
                defaultParameters.put(Parameters.SEARCH_ENABLED, "true");
                return defaultParameters;
            }

            @Override
            public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
                return BudgetBucketDAO.getBudgetBucketSelectableAsList();
            }

            @Override
            public ISelectableValueHolderCollection<Long> getFoundValueHolders(String searchString, Map<String, String> context) {
                searchString = searchString.replaceAll("\\*", "%");
                return BudgetBucketDAO.getBudgetBucketSelectableAsListByName(searchString);
            }

        });

        this.costCenter = new PickerHandler<Long>(this.getAttachmentManagerPlugin(), Long.class, new Handle<Long>() {

            @Override
            public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
                defaultParameters.put(Parameters.SEARCH_ENABLED, "true");
                return defaultParameters;
            }

            @Override
            public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
                return CostCenterDAO.getCostCenterSelectableAsVH();
            }

            @Override
            public ISelectableValueHolderCollection<Long> getFoundValueHolders(String searchString, Map<String, String> context) {
                searchString = searchString.replaceAll("\\*", "%");
                return CostCenterDAO.getCostCenterSelectableAsVHByName(searchString);
            }

        });

        this.deliverableByPortfolioEntry = new PickerHandler<Long>(this.getAttachmentManagerPlugin(), Long.class, new Handle<Long>() {

            @Override
            public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
                defaultParameters.put(Parameters.SEARCH_ENABLED, "false");
                return defaultParameters;
            }

            @Override
            public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
                Long portfolioEntryId = Long.valueOf(context.get("portfolioEntryId"));
                return DeliverableDAO.getDeliverableOwnerAsVHByPE(portfolioEntryId);
            }

        });

        this.orgUnit = new PickerHandler<Long>(this.getAttachmentManagerPlugin(), Long.class, new Handle<Long>() {

            @Override
            public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
                defaultParameters.put(Parameters.SEARCH_ENABLED, "true");
                return defaultParameters;
            }

            @Override
            public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
                return OrgUnitDao.getOrgUnitActiveAsVH();
            }

            @Override
            public ISelectableValueHolderCollection<Long> getFoundValueHolders(String searchString, Map<String, String> context) {
                searchString = searchString.replaceAll("\\*", "%");
                return OrgUnitDao.getOrgUnitActiveAsVHByKeywords(searchString);
            }

        });

        this.deliveryUnit = new PickerHandler<Long>(this.getAttachmentManagerPlugin(), Long.class, new Handle<Long>() {

            @Override
            public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
                defaultParameters.put(Parameters.SEARCH_ENABLED, "true");
                return defaultParameters;
            }

            @Override
            public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
                return OrgUnitDao.getOrgUnitActiveCanDeliverAsVH();
            }

            @Override
            public ISelectableValueHolderCollection<Long> getFoundValueHolders(String searchString, Map<String, String> context) {
                searchString = searchString.replaceAll("\\*", "%");
                return OrgUnitDao.getOrgUnitActiveCanDeliverAsVHByKeywords(searchString);
            }

        });

        this.sponsoringUnit = new PickerHandler<Long>(this.getAttachmentManagerPlugin(), Long.class, new Handle<Long>() {

            @Override
            public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
                defaultParameters.put(Parameters.SEARCH_ENABLED, "true");
                return defaultParameters;
            }

            @Override
            public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
                return OrgUnitDao.getOrgUnitActiveCanSponsorAsVH();
            }

            @Override
            public ISelectableValueHolderCollection<Long> getFoundValueHolders(String searchString, Map<String, String> context) {
                searchString = searchString.replaceAll("\\*", "%");
                return OrgUnitDao.getOrgUnitActiveCanSponsorAsVHByKeywords(searchString);
            }

        });

        this.planningPackage = new PickerHandler<Long>(this.getAttachmentManagerPlugin(), Long.class, new Handle<Long>() {

            @Override
            public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
                defaultParameters.put(Parameters.SEARCH_ENABLED, "true");
                return defaultParameters;
            }

            @Override
            public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
                Long portfolioEntryId = Long.valueOf(context.get("portfolioEntryId"));
                return PortfolioEntryPlanningPackageDao.getPEPlanningPackageAsVHByPE(portfolioEntryId, getDisplayExpenditureType(context));
            }

            @Override
            public ISelectableValueHolderCollection<Long> getFoundValueHolders(String searchString, Map<String, String> context) {
                Long portfolioEntryId = Long.valueOf(context.get("portfolioEntryId"));
                searchString = searchString.replaceAll("\\*", "%");
                return PortfolioEntryPlanningPackageDao.getPEPlanningPackageAsVHByKeywordsAndPE(searchString, portfolioEntryId,
                        getDisplayExpenditureType(context));
            }

            /**
             * Return true if the expenditure type should be displayed.
             * 
             * @param context
             *            the context
             */
            private boolean getDisplayExpenditureType(Map<String, String> context) {
                String displayExpenditureTypeAsString = context.get("displayExpenditureType");
                if (displayExpenditureTypeAsString == null || displayExpenditureTypeAsString.equals("")) {
                    return false;
                } else {
                    return Boolean.valueOf(displayExpenditureTypeAsString);
                }
            }

        });

        this.portfolioEntryType = new PickerHandler<Long>(this.getAttachmentManagerPlugin(), Long.class, new Handle<Long>() {

            @Override
            public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
                defaultParameters.put(Parameters.SEARCH_ENABLED, "false");
                return defaultParameters;
            }

            @Override
            public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
                return PortfolioEntryDao.getPETypeActiveAsVH();
            }

        });

        this.portfolio = new PickerHandler<Long>(this.getAttachmentManagerPlugin(), Long.class, new Handle<Long>() {
            @Override
            public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
                return PortfolioDao.getPortfolioActiveAsVH();
            }
        });

        this.portfolioType = new PickerHandler<Long>(this.getAttachmentManagerPlugin(), Long.class, new Handle<Long>() {

            @Override
            public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
                defaultParameters.put(Parameters.SEARCH_ENABLED, "false");
                return defaultParameters;
            }

            @Override
            public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
                return PortfolioDao.getPortfolioTypeActiveAsVH();
            }

        });

    }

    @Override
    public PickerHandler<String> getPrincipal() {
        return this.principal;
    }

    @Override
    public PickerHandler<Long> getActor() {
        return this.actor;
    }

    @Override
    public PickerHandler<Long> getActorWithoutUid() {
        return this.actorWithoutUid;
    }

    @Override
    public PickerHandler<Long> getActorByOrgUnit() {
        return this.actorByOrgUnit;
    }

    @Override
    public PickerHandler<Long> getActorByCompetency() {
        return this.actorByCompetency;
    }

    @Override
    public PickerHandler<Long> getBudgetBucket() {
        return this.budgetBucket;
    }

    @Override
    public PickerHandler<Long> getCostCenter() {
        return this.costCenter;
    }

    @Override
    public PickerHandler<Long> getDeliverableByPortfolioEntry() {
        return this.deliverableByPortfolioEntry;
    }

    @Override
    public PickerHandler<Long> getOrgUnit() {
        return this.orgUnit;
    }

    @Override
    public PickerHandler<Long> getDeliveryUnit() {
        return this.deliveryUnit;
    }

    @Override
    public PickerHandler<Long> getSponsoringUnit() {
        return this.sponsoringUnit;
    }

    @Override
    public PickerHandler<Long> getPlanningPackage() {
        return this.planningPackage;
    }

    @Override
    public PickerHandler<Long> getPortfolioEntryType() {
        return this.portfolioEntryType;
    }

    @Override
    public PickerHandler<Long> getPortfolio() {
        return this.portfolio;
    }

    @Override
    public PickerHandler<Long> getPortfolioType() {
        return this.portfolioType;
    }

    /**
     * Get the account manager service.
     */
    private IAccountManagerPlugin getAccountManagerPlugin() {
        return this.accountManagerPlugin;
    }

    /**
     * Get the attachment manager plugin.
     */
    private IAttachmentManagerPlugin getAttachmentManagerPlugin() {
        return this.attachmentManagerPlugin;
    }

}
