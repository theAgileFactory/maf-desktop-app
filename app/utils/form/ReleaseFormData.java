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
import java.util.List;

import models.delivery.Release;
import models.framework_models.parent.IModelConstants;
import models.pmo.Actor;
import play.Logger;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import dao.pmo.ActorDao;
import framework.utils.Utilities;

/**
 * An release form data is used to manage the fields when managing a release.
 * 
 * @author Johann Kohler
 */
public class ReleaseFormData {

    public Long id;

    public boolean isActive;

    @Required
    @MaxLength(value = IModelConstants.LARGE_STRING)
    public String name;

    public String description;

    public Integer capacity;

    public String cutOffDate;

    public String endTestsDate;

    @Required
    public String deploymentDate;

    @Required
    public Long manager;

    /**
     * Default constructor.
     */
    public ReleaseFormData() {
    }

    /**
     * Construct a release form with default value.
     * 
     * @param manager
     *            the default manager
     */
    public ReleaseFormData(Actor manager) {
        if (manager != null) {
            this.manager = manager.id;
        }
    }

    /**
     * Validate the dates.
     */
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        // check the dates
        try {

            // if the end tests date is given then it should be before (<=) than
            // the deployment date
            if (!this.endTestsDate.equals("")
                    && Utilities.getDateFormat(null).parse(this.endTestsDate).after(Utilities.getDateFormat(null).parse(this.deploymentDate))) {
                errors.add(new ValidationError("endTestsDate", Messages.get("object.release.end_tests_date.invalid")));
            }

            // the cut off date is given AND
            // if the end tests date and is given then the cut off date should
            // be before (<=) than the end tests date OR
            // the cut off date should be before (<=) than the deployment date
            if (!this.cutOffDate.equals("")
                    && ((!this.endTestsDate.equals("") && Utilities.getDateFormat(null).parse(this.cutOffDate)
                            .after(Utilities.getDateFormat(null).parse(this.endTestsDate))) || Utilities.getDateFormat(null).parse(this.cutOffDate)
                            .after(Utilities.getDateFormat(null).parse(this.deploymentDate)))) {
                errors.add(new ValidationError("cutOffDate", Messages.get("object.release.cut_off_date.invalid")));
            }

        } catch (Exception e) {
            Logger.warn("impossible to parse the release dates when testing the formats");
        }

        return errors.isEmpty() ? null : errors;
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param release
     *            the release in the DB
     */
    public ReleaseFormData(Release release) {

        this.id = release.id;
        this.isActive = release.isActive;
        this.name = release.name;
        this.description = release.description;
        this.capacity = release.capacity;
        this.cutOffDate = release.cutOffDate != null ? Utilities.getDateFormat(null).format(release.cutOffDate) : null;
        this.endTestsDate = release.endTestsDate != null ? Utilities.getDateFormat(null).format(release.endTestsDate) : null;
        this.deploymentDate = release.deploymentDate != null ? Utilities.getDateFormat(null).format(release.deploymentDate) : null;
        this.manager = release.manager != null ? release.manager.id : null;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param release
     *            the release in the DB
     */
    public void fill(Release release) {

        release.isActive = this.isActive;
        release.name = this.name;
        release.description = this.description;
        release.capacity = this.capacity;

        try {
            release.cutOffDate = Utilities.getDateFormat(null).parse(this.cutOffDate);
        } catch (ParseException e) {
            release.cutOffDate = null;
        }

        try {
            release.endTestsDate = Utilities.getDateFormat(null).parse(this.endTestsDate);
        } catch (ParseException e) {
            release.endTestsDate = null;
        }

        try {
            release.deploymentDate = Utilities.getDateFormat(null).parse(this.deploymentDate);
        } catch (ParseException e) {
            release.deploymentDate = null;
        }

        release.manager = this.manager != null ? ActorDao.getActorById(this.manager) : null;

    }

}
