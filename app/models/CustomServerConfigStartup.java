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
package models;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.event.ServerConfigStartup;

/**
 * Implementation of the {@link ServerConfigStartup} interface to be used to
 * configure the Ebean server.
 * 
 * @author Pierre-Yves Cloux
 */
public class CustomServerConfigStartup implements ServerConfigStartup {
    @Override
    public void onStart(ServerConfig serverConfig) {
        serverConfig.add(new CustomBeanPersistController());
    }
}