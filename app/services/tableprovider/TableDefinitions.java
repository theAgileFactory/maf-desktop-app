package services.tableprovider;

import framework.services.configuration.II18nMessagesPlugin;
import framework.services.kpi.IKpiService;
import framework.services.storage.IAttachmentManagerPlugin;
import utils.table.ActorListView;
import utils.table.ActorTypeListView;
import utils.table.ApplicationBlockListView;
import utils.table.AttachmentListView;
import utils.table.BudgetBucketLineListView;
import utils.table.BudgetBucketListView;
import utils.table.CompetencyListView;
import utils.table.CurrencyListView;
import utils.table.CustomAttributeItemListView;
import utils.table.CustomAttributeListView;
import utils.table.DataSyndicationAgreementLinkListView;
import utils.table.DataSyndicationAgreementListView;
import utils.table.DataSyndicationPartnerListView;
import utils.table.DeliverableListView;
import utils.table.GoodsReceiptListView;
import utils.table.GovernanceListView;
import utils.table.IterationListView;
import utils.table.KpiColorRuleListView;
import utils.table.KpiDefinitionListView;
import utils.table.KpiValueDefinitionListView;
import utils.table.LifeCycleMilestoneInstanceStatusTypeListView;
import utils.table.LifeCycleMilestoneListView;
import utils.table.LifeCyclePhaseListView;
import utils.table.LifeCycleProcessListView;
import utils.table.MessageListView;
import utils.table.MilestoneApprovalListView;
import utils.table.MilestoneApproverListView;
import utils.table.MilestoneRequestListView;
import utils.table.NotificationListView;
import utils.table.OrgUnitListView;
import utils.table.OrgUnitTypeListView;
import utils.table.PortfolioEntryBudgetLineListView;
import utils.table.PortfolioEntryDependencyListView;
import utils.table.PortfolioEntryDependencyTypeListView;
import utils.table.PortfolioEntryEventListView;
import utils.table.PortfolioEntryEventTypeListView;
import utils.table.PortfolioEntryListView;
import utils.table.PortfolioEntryPlanningPackageGroupListView;
import utils.table.PortfolioEntryPlanningPackageListView;
import utils.table.PortfolioEntryPlanningPackagePatternListView;
import utils.table.PortfolioEntryPlanningPackageTypeListView;
import utils.table.PortfolioEntryReportListView;
import utils.table.PortfolioEntryReportStatusTypeListView;
import utils.table.PortfolioEntryResourcePlanAllocatedActorListView;
import utils.table.PortfolioEntryResourcePlanAllocatedOrgUnitListView;
import utils.table.PortfolioEntryResourcePlanAllocatedResourceListView;
import utils.table.PortfolioEntryRiskListView;
import utils.table.PortfolioEntryRiskTypeListView;
import utils.table.PortfolioEntryStakeholderListView;
import utils.table.PortfolioEntryTypeListView;
import utils.table.PortfolioListView;
import utils.table.PortfolioMilestoneListView;
import utils.table.PortfolioReportListView;
import utils.table.PortfolioStakeholderListView;
import utils.table.PortfolioTypeListView;
import utils.table.PurchaseOrderLineItemListView;
import utils.table.PurchaseOrderLineItemWorkOrderListView;
import utils.table.PurchaseOrderListView;
import utils.table.ReportingListView;
import utils.table.RequirementListView;
import utils.table.RequirementPriorityListView;
import utils.table.RequirementSeverityListView;
import utils.table.RequirementStatusListView;
import utils.table.RoleListView;
import utils.table.StakeholderTypeListView;
import utils.table.TimesheetActivityAllocatedActorListView;
import utils.table.TimesheetActivityListView;
import utils.table.TimesheetActivityTypeListView;
import utils.table.TimesheetLogListView;
import utils.table.TimesheetReportListView;
import utils.table.WorkOrderListView;

/**
 * The table definitions.
 * 
 * @author Johann Kohler
 *
 */
public class TableDefinitions {

    public ActorListView.TableDefinition actor;
    public ActorTypeListView.TableDefinition actorType;
    public ApplicationBlockListView.TableDefinition applicationBlock;
    public AttachmentListView.TableDefinition attachment;
    public BudgetBucketLineListView.TableDefinition budgetBucketLine;
    public BudgetBucketListView.TableDefinition budgetBucket;
    public CompetencyListView.TableDefinition competency;
    public CurrencyListView.TableDefinition currency;
    public CustomAttributeItemListView.TableDefinition customAttributeItem;
    public CustomAttributeListView.TableDefinition customAttribute;
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
    public OrgUnitTypeListView.TableDefinition orgUnitType;
    public PortfolioEntryBudgetLineListView.TableDefinition portfolioEntryBudgetLine;
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
    public PortfolioEntryResourcePlanAllocatedActorListView.TableDefinition portfolioEntryResourcePlanAllocatedActor;
    public PortfolioEntryResourcePlanAllocatedOrgUnitListView.TableDefinition portfolioEntryResourcePlanAllocatedOrgUnit;
    public PortfolioEntryResourcePlanAllocatedResourceListView.TableDefinition portfolioEntryResourcePlanAllocatedResource;
    public PortfolioEntryRiskListView.TableDefinition portfolioEntryRisk;
    public PortfolioEntryRiskTypeListView.TableDefinition portfolioEntryRiskType;
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
     * 
     * @param kpiService
     *            the KPI service
     * @param i18nMessagesService
     *            the i18n messages service
     * @param attachmentManagerPlugin
     *            the attachment manager service
     */
    public TableDefinitions(IKpiService kpiService, II18nMessagesPlugin i18nMessagesService, IAttachmentManagerPlugin attachmentManagerPlugin) {
        this.actor = new ActorListView.TableDefinition(i18nMessagesService);
        this.actorType = new ActorTypeListView.TableDefinition();
        this.applicationBlock = new ApplicationBlockListView.TableDefinition(i18nMessagesService);
        this.attachment = new AttachmentListView.TableDefinition(i18nMessagesService, attachmentManagerPlugin);
        this.budgetBucketLine = new BudgetBucketLineListView.TableDefinition(i18nMessagesService);
        this.budgetBucket = new BudgetBucketListView.TableDefinition(i18nMessagesService);
        this.competency = new CompetencyListView.TableDefinition();
        this.currency = new CurrencyListView.TableDefinition();
        this.customAttributeItem = new CustomAttributeItemListView.TableDefinition();
        this.customAttribute = new CustomAttributeListView.TableDefinition();
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
        this.orgUnitType = new OrgUnitTypeListView.TableDefinition();
        this.portfolioEntryBudgetLine = new PortfolioEntryBudgetLineListView.TableDefinition(i18nMessagesService);
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
        this.portfolioEntryResourcePlanAllocatedActor = new PortfolioEntryResourcePlanAllocatedActorListView.TableDefinition(i18nMessagesService);
        this.portfolioEntryResourcePlanAllocatedOrgUnit = new PortfolioEntryResourcePlanAllocatedOrgUnitListView.TableDefinition(i18nMessagesService);
        this.portfolioEntryResourcePlanAllocatedResource = new PortfolioEntryResourcePlanAllocatedResourceListView.TableDefinition();
        this.portfolioEntryRisk = new PortfolioEntryRiskListView.TableDefinition(i18nMessagesService);
        this.portfolioEntryRiskType = new PortfolioEntryRiskTypeListView.TableDefinition();
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
