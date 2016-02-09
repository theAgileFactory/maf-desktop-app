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
package controllers.admin;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import dao.pmo.PortfolioEntryEventDao;
import dao.pmo.PortfolioEntryReportDao;
import dao.pmo.PortfolioEntryRiskDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.Color;
import framework.utils.Icon;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import models.pmo.PortfolioEntryEventType;
import models.pmo.PortfolioEntryReportStatusType;
import models.pmo.PortfolioEntryRiskType;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import services.tableprovider.ITableProvider;
import utils.form.PortfolioEntryEventTypeFormData;
import utils.form.PortfolioEntryReportStatusTypeFormData;
import utils.form.PortfolioEntryRiskTypeFormData;
import utils.table.PortfolioEntryEventTypeListView;
import utils.table.PortfolioEntryReportStatusTypeListView;
import utils.table.PortfolioEntryRiskTypeListView;

/**
 * Manage the registers reference data.
 * 
 * @author Johann Kohler
 * 
 */
@Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
public class ConfigurationRegisterController extends Controller {

    private static Form<PortfolioEntryRiskTypeFormData> riskTypeFormTemplate = Form.form(PortfolioEntryRiskTypeFormData.class);
    private static Form<PortfolioEntryReportStatusTypeFormData> reportStatusTypeFormTemplate = Form.form(PortfolioEntryReportStatusTypeFormData.class);
    private static Form<PortfolioEntryEventTypeFormData> eventTypeFormTemplate = Form.form(PortfolioEntryEventTypeFormData.class);

    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;

    @Inject
    private ITableProvider tableProvider;

    /**
     * Display the lists of data.
     */
    public Result list() {

        // risk types
        List<PortfolioEntryRiskType> portfolioEntryRiskTypes = PortfolioEntryRiskDao.getPERiskTypeAsList();

        List<PortfolioEntryRiskTypeListView> portfolioEntryRiskTypesListView = new ArrayList<PortfolioEntryRiskTypeListView>();
        for (PortfolioEntryRiskType portfolioEntryRiskType : portfolioEntryRiskTypes) {
            portfolioEntryRiskTypesListView.add(new PortfolioEntryRiskTypeListView(portfolioEntryRiskType));
        }

        Table<PortfolioEntryRiskTypeListView> portfolioEntryRiskTypesTable = PortfolioEntryRiskTypeListView.templateTable
                .fill(portfolioEntryRiskTypesListView);

        // report status types
        List<PortfolioEntryReportStatusType> portfolioEntryReportStatusTypes = PortfolioEntryReportDao.getPEReportStatusTypeAsList();

        List<PortfolioEntryReportStatusTypeListView> portfolioEntryReportStatusTypeListView = new ArrayList<PortfolioEntryReportStatusTypeListView>();
        for (PortfolioEntryReportStatusType portfolioEntryReportStatusType : portfolioEntryReportStatusTypes) {
            portfolioEntryReportStatusTypeListView.add(new PortfolioEntryReportStatusTypeListView(portfolioEntryReportStatusType, getI18nMessagesPlugin()));
        }

        Table<PortfolioEntryReportStatusTypeListView> portfolioEntryReportStatusTypesTable = PortfolioEntryReportStatusTypeListView.templateTable
                .fill(portfolioEntryReportStatusTypeListView);

        // event types
        List<PortfolioEntryEventType> portfolioEntryEventTypes = PortfolioEntryEventDao.getPEEventTypeAsList();

        List<PortfolioEntryEventTypeListView> portfolioEntryEventTypeListView = new ArrayList<PortfolioEntryEventTypeListView>();
        for (PortfolioEntryEventType portfolioEntryEventType : portfolioEntryEventTypes) {
            portfolioEntryEventTypeListView.add(new PortfolioEntryEventTypeListView(portfolioEntryEventType));
        }

        Table<PortfolioEntryEventTypeListView> portfolioEntryEventTypesTable = PortfolioEntryEventTypeListView.templateTable
                .fill(portfolioEntryEventTypeListView);

        return ok(views.html.admin.config.datareference.register.list.render(portfolioEntryRiskTypesTable, portfolioEntryReportStatusTypesTable,
                portfolioEntryEventTypesTable));
    }

    /**
     * Edit or create a risk type.
     * 
     * @param riskTypeId
     *            the risk type id (set 0 for create case)
     */
    public Result manageRiskType(Long riskTypeId) {

        // initiate the form with the template
        Form<PortfolioEntryRiskTypeFormData> riskTypeForm = riskTypeFormTemplate;

        // edit case: inject values
        if (!riskTypeId.equals(Long.valueOf(0))) {

            PortfolioEntryRiskType riskType = PortfolioEntryRiskDao.getPERiskTypeById(riskTypeId);

            riskTypeForm = riskTypeFormTemplate.fill(new PortfolioEntryRiskTypeFormData(riskType, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.register.risk_type_manage.render(riskTypeForm));

    }

    /**
     * Process the edit/create form of a risk type.
     */
    public Result processManageRiskType() {

        // bind the form
        Form<PortfolioEntryRiskTypeFormData> boundForm = riskTypeFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.register.risk_type_manage.render(boundForm));
        }

        PortfolioEntryRiskTypeFormData riskTypeFormData = boundForm.get();

        PortfolioEntryRiskType riskType = null;

        if (riskTypeFormData.id == null) { // create case

            riskType = new PortfolioEntryRiskType();

            riskTypeFormData.fill(riskType);
            riskType.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.risktype.add.successful"));

        } else { // edit case

            riskType = PortfolioEntryRiskDao.getPERiskTypeById(riskTypeFormData.id);

            riskTypeFormData.fill(riskType);
            riskType.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.risktype.edit.successful"));
        }

        riskTypeFormData.description.persist(getI18nMessagesPlugin());
        riskTypeFormData.name.persist(getI18nMessagesPlugin());

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationRegisterController.list());
    }

    /**
     * Delete a risk type.
     * 
     * @param riskTypeId
     *            the risk type id
     */
    public Result deleteRiskType(Long riskTypeId) {

        PortfolioEntryRiskType riskType = PortfolioEntryRiskDao.getPERiskTypeById(riskTypeId);

        riskType.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.risktype.delete.successful"));

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationRegisterController.list());

    }

    /**
     * Edit or create a report status type.
     * 
     * @param reportStatusTypeId
     *            the report status type id (set 0 for create case)
     */
    public Result manageReportStatusType(Long reportStatusTypeId) {

        Form<PortfolioEntryReportStatusTypeFormData> reportStatusTypeForm = reportStatusTypeFormTemplate;

        // edit case: inject values
        if (!reportStatusTypeId.equals(Long.valueOf(0))) {

            PortfolioEntryReportStatusType reportStatusType = PortfolioEntryReportDao.getPEReportStatusTypeById(reportStatusTypeId);

            reportStatusTypeForm = reportStatusTypeFormTemplate.fill(new PortfolioEntryReportStatusTypeFormData(reportStatusType, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.register.report_status_type_manage.render(reportStatusTypeForm,
                Color.getColorsAsValueHolderCollection(getI18nMessagesPlugin())));

    }

    /**
     * Process the edit/create form of a report status type.
     */
    public Result processManageReportStatusType() {

        // bind the form
        Form<PortfolioEntryReportStatusTypeFormData> boundForm = reportStatusTypeFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.register.report_status_type_manage.render(boundForm,
                    Color.getColorsAsValueHolderCollection(getI18nMessagesPlugin())));
        }

        PortfolioEntryReportStatusTypeFormData reportStatusTypeFormData = boundForm.get();

        PortfolioEntryReportStatusType reportStatusType = null;

        if (reportStatusTypeFormData.id == null) { // create case

            reportStatusType = new PortfolioEntryReportStatusType();

            reportStatusTypeFormData.fill(reportStatusType);
            reportStatusType.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.report_status_type.add.successful"));

        } else { // edit case

            reportStatusType = PortfolioEntryReportDao.getPEReportStatusTypeById(reportStatusTypeFormData.id);

            reportStatusTypeFormData.fill(reportStatusType);
            reportStatusType.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.report_status_type.edit.successful"));
        }

        reportStatusTypeFormData.description.persist(getI18nMessagesPlugin());
        reportStatusTypeFormData.name.persist(getI18nMessagesPlugin());

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationRegisterController.list());
    }

    /**
     * Delete a report status type.
     * 
     * @param reportStatusTypeId
     *            the report status type id
     */
    public Result deleteReportStatusType(Long reportStatusTypeId) {

        PortfolioEntryReportStatusType reportStatusType = PortfolioEntryReportDao.getPEReportStatusTypeById(reportStatusTypeId);

        reportStatusType.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.report_status_type.delete.successful"));

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationRegisterController.list());
    }

    /**
     * Edit or create an event type.
     * 
     * @param eventTypeId
     *            the event type id (set 0 for create case)
     */
    public Result manageEventType(Long eventTypeId) {

        Form<PortfolioEntryEventTypeFormData> eventTypeForm = eventTypeFormTemplate;

        // edit case: inject values
        if (!eventTypeId.equals(Long.valueOf(0))) {

            PortfolioEntryEventType eventType = PortfolioEntryEventDao.getPEEventTypeById(eventTypeId);

            eventTypeForm = eventTypeFormTemplate.fill(new PortfolioEntryEventTypeFormData(eventType, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.register.event_type_manage.render(eventTypeForm, Icon.getIconsAsVHC()));
    }

    /**
     * Process the edit/create form of an event type.
     */
    public Result processManageEventType() {

        // bind the form
        Form<PortfolioEntryEventTypeFormData> boundForm = eventTypeFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.register.event_type_manage.render(boundForm, Icon.getIconsAsVHC()));
        }

        PortfolioEntryEventTypeFormData eventTypeFormData = boundForm.get();

        PortfolioEntryEventType eventType = null;

        if (eventTypeFormData.id == null) { // create case

            eventType = new PortfolioEntryEventType();

            eventTypeFormData.fill(eventType);
            eventType.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.event_type.add.successful"));

        } else { // edit case

            eventType = PortfolioEntryEventDao.getPEEventTypeById(eventTypeFormData.id);

            eventTypeFormData.fill(eventType);
            eventType.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.event_type.edit.successful"));
        }

        eventTypeFormData.name.persist(getI18nMessagesPlugin());

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationRegisterController.list());
    }

    /**
     * Delete an event type.
     * 
     * @param eventTypeId
     *            the event type id
     */
    public Result deleteEventType(Long eventTypeId) {

        PortfolioEntryEventType eventType = PortfolioEntryEventDao.getPEEventTypeById(eventTypeId);

        eventType.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.event_type.delete.successful"));

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationRegisterController.list());
    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    /**
     * Get the table provider.
     */
    private ITableProvider getTableProvider() {
        return this.tableProvider;
    }

}
