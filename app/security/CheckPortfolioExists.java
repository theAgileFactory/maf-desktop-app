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

import models.pmo.Portfolio;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import dao.pmo.PortfolioDao;

/**
 * Check if a portfolio exists.
 * 
 * @author Johann Kohler
 */
public class CheckPortfolioExists extends Action.Simple {

    @Override
    public Promise<Result> call(final Http.Context ctx) throws Throwable {

        Long id = DefaultDynamicResourceHandler.getId(ctx);
        if (id != null) {
            Portfolio portfolio = PortfolioDao.getPortfolioById(id);
            if (portfolio != null) {
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