package services.configuration;

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
import javax.inject.Inject;
import javax.inject.Singleton;

import controllers.Assets.Asset;
import dao.finance.CurrencyDAO;
import framework.services.configuration.IImplementationDefinedObjectService;
import framework.services.database.IDatabaseDependencyService;
import framework.utils.routes;
import models.delivery.Deliverable;
import models.pmo.Actor;
import models.pmo.OrgUnit;
import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F.Promise;
import play.mvc.Call;

/**
 * Define the objects which can only be defined in the implementation.
 * 
 * @author Pierre-Yves Cloux
 */
@Singleton
public class ImplementationDefinedObjectImpl implements IImplementationDefinedObjectService {

    private static Logger.ALogger log = Logger.of(ImplementationDefinedObjectImpl.class);

    /**
     * Default constructor.
     * 
     * @param lifecycle
     *            the play application lifecycle listener
     * @param configuration
     *            the play application configuratio
     * @param databaseDependencyService
     *            the service which ensures that the database is available
     */
    @Inject
    public ImplementationDefinedObjectImpl(ApplicationLifecycle lifecycle, Configuration configuration,
            IDatabaseDependencyService databaseDependencyService) {
        log.info("SERVICE>>> ImplementationDefinedObjectImpl starting...");
        lifecycle.addStopHook(() -> {
            log.info("SERVICE>>> ImplementationDefinedObjectImpl stopping...");
            log.info("SERVICE>>> ImplementationDefinedObjectImpl stopped");
            return Promise.pure(null);
        });
        log.info("SERVICE>>> ImplementationDefinedObjectImpl started");
    }

    /**
     * Return the default currency for the system.
     */
    @Override
    public String getDefaultCurrencyCode() {
        return CurrencyDAO.getCurrencyDefaultAsCode();
    }

    @Override
    public Call getRouteForAjaxWaitImage() {
        return controllers.routes.Assets.versioned(new Asset("images/ajax-loader.gif"));
    }

    @Override
    public Call getRouteForDynamicSingleCustomAttributeApi() {
        return controllers.routes.Application.dynamicSingleCustomAttributeApi();
    }

    @Override
    public Call getRouteForDownloadAttachedFile(Long attachmentId) {
        return controllers.routes.Application.downloadFileAttachment(attachmentId);
    }

    @Override
    public Call getRouteForDeleteAttachedFile(Long attachmentId) {
        return controllers.routes.Application.deleteFileAttachment(attachmentId);
    }

    @Override
    public boolean isFilterConfigurationActive() {
        return true;
    }

    @Override
    public Call getRouteForFilterConfigurationCreate() {
        return framework.utils.routes.FilterConfigController.filterConfigurationCreate();
    }

    @Override
    public Call getRouteForFilterConfigurationSave() {
        return framework.utils.routes.FilterConfigController.filterConfigurationSave();
    }

    @Override
    public Call getRouteForFilterConfigurationChange() {
        return framework.utils.routes.FilterConfigController.filterConfigurationChange();
    }

    @Override
    public Call getRouteForFilterConfigurationEdit() {
        return framework.utils.routes.FilterConfigController.filterConfigurationEdit();
    }

    @Override
    public Call getRouteForFilterConfigurationDelete() {
        return framework.utils.routes.FilterConfigController.filterConfigurationDelete();
    }

    @Override
    public Call getRouteForFilterConfigurationShare() {
        return framework.utils.routes.FilterConfigController.filterConfigurationShare();
    }

    @Override
    public Call getRouteForFilterConfigurationSearchPrincipal() {
        return framework.utils.routes.FilterConfigController.filterConfigurationSearchPrincipal();
    }

    @Override
    public Call getRouteForFilterConfigurationClear() {
        return routes.FilterConfigController.filterConfigurationClear();
    }

    @Override
    public String renderObject(Object object) {
        if (object instanceof OrgUnit) {
            return views.html.modelsparts.display_org_unit.render((OrgUnit) object).body();
        }
        if (object instanceof Portfolio) {
            return views.html.modelsparts.display_portfolio.render((Portfolio) object).body();
        }
        if (object instanceof Deliverable) {
            return views.html.modelsparts.display_deliverable.render((Deliverable) object).body();
        }
        if (object instanceof Actor) {
            return views.html.modelsparts.display_actor.render((Actor) object).body();
        }
        if (object instanceof PortfolioEntry) {
            return views.html.modelsparts.display_portfolio_entry.render((PortfolioEntry) object, true).body();
        }
        return object.toString();
    }
}
