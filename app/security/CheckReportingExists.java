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
package security;

import models.reporting.Reporting;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import dao.reporting.ReportingDao;
import framework.utils.Utilities;

/**
 * Check if a report exists.
 * 
 * @author Johann Kohler
 * 
 */
public class CheckReportingExists extends Action.Simple {

    @Override
    public Promise<Result> call(final Http.Context ctx) throws Throwable {

        Long id = Utilities.getId(ctx);
        if (id != null) {
            Reporting reporting = ReportingDao.getReportingById(id);
            if (reporting != null) {
                return delegate.call(ctx);
            }
        }

        return Promise.promise(new Function0<Result>() {

            public Result apply() throws Throwable {
                return play.mvc.Results.notFound(views.html.error.not_found.render(ctx.request().uri()));
            }

        });

    }
}