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
package controllers.api.core;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import controllers.api.request.PortfolioEntryListRequest;
import controllers.api.request.post.PortfolioEntryRequestPost;
import controllers.api.request.post.PortfolioEntryRequestPut;
import controllers.core.PortfolioEntryController;
import dao.delivery.IterationDAO;
import dao.delivery.RequirementDAO;
import dao.finance.PortfolioEntryBudgetDAO;
import dao.finance.PortfolioEntryResourcePlanDAO;
import dao.finance.PurchaseOrderDAO;
import dao.finance.WorkOrderDAO;
import dao.governance.LifeCycleProcessDao;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioEntryDao;
import dao.pmo.PortfolioEntryEventDao;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import dao.pmo.PortfolioEntryReportDao;
import dao.pmo.PortfolioEntryRiskDao;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.delivery.Iteration;
import models.delivery.Requirement;
import models.finance.PortfolioEntryBudgetLine;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.finance.PortfolioEntryResourcePlanAllocatedCompetency;
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;
import models.finance.WorkOrder;
import models.governance.LifeCycleProcess;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryEvent;
import models.pmo.PortfolioEntryPlanningPackage;
import models.pmo.PortfolioEntryReport;
import models.pmo.PortfolioEntryRisk;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;
import services.licensesmanagement.ILicensesManagementService;

/**
 * The API controller for the {@link PortfolioEntry}.
 * 
 * @author Oury Diallo
 */
@Api(value = "/api/core/portfolio-entry", description = "Operations on Portfolio Entries")
public class PortfolioEntryApiController extends ApiController {

    @Inject
    private ILicensesManagementService licensesManagementService;

    @Inject
    IPreferenceManagerPlugin preferenceManagerPlugin;

    public static Form<PortfolioEntryListRequest> portfolioEntryListRequestFormTemplate = Form.form(PortfolioEntryListRequest.class);
    public static Form<PortfolioEntryRequestPost> portfolioEntryRequestPostFormTemplate = Form.form(PortfolioEntryRequestPost.class);
    public static Form<PortfolioEntryRequestPut> portfolioEntryRequestPutFormTemplate = Form.form(PortfolioEntryRequestPut.class);
    public static ObjectMapper portfolioEntryMapper = new ObjectMapper();

    /**
     * Get the portfolio entries list with filter.
     * 
     * @param managerId
     *            if not null then return only portfolio entries for the given
     *            manager.
     * @param sponsoringUnitId
     *            if not null then return only portfolio entries with the given
     *            sponsoring unit.
     * @param deliveryUnitId
     *            if not null then return only portfolio entries with the given
     *            delivery unit.
     * @param portfolioId
     *            if not null then return only portfolio entries belonging to
     *            the given portfolio.
     * @param archived
     *            true to return only archived portfolio entries, false only
     *            active, null all.
     * @param portfolioEntryTypeId
     *            if not null then return only portfolio entries with the given
     *            type.
     * @param isPublic
     *            true to return only public portfolio entries, false only
     *            confidential, null all.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Portfolio Entries", notes = "Return the list of Portfolio Entries in the system", response = PortfolioEntry.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntriesList(@ApiParam(value = "managerId", required = false) @QueryParam("managerId") Long managerId,
            @ApiParam(value = "sponsoringUnitId", required = false) @QueryParam("sponsoringUnitId") Long sponsoringUnitId,
            @ApiParam(value = "deliveryUnitId", required = false) @QueryParam("deliveryUnitId") Long deliveryUnitId,
            @ApiParam(value = "portfolioId", required = false) @QueryParam("portfolioId") Long portfolioId,
            @ApiParam(value = "archived", required = false) @QueryParam("archived") Boolean archived,
            @ApiParam(value = "portfolioEntryTypeId", required = false) @QueryParam("portfolioEntryTypeId") Long portfolioEntryTypeId,
            @ApiParam(value = "isPublic", required = false) @QueryParam("isPublic") Boolean isPublic) {

        try {

            // Validation form
            PortfolioEntryListRequest portfolioEntryListRequest = new PortfolioEntryListRequest(managerId, sponsoringUnitId, deliveryUnitId, portfolioId,
                    archived, portfolioEntryTypeId, isPublic);

            // object to jsonNode
            JsonNode node = portfolioEntryMapper.valueToTree(portfolioEntryListRequest);

            // fill a play form
            Form<PortfolioEntryListRequest> portfolioEntryListRequestForm = portfolioEntryListRequestFormTemplate.bind(node);

            if (portfolioEntryListRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = portfolioEntryListRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            return getJsonSuccessResponse(PortfolioEntryDao.getPEAsListByFilter(managerId, sponsoringUnitId, deliveryUnitId, portfolioId, archived,
                    portfolioEntryTypeId, isPublic));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }

    }

    /**
     * Get a portfolio entry by id.
     * 
     * @param id
     *            the portfolio entry id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Portfolio Entry ", notes = "Return the Portfolio Entry with the specified id", response = PortfolioEntry.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryById(@ApiParam(value = "portfolio entry id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryDao.getPEById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Create a portfolio entry.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create an Portfolio Entry", notes = "Create an Portfolio Entry", response = PortfolioEntryRequestPost.class, httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A portfolio entry", required = true, dataType = "PortfolioEntryRequestPost", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createPortfolioEntry() {
        try {

            if (!getLicensesManagementService().canCreatePortfolioEntry()) {
                return getJsonErrorResponse(new ApiError(403, "No available license."));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PortfolioEntryRequestPost> portfolioEntryRequestForm = portfolioEntryRequestPostFormTemplate.bind(json);

            // if errors
            if (portfolioEntryRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = portfolioEntryRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            PortfolioEntryRequestPost portfolioEntryRequest = portfolioEntryRequestForm.get();

            PortfolioEntry portfolioEntry = new PortfolioEntry();

            // fill to match with DB
            portfolioEntry.name = portfolioEntryRequest.name;
            portfolioEntry.governanceId = portfolioEntryRequest.governanceId;
            portfolioEntry.erpRefId = portfolioEntryRequest.erpRefId;
            portfolioEntry.description = portfolioEntryRequest.description;
            portfolioEntry.refId = portfolioEntryRequest.refId;
            portfolioEntry.isPublic = portfolioEntryRequest.isPublic;
            portfolioEntry.archived = portfolioEntryRequest.archived;
            portfolioEntry.creationDate = new Date();
            portfolioEntry.manager = ActorDao.getActorById(portfolioEntryRequest.managerId);
            portfolioEntry.portfolioEntryType = PortfolioEntryDao.getPETypeById(portfolioEntryRequest.portfolioEntryTypeId);
            portfolioEntry.sponsoringUnit = OrgUnitDao.getOrgUnitById(portfolioEntryRequest.sponsoringUnitId);
            portfolioEntry.save();

            LifeCycleProcess lifeCycleProcess = LifeCycleProcessDao.getLCProcessById(portfolioEntryRequest.lifeCycleProcessId);
            PortfolioEntryController.createLifeCycleProcessTree(portfolioEntry, lifeCycleProcess);

            getLicensesManagementService().updateConsumedPortfolioEntries();

            // return json success
            return getJsonSuccessCreatedResponse(portfolioEntry);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update a portfolio entry.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the portfolio entry id
     * 
     * @return the JSON object of the corresponding portfolio entry.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Portfolio Entry, default for empty fields : null", notes = "Update an Portfolio Entry",
            response = PortfolioEntryRequestPut.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A portfolio entry", required = true, dataType = "PortfolioEntryRequestPut", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result updatePortfolioEntry(@ApiParam(value = "An portfolio entry id", required = true) @PathParam("id") Long id) {
        try {

            // check if p-e exist
            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PortfolioEntryRequestPut> portfolioEntryRequestForm = portfolioEntryRequestPutFormTemplate.bind(json);

            // if errors
            if (portfolioEntryRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = portfolioEntryRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            PortfolioEntryRequestPut portfolioEntryRequest = portfolioEntryRequestForm.get();

            PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

            // fill to match with DB
            portfolioEntry.name = portfolioEntryRequest.name;
            portfolioEntry.governanceId = portfolioEntryRequest.governanceId;
            portfolioEntry.erpRefId = portfolioEntryRequest.erpRefId;
            portfolioEntry.description = portfolioEntryRequest.description;
            portfolioEntry.refId = portfolioEntryRequest.refId;
            portfolioEntry.isPublic = portfolioEntryRequest.isPublic;
            portfolioEntry.archived = portfolioEntryRequest.archived;
            portfolioEntry.creationDate = new Date();
            portfolioEntry.manager = ActorDao.getActorById(portfolioEntryRequest.managerId);
            portfolioEntry.portfolioEntryType = PortfolioEntryDao.getPETypeById(portfolioEntryRequest.portfolioEntryTypeId);
            portfolioEntry.sponsoringUnit = OrgUnitDao.getOrgUnitById(portfolioEntryRequest.sponsoringUnitId);

            portfolioEntry.save();

            // json success
            return getJsonSuccessResponse(portfolioEntry);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Get all events of a portfolio entry as an expression list.
     * 
     * @param id
     *            the portfolio entry id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Events of the specified Portfolio Entry",
            notes = "Return the list of the Events of the specified Portfolio Entry in the system", response = PortfolioEntryEvent.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryEventsList(@ApiParam(value = "portfolio entry id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryEventDao.getPEEventAsExprByPE(id).findList());

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get all planning packages of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Planning Packages of the specified Portfolio Entry",
            notes = "Return the list of the Planning Packages of the specified Portfolio Entry in the system", response = PortfolioEntryPlanningPackage.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryPlanningPackagesList(@ApiParam(value = "portfolio entry id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryPlanningPackageDao.getPEPlanningPackageAsListByPE(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get portfolio entry reports of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Reports of the specified Portfolio Entry",
            notes = "Return the list of the Reports of the specified Portfolio Entry in the system", response = PortfolioEntryReport.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryReportsList(@ApiParam(value = "portfolio entry id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryReportDao.getPEReportAsListByPE(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }

    }

    /**
     * Get the portfolio entry risks of a portfolio entry with filters.
     * 
     * @param isActive
     *            true to return only active risks, false only non-active, null
     *            all.
     * @param id
     *            the portfolio entry
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Risks of the specified Portfolio Entry",
            notes = "Return the list of the Risks of the specified Portfolio Entry in the system", response = PortfolioEntryRisk.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryRisksList(@ApiParam(value = "isActive", required = false) @QueryParam("isActive") Boolean isActive,
            @ApiParam(value = "portfolio entry id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The portfolio entry with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryRiskDao.getPERiskAsListByFilter(isActive, id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }

    }

    /**
     * Get list of all requirements of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Requirements of the specified Portfolio Entry",
            notes = "Return the list of the Requirements of the specified Portfolio Entry in the system", response = Requirement.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getRequirementsList(@ApiParam(value = "portfolio entry id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry with the specified id is not found"));
            }
            return getJsonSuccessResponse(RequirementDAO.getRequirementAsListByPE(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }

    }

    /**
     * Get list of all iterations of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Iterations of the specified Portfolio Entry",
            notes = "Return the list of the Iterations of the specified Portfolio Entry in the system", response = Iteration.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getIterationsList(@ApiParam(value = "portfolio entry id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry with the specified id is not found"));
            }
            return getJsonSuccessResponse(IterationDAO.getIterationsListByPE(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }

    }

    /**
     * Get the list portfolio entry budget lines of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Budget Lines of the specified Portfolio Entry",
            notes = "Return the Budget Lines of the specified Portfolio Entry in the system", response = PortfolioEntryBudgetLine.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getBudgetLinesOfPortfolioEntriesList(@ApiParam(value = "portfolio entry id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The portfolio entry with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryBudgetDAO.getPEBudgetLineAsListByPE(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }

    }

    /**
     * Get the allocated org units of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Allocated Org Units of the specified Portfolio Entry",
            notes = "Return the list of the Allocated Org Units of the specified Portfolio Entry in the system",
            response = PortfolioEntryResourcePlanAllocatedOrgUnit.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryResourcePlanAllocatedOrgUnitsList(@ApiParam(value = "portfolio entry id", required = true) @PathParam("id") Long id) {
        try {
            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The portfolio entry with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedOrgUnitAsListByPE(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get the allocated actors of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Allocated actors of the specified Portfolio Entry",
            notes = "Return the list of the Allocated actors of the specified Portfolio Entry in the system",
            response = PortfolioEntryResourcePlanAllocatedActor.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryResourcePlanAllocatedActorsList(@ApiParam(value = "portfolio entry id", required = true) @PathParam("id") Long id) {
        try {
            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The portfolio entry with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsListByPE(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get the allocated competencies of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Allocated Competencies of the specified Portfolio Entry",
            notes = "Return the list of the Allocated Competencies of the specified Portfolio Entry in the system",
            response = PortfolioEntryResourcePlanAllocatedCompetency.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryResourcePlanAllocatedCompetenciesList(@ApiParam(value = "portfolio entry id", required = true) @PathParam("id") Long id) {
        try {
            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The portfolio entry with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedCompetencyAsListByPE(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get the work orders of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Work Orders of the specified Portfolio Entry",
            notes = "Return the list of the Work Orders of the specified Portfolio Entry in the system", response = WorkOrder.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getWorkOrdersList(@ApiParam(value = "portfolioEntryId", required = true) @PathParam("id") Long id) {
        try {
            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The portfolio entry with the specified id is not found"));
            }
            return getJsonSuccessResponse(WorkOrderDAO.getWorkOrderAsList(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get the isEngaged attribute of a work order.
     * 
     * @param id
     *            the portfolio entry id
     * @param workOrderId
     *            the work order id
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "\"isEngaged\" attribute of a work order",
            notes = "Return the \"isEngaged\" attribute of a work order according to the BizDock instance context (use or not purchase order)",
            response = Boolean.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getWorkOrderIsEngaged(@ApiParam(value = "The portfolio entry ID", required = true) @PathParam("id") Long id,
            @ApiParam(value = "The work order ID", required = true) @PathParam("workOrderId") Long workOrderId) {
        try {

            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The portfolio entry with the specified id is not found"));
            }

            if (WorkOrderDAO.getWorkOrderById(workOrderId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The work order with the specified id is not found"));
            }

            WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

            if (!workOrder.portfolioEntry.id.equals(id)) {
                return getJsonErrorResponse(new ApiError(400, "The work order does not belong to the given portfolio entry"));
            }

            return getJsonSuccessResponse(
                    workOrder.getComputedIsEngaged(PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(this.getPreferenceManagerPlugin())));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get the amount attribute of a work order.
     * 
     * @param id
     *            the portfolio entry id
     * @param workOrderId
     *            the work order id
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "\"amount\" attribute of a work order",
            notes = "Return the \"amount\" attribute of a work order according to the BizDock instance context (use or not purchase order)",
            response = Double.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getWorkOrderAmount(@ApiParam(value = "The portfolio entry ID", required = true) @PathParam("id") Long id,
            @ApiParam(value = "The work order ID", required = true) @PathParam("workOrderId") Long workOrderId) {
        try {

            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The portfolio entry with the specified id is not found"));
            }

            if (WorkOrderDAO.getWorkOrderById(workOrderId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The work order with the specified id is not found"));
            }

            WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

            if (!workOrder.portfolioEntry.id.equals(id)) {
                return getJsonErrorResponse(new ApiError(400, "The work order does not belong to the given portfolio entry"));
            }

            return getJsonSuccessResponse(
                    workOrder.getComputedAmount(PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(this.getPreferenceManagerPlugin())));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get the amountReceived attribute of a work order.
     * 
     * @param id
     *            the portfolio entry id
     * @param workOrderId
     *            the work order id
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "\"amountReceived\" attribute of a work order",
            notes = "Return the \"amountReceived\" attribute of a work order according to the BizDock instance context (use or not purchase order)",
            response = Double.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getWorkOrderAmountReceived(@ApiParam(value = "The portfolio entry ID", required = true) @PathParam("id") Long id,
            @ApiParam(value = "The work order ID", required = true) @PathParam("workOrderId") Long workOrderId) {
        try {

            if (PortfolioEntryDao.getPEById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The portfolio entry with the specified id is not found"));
            }

            if (WorkOrderDAO.getWorkOrderById(workOrderId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The work order with the specified id is not found"));
            }

            WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

            if (!workOrder.portfolioEntry.id.equals(id)) {
                return getJsonErrorResponse(new ApiError(400, "The work order does not belong to the given portfolio entry"));
            }

            return getJsonSuccessResponse(
                    workOrder.getComputedAmountReceived(PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(this.getPreferenceManagerPlugin())));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get the licenses management service.
     */
    private ILicensesManagementService getLicensesManagementService() {
        return licensesManagementService;
    }

    /**
     * Get the preference manager service.
     */
    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return this.preferenceManagerPlugin;
    }

}
