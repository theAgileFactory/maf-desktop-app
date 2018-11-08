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
import framework.services.session.IUserSessionManagerPlugin;
import models.common.BizDockModel;
import models.pmo.Actor;
import play.Play;
import play.mvc.Controller;

import java.util.Date;

/**
 * @author Guillaume Petit
 */
public abstract class AbstractFormData<T extends BizDockModel> {

    public AbstractFormData() {
    }

    public abstract void fillEntity(T entity);

    public void fill(T entity) {
        this.fillEntity(entity);
    }

}