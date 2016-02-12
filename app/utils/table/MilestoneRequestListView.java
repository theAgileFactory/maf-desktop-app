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
package utils.table;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import dao.governance.LifeCycleMilestoneDao;
import dao.pmo.PortfolioEntryDao;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.ObjectFormatter;
import models.framework_models.common.Attachment;
import models.governance.LifeCycleMilestone;
import models.governance.ProcessTransitionRequest;
import models.pmo.Actor;
import models.pmo.PortfolioEntry;
import play.Logger;
import utils.form.RequestMilestoneFormData;

/**
 * A milestone request list view is used to display a milestone approval request
 * row in a table.
 * 
 * @author Johann Kohler
 */
public class MilestoneRequestListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<MilestoneRequestListView> templateTable;

        public TableDefinition() {
            this.templateTable = getTable();
        }

        public Table<MilestoneRequestListView> getTable() {
            return new Table<MilestoneRequestListView>() {
                {
                    setIdFieldName("id");

                    addColumn("portfolioEntryGovernanceId", "portfolioEntryGovernanceId", "object.portfolio_entry.governance_id.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryGovernanceId", new ObjectFormatter<MilestoneRequestListView>());

                    addColumn("portfolioEntry", "portfolioEntry", "object.process_transition_request.portfolio_entry.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntry", new IColumnFormatter<MilestoneRequestListView>() {
                        @Override
                        public String apply(MilestoneRequestListView requestListView, Object value) {
                            return views.html.modelsparts.display_portfolio_entry.render(requestListView.portfolioEntry, true).body();
                        }
                    });
                    this.setColumnValueCssClass("portfolioEntry", "rowlink-skip");

                    addColumn("lifeCycleName", "lifeCycleName", "object.portfolio_entry.life_cycle_process.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("lifeCycleName", new ObjectFormatter<MilestoneRequestListView>());

                    addColumn("milestone", "milestone", "object.process_transition_request.life_cycle_milestone.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("milestone", new IColumnFormatter<MilestoneRequestListView>() {
                        @Override
                        public String apply(MilestoneRequestListView requestListView, Object value) {
                            return views.html.modelsparts.display_milestone.render(requestListView.milestone).body();
                        }
                    });

                    addColumn("requester", "requester", "object.process_transition_request.requester.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("requester", new IColumnFormatter<MilestoneRequestListView>() {
                        @Override
                        public String apply(MilestoneRequestListView requestListView, Object value) {
                            return views.html.modelsparts.display_actor.render(requestListView.requester).body();
                        }
                    });
                    this.setColumnValueCssClass("requester", "rowlink-skip");

                    addColumn("passedDate", "passedDate", "object.process_transition_request.passed_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("passedDate", new DateFormatter<MilestoneRequestListView>());

                    addColumn("creationDate", "creationDate", "object.process_transition_request.creation_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("creationDate", new DateFormatter<MilestoneRequestListView>());

                    this.setLineAction(new IColumnFormatter<MilestoneRequestListView>() {
                        @Override
                        public String apply(MilestoneRequestListView requestListView, Object value) {
                            return controllers.core.routes.ProcessTransitionRequestController
                                    .processMilestoneRequest(requestListView.portfolioEntry.id, requestListView.id).url();
                        }
                    });

                    setEmptyMessageKey("object.process_transition_request.table.milestone.empty");
                }
            };

        }
    }

    public Long id;

    public Actor requester;
    public Date creationDate;
    public Date reviewDate;

    public Date passedDate;
    public String portfolioEntryGovernanceId;
    public PortfolioEntry portfolioEntry;
    public String lifeCycleName;
    public LifeCycleMilestone milestone;

    /**
     * Construct a row with a DB entry.
     * 
     * @param attachmentManagerPlugin
     *            the attachment manager service
     * @param request
     *            the process transition request in the DB
     */
    public MilestoneRequestListView(IAttachmentManagerPlugin attachmentManagerPlugin, ProcessTransitionRequest request) {

        this.id = request.id;
        this.requester = request.requester;
        this.creationDate = request.creationDate;
        this.reviewDate = request.reviewDate;

        this.portfolioEntry = null;
        this.milestone = null;
        if (request.requestType.equals(ProcessTransitionRequest.RequestType.MILESTONE_APPROVAL.name())) {

            List<Attachment> structuredDocumentAttachments = attachmentManagerPlugin.getAttachmentsFromObjectTypeAndObjectId(ProcessTransitionRequest.class,
                    request.id, true);
            if (structuredDocumentAttachments != null && structuredDocumentAttachments.size() > 0) {
                RequestMilestoneFormData requestMilestoneFormData = (RequestMilestoneFormData) Utilities
                        .unmarshallObject(structuredDocumentAttachments.get(0).structuredDocument.content);

                try {
                    this.passedDate = Utilities.getDateFormat(null).parse(requestMilestoneFormData.passedDate);
                } catch (ParseException e) {
                    Logger.error("impossible to parse the passed date of the milestone " + requestMilestoneFormData.milestoneId, e);
                }

                PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(requestMilestoneFormData.id);
                if (portfolioEntry != null) {
                    this.portfolioEntry = portfolioEntry;
                    this.portfolioEntryGovernanceId = portfolioEntry.governanceId;

                    this.lifeCycleName = portfolioEntry.activeLifeCycleInstance.lifeCycleProcess.getShortName();
                }

                this.milestone = LifeCycleMilestoneDao.getLCMilestoneById(requestMilestoneFormData.milestoneId);

            }

        }

    }
}
