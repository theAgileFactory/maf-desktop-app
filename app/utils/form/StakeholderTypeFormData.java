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

import java.util.ArrayList;
import java.util.List;

import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.CustomConstraints.MultiLanguagesStringMaxLength;
import framework.utils.CustomConstraints.MultiLanguagesStringRequired;
import framework.utils.MultiLanguagesString;
import models.framework_models.parent.IModelConstants;
import models.pmo.PortfolioEntryType;
import models.pmo.PortfolioType;
import models.pmo.StakeholderType;

/**
 * A stakeholder type form data is used to manage the fields when adding/editing
 * a stakeholder type.
 * 
 * @author Johann Kohler
 */
public class StakeholderTypeFormData {

    public Long id;

    public boolean selectable;

    @MultiLanguagesStringRequired
    @MultiLanguagesStringMaxLength(value = IModelConstants.MEDIUM_STRING)
    public MultiLanguagesString name;

    @MultiLanguagesStringMaxLength(value = IModelConstants.VLARGE_STRING)
    public MultiLanguagesString description;

    public List<Long> portfolioTypes = new ArrayList<Long>();

    public List<Long> portfolioEntryTypes = new ArrayList<Long>();

    /**
     * Default constructor.
     */
    public StakeholderTypeFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param stakeholderType
     *            the stakeholder type in the DB
     * @param i18nMessagesPlugin
     *            the i18n manager
     */
    public StakeholderTypeFormData(StakeholderType stakeholderType, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = stakeholderType.id;
        this.selectable = stakeholderType.selectable;
        this.name = MultiLanguagesString.getByKey(stakeholderType.name, i18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(stakeholderType.description, i18nMessagesPlugin);
        if (stakeholderType.portfolioTypes != null) {
            for (PortfolioType portfolioType : stakeholderType.portfolioTypes) {
                this.portfolioTypes.add(portfolioType.id);
            }
        }
        if (stakeholderType.portfolioEntryTypes != null) {
            for (PortfolioEntryType portfolioEntryType : stakeholderType.portfolioEntryTypes) {
                this.portfolioEntryTypes.add(portfolioEntryType.id);
            }
        }

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param stakeholderType
     *            the stakeholder type in the DB
     */
    public void fill(StakeholderType stakeholderType) {

        stakeholderType.selectable = this.selectable;
        stakeholderType.name = this.name.getKeyIfValue();
        stakeholderType.description = this.description.getKeyIfValue();
        stakeholderType.portfolioTypes = new ArrayList<PortfolioType>();
        for (Long portfolioTypeId : this.portfolioTypes) {
            stakeholderType.portfolioTypes.add(PortfolioDao.getPortfolioTypeById(portfolioTypeId));
        }
        stakeholderType.portfolioEntryTypes = new ArrayList<PortfolioEntryType>();
        for (Long portfolioEntryTypeId : this.portfolioEntryTypes) {
            stakeholderType.portfolioEntryTypes.add(PortfolioEntryDao.getPETypeById(portfolioEntryTypeId));
        }

    }
}
