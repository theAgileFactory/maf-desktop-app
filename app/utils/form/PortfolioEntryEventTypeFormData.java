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

import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.CustomConstraints.MultiLanguagesStringMaxLength;
import framework.utils.CustomConstraints.MultiLanguagesStringRequired;
import framework.utils.MultiLanguagesString;
import models.framework_models.parent.IModelConstants;
import models.pmo.PortfolioEntryEventType;
import play.data.validation.Constraints.Required;

/**
 * A portfolio entry event type form data is used to manage the fields when
 * adding/editing a portfolio entry event type.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryEventTypeFormData {

    public Long id;

    public boolean selectable;

    public boolean readOnly;

    @MultiLanguagesStringRequired
    @MultiLanguagesStringMaxLength(value = IModelConstants.MEDIUM_STRING)
    public MultiLanguagesString name;

    @Required
    public String bootstrapGlyphicon;

    /**
     * Default constructor.
     */
    public PortfolioEntryEventTypeFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolioEntryEventType
     *            the portfolio entry event type in the DB
     * @param i18nMessagesPlugin
     *            the i18n manager
     */
    public PortfolioEntryEventTypeFormData(PortfolioEntryEventType portfolioEntryEventType, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = portfolioEntryEventType.id;
        this.selectable = portfolioEntryEventType.selectable;
        this.readOnly = portfolioEntryEventType.readOnly;
        this.name = MultiLanguagesString.getByKey(portfolioEntryEventType.name, i18nMessagesPlugin);
        this.bootstrapGlyphicon = portfolioEntryEventType.bootstrapGlyphicon;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolioEntryEventType
     *            the portfolio entry event type in the DB
     */
    public void fill(PortfolioEntryEventType portfolioEntryEventType) {

        portfolioEntryEventType.selectable = this.selectable;
        portfolioEntryEventType.name = this.name.getKeyIfValue();
        portfolioEntryEventType.bootstrapGlyphicon = this.bootstrapGlyphicon;
        portfolioEntryEventType.readOnly = this.readOnly;

    }
}
