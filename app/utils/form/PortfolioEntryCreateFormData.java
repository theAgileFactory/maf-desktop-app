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
package utils.form;

import dao.pmo.ActorDao;
import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;
import framework.utils.FileField;
import framework.utils.FileFieldOptionalValidator;
import models.framework_models.parent.IModelConstants;
import models.pmo.PortfolioEntry;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A portfolio entry create form data is used to manage the fields when creating
 * an portfolio entry.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryCreateFormData extends AbstractFormData<PortfolioEntry> {

    public PortfolioEntryCreateFormData() {}

    @Override
    public void fillEntity(PortfolioEntry entity) {
        // Get the last governance id
        Integer lastGovernanceId = PortfolioEntryDao.getPEAsLastGovernanceId();

        // Create the portfolio entry
        entity.name = this.name;
        entity.description = this.description;
        entity.manager = ActorDao.getActorById(this.manager);
        entity.portfolios = portfolios == null ? null : Arrays.stream(portfolios).map(PortfolioDao::getPortfolioById).collect(Collectors.toList());
        entity.isPublic = !this.isConfidential;
        entity.portfolioEntryType = PortfolioEntryDao.getPETypeById(this.portfolioEntryType);
        entity.governanceId = lastGovernanceId != null ? String.valueOf(lastGovernanceId + 1) : "1";
        entity.erpRefId = "";
    }

    /**
     * Construct with a default manager.
     * 
     * @param isRelease
     *            true for a release, false for an initiative
     * @param managerId
     *            the manager id
     */
    public PortfolioEntryCreateFormData(boolean isRelease, Long managerId) {
        this.isRelease = isRelease;
        this.manager = managerId;
    }

    public boolean isRelease;

    public boolean isConfidential;

    @Required
    @MinLength(value = 3)
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @Required
    @MaxLength(value = IModelConstants.XLARGE_STRING)
    public String description;

    @Required
    public Long manager;

    @ConditionalRequired(value = "portfolioEntry.portfolios")
    public Long[] portfolios;

    @Required
    public Long portfolioEntryType;

    @Required
    public Long requestedLifeCycleProcess;

    @ValidateWith(value = FileFieldOptionalValidator.class, message = "form.input.file_field.error")
    public FileField scopeDescription;

    @Override
    public String toString() {
        return "PortfolioEntryCreateFormData [isConfidential=" + isConfidential + ", name=" + name + ", description=" + description + ", portfolio="
                + Arrays.toString(portfolios) + ", portfolioEntryType=" + portfolioEntryType + ", requestedLifeCycleProcess=" + requestedLifeCycleProcess
                + ", scopeDescription=" + scopeDescription + "]";
    }
}
