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
import controllers.api.core.RootApiController;
import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import models.pmo.PortfolioEntryDependencyType;
import models.pmo.PortfolioEntryType;
import models.pmo.PortfolioType;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import utils.form.PortfolioEntryDependencyTypeFormData;
import utils.form.PortfolioEntryTypeFormData;
import utils.form.PortfolioTypeFormData;
import utils.table.PortfolioEntryDependencyTypeListView;
import utils.table.PortfolioEntryTypeListView;
import utils.table.PortfolioTypeListView;

/**
 * Manage the portfolios and portfolio entries reference data.
 * 
 * @author Johann Kohler
 * 
 */
@Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
public class ConfigurationPortfolioController extends Controller {

    private static Form<PortfolioEntryTypeFormData> portfolioEntryTypeFormTemplate = Form.form(PortfolioEntryTypeFormData.class);
    private static Form<PortfolioEntryDependencyTypeFormData> portfolioEntryDependencyTypeFormTemplate = Form
            .form(PortfolioEntryDependencyTypeFormData.class);
    private static Form<PortfolioTypeFormData> portfolioTypeFormTemplate = Form.form(PortfolioTypeFormData.class);

    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;

    /**
     * Display the lists of data.
     */
    public Result list() {

        // portfolio entry types of initiatives
        List<PortfolioEntryTypeListView> initiativeTypesListView = new ArrayList<PortfolioEntryTypeListView>();
        for (PortfolioEntryType portfolioEntryType : PortfolioEntryDao.getPETypeInitiativeAsList()) {
            initiativeTypesListView.add(new PortfolioEntryTypeListView(portfolioEntryType));
        }

        Table<PortfolioEntryTypeListView> initiativeTypesFilledTable = PortfolioEntryTypeListView.templateTable.fill(initiativeTypesListView);

        // portfolio entry types of releases
        List<PortfolioEntryTypeListView> releaseTypesListView = new ArrayList<PortfolioEntryTypeListView>();
        for (PortfolioEntryType portfolioEntryType : PortfolioEntryDao.getPETypeReleaseAsList()) {
            releaseTypesListView.add(new PortfolioEntryTypeListView(portfolioEntryType));
        }

        Table<PortfolioEntryTypeListView> releaseTypesFilledTable = PortfolioEntryTypeListView.templateTable.fill(releaseTypesListView);

        // portfolio entry dependency types
        List<PortfolioEntryDependencyType> portfolioEntryDependencyTypes = PortfolioEntryDao.getPEDependencyTypeAsList();

        List<PortfolioEntryDependencyTypeListView> portfolioEntryDependencyTypesListView = new ArrayList<PortfolioEntryDependencyTypeListView>();
        for (PortfolioEntryDependencyType portfolioEntryDependencyType : portfolioEntryDependencyTypes) {
            portfolioEntryDependencyTypesListView.add(new PortfolioEntryDependencyTypeListView(portfolioEntryDependencyType));
        }

        Table<PortfolioEntryDependencyTypeListView> portfolioEntryDependencyTypesFilledTable = PortfolioEntryDependencyTypeListView.templateTable
                .fill(portfolioEntryDependencyTypesListView);

        // portfolio types
        List<PortfolioType> portfolioTypes = PortfolioDao.getPortfolioTypeAsList();

        List<PortfolioTypeListView> portfolioTypesListView = new ArrayList<PortfolioTypeListView>();
        for (PortfolioType portfolioType : portfolioTypes) {
            portfolioTypesListView.add(new PortfolioTypeListView(portfolioType));
        }

        Table<PortfolioTypeListView> portfolioTypesFilledTable = PortfolioTypeListView.templateTable.fill(portfolioTypesListView);

        return ok(views.html.admin.config.datareference.portfolio.list.render(initiativeTypesFilledTable, releaseTypesFilledTable,
                portfolioEntryDependencyTypesFilledTable, portfolioTypesFilledTable));
    }

    /**
     * Edit or create a portfolio entry type.
     * 
     * @param isRelease
     *            true if the portfolio entry type is a release (used only in
     *            create case)
     * @param portfolioEntryTypeId
     *            the portfolio entry type id (set 0 for create case)
     */
    public Result managePortfolioEntryType(Boolean isRelease, Long portfolioEntryTypeId) {

        // initiate the form with the template
        Form<PortfolioEntryTypeFormData> portfolioEntryTypeForm = null;

        // edit case: inject values
        if (!portfolioEntryTypeId.equals(Long.valueOf(0))) {

            PortfolioEntryType portfolioEntryType = PortfolioEntryDao.getPETypeById(portfolioEntryTypeId);

            portfolioEntryTypeForm = portfolioEntryTypeFormTemplate.fill(new PortfolioEntryTypeFormData(portfolioEntryType, getI18nMessagesPlugin()));

        } else {
            portfolioEntryTypeForm = portfolioEntryTypeFormTemplate.fill(new PortfolioEntryTypeFormData(isRelease));
        }

        return ok(views.html.admin.config.datareference.portfolio.portfolio_entry_type_manage.render(portfolioEntryTypeForm));
    }

    /**
     * Process the edit/create form of a portfolio entry type.
     */
    public Result savePortfolioEntryType() {

        // bind the form
        Form<PortfolioEntryTypeFormData> boundForm = portfolioEntryTypeFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.portfolio.portfolio_entry_type_manage.render(boundForm));
        }

        PortfolioEntryTypeFormData portfolioEntryTypeFormData = boundForm.get();

        PortfolioEntryType portfolioEntryType = null;

        if (portfolioEntryTypeFormData.id == null) { // create case

            portfolioEntryType = new PortfolioEntryType();

            portfolioEntryTypeFormData.fill(portfolioEntryType);
            portfolioEntryType.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.portfolioentrytype.add.successful"));

        } else { // edit case

            portfolioEntryType = PortfolioEntryDao.getPETypeById(portfolioEntryTypeFormData.id);

            portfolioEntryTypeFormData.fill(portfolioEntryType);
            portfolioEntryType.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.portfolioentrytype.edit.successful"));
        }

        portfolioEntryTypeFormData.description.persist(getI18nMessagesPlugin());
        portfolioEntryTypeFormData.name.persist(getI18nMessagesPlugin());

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationPortfolioController.list());
    }

    /**
     * Delete a portfolio entry type.
     * 
     * @param portfolioEntryTypeId
     *            the portfolio entry type id
     */
    public Result deletePortfolioEntryType(Long portfolioEntryTypeId) {

        PortfolioEntryType portfolioEntryType = PortfolioEntryDao.getPETypeById(portfolioEntryTypeId);

        portfolioEntryType.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.portfolioentrytype.delete.successful"));

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationPortfolioController.list());
    }

    /**
     * Edit or create a portfolio entry dependency type.
     * 
     * @param portfolioEntryDependencyTypeId
     *            the portfolio entry dependency type id (set 0 for create case)
     */
    public Result managePortfolioEntryDependencyType(Long portfolioEntryDependencyTypeId) {

        // initiate the form with the template
        Form<PortfolioEntryDependencyTypeFormData> portfolioEntryDependencyTypeForm = portfolioEntryDependencyTypeFormTemplate;

        // edit case: inject values
        if (!portfolioEntryDependencyTypeId.equals(Long.valueOf(0))) {

            PortfolioEntryDependencyType portfolioEntryDependencyType = PortfolioEntryDao.getPEDependencyTypeById(portfolioEntryDependencyTypeId);

            portfolioEntryDependencyTypeForm = portfolioEntryDependencyTypeFormTemplate
                    .fill(new PortfolioEntryDependencyTypeFormData(portfolioEntryDependencyType, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.portfolio.portfolio_entry_dependency_type_manage.render(portfolioEntryDependencyTypeForm));
    }

    /**
     * Process the edit/create form of a portfolio entry dependency type.
     */
    public Result savePortfolioEntryDependencyType() {

        // bind the form
        Form<PortfolioEntryDependencyTypeFormData> boundForm = portfolioEntryDependencyTypeFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.portfolio.portfolio_entry_dependency_type_manage.render(boundForm));
        }

        PortfolioEntryDependencyTypeFormData portfolioEntryDependencyTypeFormData = boundForm.get();

        PortfolioEntryDependencyType portfolioEntryDependencyType = null;

        if (portfolioEntryDependencyTypeFormData.id == null) { // create case

            portfolioEntryDependencyType = new PortfolioEntryDependencyType();

            portfolioEntryDependencyTypeFormData.fill(portfolioEntryDependencyType);
            portfolioEntryDependencyType.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.portfolioentrydependencytype.add.successful"));

        } else { // edit case

            portfolioEntryDependencyType = PortfolioEntryDao.getPEDependencyTypeById(portfolioEntryDependencyTypeFormData.id);

            portfolioEntryDependencyTypeFormData.fill(portfolioEntryDependencyType);
            portfolioEntryDependencyType.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.portfolioentrydependencytype.edit.successful"));
        }

        portfolioEntryDependencyTypeFormData.description.persist(getI18nMessagesPlugin());
        portfolioEntryDependencyTypeFormData.name.persist(getI18nMessagesPlugin());
        portfolioEntryDependencyTypeFormData.contrary.persist(getI18nMessagesPlugin());

        return redirect(controllers.admin.routes.ConfigurationPortfolioController.list());
    }

    /**
     * Delete a portfolio entry dependency type.
     * 
     * @param portfolioEntryDependencyTypeId
     *            the portfolio entry dependency type id
     */
    public Result deletePortfolioEntryDependencyType(Long portfolioEntryDependencyTypeId) {

        PortfolioEntryDependencyType portfolioEntryDependencyType = PortfolioEntryDao.getPEDependencyTypeById(portfolioEntryDependencyTypeId);

        portfolioEntryDependencyType.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.portfolioentrydependencytype.delete.successful"));

        return redirect(controllers.admin.routes.ConfigurationPortfolioController.list());
    }

    /**
     * Edit or create a portfolio type.
     * 
     * @param portfolioTypeId
     *            the portfolio type id (set 0 for create case)
     */
    public Result managePortfolioType(Long portfolioTypeId) {

        // initiate the form with the template
        Form<PortfolioTypeFormData> portfolioTypeForm = portfolioTypeFormTemplate;

        // edit case: inject values
        if (!portfolioTypeId.equals(Long.valueOf(0))) {

            PortfolioType portfolioType = PortfolioDao.getPortfolioTypeById(portfolioTypeId);

            portfolioTypeForm = portfolioTypeFormTemplate.fill(new PortfolioTypeFormData(portfolioType, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.portfolio.portfolio_type_manage.render(portfolioTypeForm));

    }

    /**
     * Process the edit/create form of a portfolio type.
     */
    public Result savePortfolioType() {

        // bind the form
        Form<PortfolioTypeFormData> boundForm = portfolioTypeFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.portfolio.portfolio_type_manage.render(boundForm));
        }

        PortfolioTypeFormData portfolioTypeFormData = boundForm.get();

        PortfolioType portfolioType = null;

        if (portfolioTypeFormData.id == null) { // create case

            portfolioType = new PortfolioType();

            portfolioTypeFormData.fill(portfolioType);
            portfolioType.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.portfoliotype.add.successful"));

        } else { // edit case

            portfolioType = PortfolioDao.getPortfolioTypeById(portfolioTypeFormData.id);

            portfolioTypeFormData.fill(portfolioType);
            portfolioType.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.portfoliotype.edit.successful"));
        }

        portfolioTypeFormData.description.persist(getI18nMessagesPlugin());
        portfolioTypeFormData.name.persist(getI18nMessagesPlugin());

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationPortfolioController.list());
    }

    /**
     * Delete a portfolio type.
     * 
     * @param portfolioTypeId
     *            the portfolio type id
     */
    public Result deletePortfolioType(Long portfolioTypeId) {

        PortfolioType portfolioType = PortfolioDao.getPortfolioTypeById(portfolioTypeId);

        portfolioType.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.portfoliotype.delete.successful"));

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationPortfolioController.list());
    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

}
