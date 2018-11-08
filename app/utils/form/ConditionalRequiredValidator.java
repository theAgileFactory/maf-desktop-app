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

import play.Play;
import play.data.validation.Constraints;
import play.libs.F;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;

/**
 * @author maf
 */
public class ConditionalRequiredValidator extends Constraints.Validator<Object> implements ConstraintValidator<ConditionalRequired, Object> {

    private String field;
    private boolean mandatoryByDefault;

    @Override
    public void initialize(ConditionalRequired constraint) {
        field = constraint.value();
        mandatoryByDefault = constraint.mandatoryByDefault();
    }

    @Override
    public boolean isValid(Object object) {
        Boolean fieldRequired = Play.application().configuration().getBoolean(String.format("form.%s.required", this.field));

        if ((!mandatoryByDefault && fieldRequired != null && fieldRequired) || (mandatoryByDefault && (fieldRequired == null || fieldRequired))) {
            if(object == null) {
                return false;
            }

            if(object instanceof String) {
                return !((String)object).isEmpty();
            }

            if(object instanceof Collection) {
                return !((Collection)object).isEmpty();
            }
        }
        return true;
    }

    @Override
    public F.Tuple<String, Object[]> getErrorMessageKey() {
        return new F.Tuple<>(Constraints.RequiredValidator.message, new Object[]{});
    }
}