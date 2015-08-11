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

import java.util.Set;

import javax.inject.Inject;

import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanPersistRequest;

import framework.services.audit.IAuditLoggerService;

/**
 * Class which acts as a listener of the Ebean server.<br/>
 * Its methods are called when an object is modified (updated, deleted or
 * created).
 * 
 * @author Pierre-Yves Cloux
 */
public class CustomBeanPersistController implements BeanPersistController {
    @Inject
    private static IAuditLoggerService auditLoggerService;
    
    /**
     * Default constructor.
     */
    public CustomBeanPersistController() {
    }

    @Override
    public int getExecutionOrder() {
        return 0;
    }

    @Override
    public boolean isRegisterFor(Class<?> arg0) {
        return true;
    }

    @Override
    public void postLoad(Object bean, Set<String> parameters) {
    }

    @Override
    public void postInsert(BeanPersistRequest<?> beanPersistRequest) {
        if(getAuditLoggerService()!=null){
            getAuditLoggerService().logCreate(beanPersistRequest.getBean());
        }
    }

    @Override
    public void postDelete(BeanPersistRequest<?> beanPersistRequest) {
        if(getAuditLoggerService()!=null){
            getAuditLoggerService().logDelete(beanPersistRequest.getBean());
        }
    }

    @Override
    public void postUpdate(BeanPersistRequest<?> beanPersistRequest) {
        if(getAuditLoggerService()!=null){
            getAuditLoggerService().logUpdate(beanPersistRequest.getBean());
        }
    }

    @Override
    public boolean preUpdate(BeanPersistRequest<?> beanPersistRequest) {
        return true;
    }

    @Override
    public boolean preInsert(BeanPersistRequest<?> beanPersistRequest) {
        return true;
    }

    @Override
    public boolean preDelete(BeanPersistRequest<?> beanPersistRequest) {
        return true;
    }

    private static IAuditLoggerService getAuditLoggerService() {
        return auditLoggerService;
    }
}
