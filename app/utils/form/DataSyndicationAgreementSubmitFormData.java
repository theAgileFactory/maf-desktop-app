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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import framework.utils.Utilities;
import play.Logger;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.i18n.Messages;

/**
 * Form to submit a data agreement from a master instance.
 * 
 * @author Johann Kohler
 */
public class DataSyndicationAgreementSubmitFormData {

    public String slaveDomain;

    public String refId;

    @Required
    public String name;

    @Required
    public String startDate;

    @Required
    public String endDate;

    public List<Long> itemIds = new ArrayList<Long>();

    /**
     * Default constructor.
     */
    public DataSyndicationAgreementSubmitFormData() {
    }

    /**
     * Initialize the form.
     * 
     * @param slaveDomain
     *            the slave domain
     */
    public DataSyndicationAgreementSubmitFormData(String slaveDomain) {

        this.slaveDomain = slaveDomain;
        this.startDate = Utilities.getDateFormat(null).format(new Date());

    }

    /**
     * Validate the dates.
     */
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        try {

            if (Utilities.getDateFormat(null).parse(this.startDate).after(Utilities.getDateFormat(null).parse(this.endDate))) {
                // the end date should be after the start date
                errors.add(new ValidationError("endDate", Messages.get("object.data_syndication_agreement.end_date.invalid")));
            }

        } catch (Exception e) {
            Logger.warn("impossible to validate the dates");
        }

        return errors.isEmpty() ? null : errors;
    }

    /**
     * Get the start date as a date.
     */
    public Date getStartDateAsDate() {
        try {
            return Utilities.getDateFormat(null).parse(this.startDate);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Get the end date as a date.
     */
    public Date getEndDateAsDate() {
        try {
            return Utilities.getDateFormat(null).parse(this.endDate);
        } catch (ParseException e) {
            return null;
        }
    }

}
