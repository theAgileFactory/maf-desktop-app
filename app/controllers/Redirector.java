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
package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.SubjectPresent;

/**
 * A controller that holds a common set of redirections to be used in the
 * application code.
 * 
 * @author Pierre-Yves Cloux
 */
public class Redirector extends Controller {

    /**
     * Project management tool (redmine) redirector.
     */
    @SubjectPresent
    public static Result pm() {
        return redirect("/redmine");
    }

    /**
     * CI redirector.
     * 
     */
    @SubjectPresent
    public static Result ci() {
        return redirect("/");
    }

    /**
     * Governance redirector.
     */
    @SubjectPresent
    public static Result governance() {
        return redirect("/");
    }

    /**
     * SVN redirector.
     */
    @SubjectPresent
    public static Result scm() {
        return redirect("/svn");
    }

    /**
     * Reporting tool redirector.
     */
    @SubjectPresent
    public static Result reporting() {
        return redirect("/maf-reporting");
    }
}
