package services.tableprovider;

import framework.services.account.IPreferenceManagerPlugin;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.kpi.IKpiService;
import framework.services.storage.IAttachmentManagerPlugin;
import utils.table.*;

/**
 * The table definitions.
 * 
 * @author Johann Kohler
 *
 */
public class TableDefinitions {

    public ActorListView.TableDefinition actor;
    public ActorAllocatedPortfolioEntryListView.ActorAllocatedPortfolioEntryTableDefinition actorAllocatedPortfolioEntry;
    public ActorTypeListView.TableDefinition actorType;
    public ApplicationBlockListView.TableDefinition applicationBlock;
    public AttachmentListView.TableDefinition attachment;
    public AttachmentManagementListView.TableDefinition attachmentManagement;
    public PortfolioEntryAttachmentListView.TableDefinition docsTableDefinition;
    public BudgetBucketLineListView.TableDefinition budgetBucketLine;
    public BudgetBucketListView.TableDefinition budgetBucket;
    public CompetencyListView.TableDefinition competency;
    public CurrencyListView.TableDefinition currency;
    public CustomAttributeItemListView.TableDefinition customAttributeItem;
    public CustomAttributeListView.TableDefinition customAttribute;
    public CustomAttributeGroupListView.TableDefinition customAttributeGroup;
    public DataSyndicationAgreementLinkListView.TableDefinition dataSyndicationAgreementLink;
    public DataSyndicationAgreementListView.TableDefinition dataSyndicationAgreement;
    public DataSyndicationPartnerListView.TableDefinition dataSyndicationPartner;
    public DeliverableListView.TableDefinition deliverable;
    public GoodsReceiptListView.TableDefinition goodsReceipt;
    public GovernanceListView.TableDefinition governance;
    public IterationListView.TableDefinition iteration;
    public KpiColorRuleListView.TableDefinition kpiColorRule;
    public KpiDefinitionListView.TableDefinition kpiDefinition;
    public KpiValueDefinitionListView.TableDefinition kpiValueDefinition;
    public LifeCycleMilestoneInstanceStatusTypeListView.TableDefinition lifeCycleMilestoneInstanceStatusType;
    public LifeCycleMilestoneListView.TableDefinition lifeCycleMilestone;
    public LifeCyclePhaseListView.TableDefinition lifeCyclePhase;
    public LifeCycleProcessListView.TableDefinition lifeCycleProcess;
    public MessageListView.TableDefinition message;
    public MilestoneApprovalListView.TableDefinition milestoneApproval;
    public MilestoneApproverListView.TableDefinition milestoneApprover;
    public MilestoneRequestListView.TableDefinition milestoneRequest;
    public NotificationListView.TableDefinition notification;
    public OrgUnitListView.TableDefinition orgUnit;
    public OrgUnitAllocatedActorListView.OrgUnitAllocatedActorTableDefinition orgUnitAllocatedActor;
    public OrgUnitTypeListView.TableDefinition orgUnitType;
    public OrgUnitAllocationRequestListView.OrgUnitAllocationRequestTableDefinition orgUnitAllocationRequest;
    public PortfolioEntryBudgetLineListView.TableDefinition portfolioEntryBudgetLine;
    public PortfolioEntryBudgetLineTypeListView.TableDefinition portfolioEntryBudgetLineType;
    public PortfolioEntryDependencyListView.TableDefinition portfolioEntryDependency;
    public PortfolioEntryDependencyTypeListView.TableDefinition portfolioEntryDependencyType;
    public PortfolioEntryEventListView.TableDefinition portfolioEntryEvent;
    public PortfolioEntryEventTypeListView.TableDefinition portfolioEntryEventType;
    public PortfolioEntryListView.TableDefinition portfolioEntry;
    public PortfolioEntryPlanningPackageGroupListView.TableDefinition portfolioEntryPlanningPackageGroup;
    public PortfolioEntryPlanningPackageListView.TableDefinition portfolioEntryPlanningPackage;
    public PortfolioEntryPlanningPackagePatternListView.TableDefinition portfolioEntryPlanningPackagePattern;
    public PortfolioEntryPlanningPackageTypeListView.TableDefinition portfolioEntryPlanningPackageType;
    public PortfolioEntryReportListView.TableDefinition portfolioEntryReport;
    public PortfolioEntryReportStatusTypeListView.TableDefinition portfolioEntryReportStatusType;
    public PortfolioEntryResourcePlanAllocatedActorListView.PortfolioEntryResourcePlanAllocatedActorTableDefinition portfolioEntryResourcePlanAllocatedActor;
    public PortfolioEntryResourcePlanAllocatedOrgUnitListView.PortfolioEntryResourcePlanTableDefinition portfolioEntryResourcePlanAllocatedOrgUnit;
    public PortfolioEntryResourcePlanAllocatedCompetencyListView.TableDefinition portfolioEntryResourcePlanAllocatedCompetency;
    public PortfolioEntryResourcePlanAllocatedResourceListView.TableDefinition portfolioEntryResourcePlanAllocatedResource;
    public PortfolioEntryRiskListView.TableDefinition portfolioEntryRisk;
    public PortfolioEntryRiskTypeListView.TableDefinition portfolioEntryRiskType;
    public PortfolioEntryIssueListView.TableDefinition portfolioEntryIssue;
    public PortfolioEntryIssueTypeListView.TableDefinition portfolioEntryIssueType;
    public PortfolioEntryStakeholderListView.TableDefinition portfolioEntryStakeholder;
    public PortfolioEntryTypeListView.TableDefinition portfolioEntryType;
    public PortfolioListView.TableDefinition portfolio;
    public PortfolioMilestoneListView.TableDefinition portfolioMilestone;
    public PortfolioReportListView.TableDefinition portfolioReport;
    public PortfolioStakeholderListView.TableDefinition portfolioStakeholder;
    public PortfolioTypeListView.TableDefinition portfolioType;
    public PurchaseOrderLineItemListView.TableDefinition purchaseOrderLineItem;
    public PurchaseOrderLineItemWorkOrderListView.TableDefinition purchaseOrderLineItemWorkOrder;
    public PurchaseOrderListView.TableDefinition purchaseOrder;
    public ReportingListView.TableDefinition reporting;
    public RequirementListView.TableDefinition requirement;
    public RequirementPriorityListView.TableDefinition requirementPriority;
    public RequirementSeverityListView.TableDefinition requirementSeverity;
    public RequirementStatusListView.TableDefinition requirementStatus;
    public RoleListView.TableDefinition role;
    public StakeholderTypeListView.TableDefinition stakeholderType;
    public TimesheetActivityAllocatedActorListView.TableDefinition timesheetActivityAllocatedActor;
    public TimesheetActivityListView.TableDefinition timesheetActivity;
    public TimesheetActivityTypeListView.TableDefinition timesheetActivityType;
    public TimesheetLogListView.TableDefinition timesheetLog;
    public TimesheetReportListView.TableDefinition timesheetReport;
    public WorkOrderListView.TableDefinition workOrder;

    /**
     * Default constructor.
     *  @param kpiService
     *            the KPI service
     * @param i18nMessagesService
     *            the i18n messages service
     * @param attachmentManagerPlugin
     *            the attachment manager service
     * @param iPreferenceManagerPlugin
     *            the preference manager service
     */
    public TableDefinitions(IKpiService kpiService, II18nMessagesPlugin i18nMessagesService, IAttachmentManagerPlugin attachmentManagerPlugin, IPreferenceManagerPlugin iPreferenceManagerPlugin) {
        this.actor = new ActorListView.TableDefinition(i18nMessagesService);
        this.actorAllocatedPortfolioEntry = new ActorAllocatedPortfolioEntryListView.ActorAllocatedPortfolioEntryTableDefinition(i18nMessagesService);
        this.actorType = new ActorTypeListView.TableDefinition();
        this.applicationBlock = new ApplicationBlockListView.TableDefinition(i18nMessagesService);
        this.attachment = new AttachmentListView.TableDefinition(i18nMessagesService, attachmentManagerPlugin);
        this.attachmentManagement = new AttachmentManagementListView.TableDefinition(i18nMessagesService, attachmentManagerPlugin);
        this.docsTableDefinition = new PortfolioEntryAttachmentListView.TableDefinition(i18nMessagesService);
        this.budgetBucketLine = new BudgetBucketLineListView.TableDefinition(i18nMessagesService);
        this.budgetBucket = new BudgetBucketListView.TableDefinition(i18nMessagesService);
        this.competency = new CompetencyListView.TableDefinition();
        this.currency = new CurrencyListView.TableDefinition();
        this.customAttributeItem = new CustomAttributeItemListView.TableDefinition();
        this.customAttribute = new CustomAttributeListView.TableDefinition();
        this.customAttributeGroup = new CustomAttributeGroupListView.TableDefinition();
        this.dataSyndicationAgreementLink = new DataSyndicationAgreementLinkListView.TableDefinition();
        this.dataSyndicationAgreement = new DataSyndicationAgreementListView.TableDefinition();
        this.dataSyndicationPartner = new DataSyndicationPartnerListView.TableDefinition();
        this.deliverable = new DeliverableListView.TableDefinition(i18nMessagesService);
        this.goodsReceipt = new GoodsReceiptListView.TableDefinition();
        this.governance = new GovernanceListView.TableDefinition();
        this.iteration = new IterationListView.TableDefinition(i18nMessagesService);
        this.kpiColorRule = new KpiColorRuleListView.TableDefinition();
        this.kpiDefinition = new KpiDefinitionListView.TableDefinition();
        this.kpiValueDefinition = new KpiValueDefinitionListView.TableDefinition();
        this.lifeCycleMilestoneInstanceStatusType = new LifeCycleMilestoneInstanceStatusTypeListView.TableDefinition();
        this.lifeCycleMilestone = new LifeCycleMilestoneListView.TableDefinition();
        this.lifeCyclePhase = new LifeCyclePhaseListView.TableDefinition();
        this.lifeCycleProcess = new LifeCycleProcessListView.TableDefinition();
        this.message = new MessageListView.TableDefinition();
        this.milestoneApproval = new MilestoneApprovalListView.TableDefinition();
        this.milestoneApprover = new MilestoneApproverListView.TableDefinition();
        this.milestoneRequest = new MilestoneRequestListView.TableDefinition();
        this.notification = new NotificationListView.TableDefinition();
        this.orgUnit = new OrgUnitListView.TableDefinition(i18nMessagesService);
        this.orgUnitAllocatedActor = new OrgUnitAllocatedActorListView.OrgUnitAllocatedActorTableDefinition(i18nMessagesService);
        this.orgUnitType = new OrgUnitTypeListView.TableDefinition();
        this.orgUnitAllocationRequest = new OrgUnitAllocationRequestListView.OrgUnitAllocationRequestTableDefinition(i18nMessagesService);
        this.portfolioEntryBudgetLine = new PortfolioEntryBudgetLineListView.TableDefinition(i18nMessagesService, iPreferenceManagerPlugin);
        this.portfolioEntryBudgetLineType = new PortfolioEntryBudgetLineTypeListView.TableDefinition(i18nMessagesService);
        this.portfolioEntryDependency = new PortfolioEntryDependencyListView.TableDefinition();
        this.portfolioEntryDependencyType = new PortfolioEntryDependencyTypeListView.TableDefinition();
        this.portfolioEntryEvent = new PortfolioEntryEventListView.TableDefinition(i18nMessagesService);
        this.portfolioEntryEventType = new PortfolioEntryEventTypeListView.TableDefinition();
        this.portfolioEntry = new PortfolioEntryListView.TableDefinition(kpiService, i18nMessagesService);
        this.portfolioEntryPlanningPackageGroup = new PortfolioEntryPlanningPackageGroupListView.TableDefinition();
        this.portfolioEntryPlanningPackage = new PortfolioEntryPlanningPackageListView.TableDefinition(i18nMessagesService);
        this.portfolioEntryPlanningPackagePattern = new PortfolioEntryPlanningPackagePatternListView.TableDefinition();
        this.portfolioEntryPlanningPackageType = new PortfolioEntryPlanningPackageTypeListView.TableDefinition();
        this.portfolioEntryReport = new PortfolioEntryReportListView.TableDefinition(i18nMessagesService);
        this.portfolioEntryReportStatusType = new PortfolioEntryReportStatusTypeListView.TableDefinition();
        this.portfolioEntryResourcePlanAllocatedActor = new PortfolioEntryResourcePlanAllocatedActorListView.PortfolioEntryResourcePlanAllocatedActorTableDefinition(i18nMessagesService);
        this.portfolioEntryResourcePlanAllocatedOrgUnit = new PortfolioEntryResourcePlanAllocatedOrgUnitListView.PortfolioEntryResourcePlanTableDefinition(i18nMessagesService);
        this.portfolioEntryResourcePlanAllocatedCompetency = new PortfolioEntryResourcePlanAllocatedCompetencyListView.TableDefinition(i18nMessagesService);
        this.portfolioEntryResourcePlanAllocatedResource = new PortfolioEntryResourcePlanAllocatedResourceListView.TableDefinition(i18nMessagesService);
        this.portfolioEntryRisk = new PortfolioEntryRiskListView.TableDefinition(i18nMessagesService);
        this.portfolioEntryRiskType = new PortfolioEntryRiskTypeListView.TableDefinition();
        this.portfolioEntryIssue = new PortfolioEntryIssueListView.TableDefinition(i18nMessagesService);
        this.portfolioEntryIssueType = new PortfolioEntryIssueTypeListView.TableDefinition();
        this.portfolioEntryStakeholder = new PortfolioEntryStakeholderListView.TableDefinition();
        this.portfolioEntryType = new PortfolioEntryTypeListView.TableDefinition();
        this.portfolio = new PortfolioListView.TableDefinition(i18nMessagesService);
        this.portfolioMilestone = new PortfolioMilestoneListView.TableDefinition();
        this.portfolioReport = new PortfolioReportListView.TableDefinition();
        this.portfolioStakeholder = new PortfolioStakeholderListView.TableDefinition();
        this.portfolioType = new PortfolioTypeListView.TableDefinition();
        this.purchaseOrderLineItem = new PurchaseOrderLineItemListView.TableDefinition();
        this.purchaseOrderLineItemWorkOrder = new PurchaseOrderLineItemWorkOrderListView.TableDefinition(i18nMessagesService);
        this.purchaseOrder = new PurchaseOrderListView.TableDefinition();
        this.reporting = new ReportingListView.TableDefinition();
        this.requirement = new RequirementListView.TableDefinition(i18nMessagesService);
        this.requirementPriority = new RequirementPriorityListView.TableDefinition();
        this.requirementSeverity = new RequirementSeverityListView.TableDefinition();
        this.requirementStatus = new RequirementStatusListView.TableDefinition();
        this.role = new RoleListView.TableDefinition();
        this.stakeholderType = new StakeholderTypeListView.TableDefinition();
        this.timesheetActivityAllocatedActor = new TimesheetActivityAllocatedActorListView.TableDefinition(i18nMessagesService);
        this.timesheetActivity = new TimesheetActivityListView.TableDefinition();
        this.timesheetActivityType = new TimesheetActivityTypeListView.TableDefinition();
        this.timesheetLog = new TimesheetLogListView.TableDefinition();
        this.timesheetReport = new TimesheetReportListView.TableDefinition();
        this.workOrder = new WorkOrderListView.TableDefinition(i18nMessagesService);
    }

}
