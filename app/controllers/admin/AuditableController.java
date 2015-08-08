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
package controllers.admin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import play.data.Form;
import play.i18n.Messages;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.ControllersUtils;
import framework.commons.DataType;
import framework.services.ServiceManager;
import framework.services.audit.AuditLoggerUtilities;
import framework.services.audit.Auditable;
import framework.services.notification.INotificationManagerPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IPersonalStoragePlugin;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolder;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.PickerHandler;
import framework.utils.PickerHandler.Handle;
import framework.utils.SysAdminUtils;
import framework.utils.Table;
import framework.utils.Table.ColumnDef.SorterType;
import framework.utils.TableExcelRenderer;
import framework.utils.Utilities;

/**
 * The auditable controller.
 * 
 * @author Pierre-Yves Cloux
 */
@Restrict({ @Group(IMafConstants.ADMIN_AUDIT_LOG_PERMISSION) })
public class AuditableController extends Controller {

    // Set to true for activating the search box
    public static final boolean PICKER_OBJECTCLASS_SEARCH = false;

    // Set to true to enable navigation of values
    public static final boolean PICKER_OBJECTCLASS_NAVIGABLE = false;

    private static Logger.ALogger log = Logger.of(AuditableController.class);

    private static Table<Auditable> tableTemplate = new Table<Auditable>() {
        {

            this.addColumn("objectClass", "objectClass", "admin.auditable.object_class.label", SorterType.NONE, true);

            this.addColumn("isAuditable", "isAuditable", "admin.auditable.is_auditable.label", SorterType.NONE, true);

            this.addColumn("editActionLink", "objectClass", "", SorterType.NONE);
            setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

            this.addColumn("deleteActionLink", "objectClass", "", SorterType.NONE);
            setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

            this.setIdFieldName("objectClass");
        }
    };

    private static Form<Auditable> auditableForm = Form.form(Auditable.class);

    /*--------------------------------------------------------------------------------
     Controllers actions                               
     --------------------------------------------------------------------------------*/

    /**
     * Display a list of Auditable.
     */
    public Result listAuditable() {
        try {
            Table<Auditable> table = tableTemplate.fill(AuditLoggerUtilities.getInstance().getAllActiveAuditable());
            return ok(views.html.admin.audit.auditable_table.render(Messages.get("admin.auditable.list.title"), table));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Delete the Auditable associated with the specified objectClass.
     * 
     * @param objectClass
     *            an Auditable objectClasst
     */
    public Result deleteAuditable(String objectClass) {
        try {
            AuditLoggerUtilities.getInstance().deleteAuditable(objectClass);
            Utilities.sendSuccessFlashMessage(Messages.get("admin.auditable.delete.success"));
            return redirect(routes.AuditableController.listAuditable());
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Display the edition form for the Auditable associated with the specified
     * objectClass.
     * 
     * @param objectClass
     *            an Auditable objectClass
     */
    public Result editAuditable(String objectClass) {
        try {
            Auditable auditable = AuditLoggerUtilities.getInstance().getAuditableFromObjectClass(objectClass);
            if (auditable != null) {
                Form<Auditable> loadedForm = auditableForm.fill(auditable);
                return ok(views.html.admin.audit.auditable_form.render(Messages.get("admin.auditable.manage.title"), loadedForm));
            } else {
                Utilities.sendErrorFlashMessage(Msg.get("admin.auditable.manage.not_found"));
                return redirect(routes.AuditableController.listAuditable());
            }
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Display an edition form for a new Auditable.
     */
    public Result createAuditable() {
        try {
            Auditable auditable = new Auditable();
            Form<Auditable> loadedForm = auditableForm.fill(auditable);
            return ok(views.html.admin.audit.auditable_form.render(Messages.get("admin.auditable.manage.title"), loadedForm));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Save the Auditable submitted by an edition form.
     */
    public Result saveAuditable() {
        try {
            Form<Auditable> boundForm = auditableForm.bindFromRequest();
            if (boundForm.hasErrors()) {
                return ok(views.html.admin.audit.auditable_form.render(Messages.get("admin.auditable.manage.title"), boundForm));
            }
            Auditable auditable = boundForm.get();
            AuditLoggerUtilities.getInstance().saveAuditable(auditable);
            Utilities.sendSuccessFlashMessage(Messages.get("admin.auditable.manage.success", auditable));
            return redirect(routes.AuditableController.listAuditable());
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Generate an excel representation of all the Auditable objects.
     */
    public Promise<Result> excelAuditable() {
        final List<Auditable> listOfauditable = AuditLoggerUtilities.getInstance().getAllActiveAuditable();
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try {
                    Table<Auditable> excelTableTemplate = new Table<Auditable>() {
                        {
                            this.addColumn("objectClass", "objectClass", "admin.auditable.object_class.label", SorterType.NONE, true);
                            this.addColumn("isAuditable", "isAuditable", "admin.auditable.is_auditable.label", SorterType.NONE, true);
                            this.setIdFieldName("objectClass");
                        }
                    };
                    Table<Auditable> exportTable = excelTableTemplate.fill(listOfauditable);
                    byte[] excelFile = TableExcelRenderer.renderNotFormatted(exportTable);
                    response().setContentType("application/x-download");
                    response().setHeader("Content-disposition", "attachment; filename=export.xlsx");
                    return ok(excelFile);
                } catch (Exception e) {
                    return ControllersUtils.logAndReturnUnexpectedError(e, log);
                }
            }
        });
    }

    /**
     * Creates an archive of all the audit logs files and set it into the
     * personal space of the current user.
     */
    public Promise<Result> exportAuditLogs() {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try {
                    IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME,
                            IUserSessionManagerPlugin.class);
                    final String currentUserId = userSessionManagerPlugin.getUserSessionId(ctx());
                    final String errorMessage = Msg.get("admin.auditable.export.notification.error.message");
                    final String successTitle = Msg.get("admin.auditable.export.notification.success.title");
                    final String successMessage = Msg.get("admin.auditable.export.notification.success.message");
                    final String notFoundMessage = Msg.get("admin.auditable.export.notification.not_found.message");
                    final String errorTitle = Msg.get("admin.auditable.export.notification.error.title");

                    // Audit log export requested
                    if (log.isDebugEnabled()) {
                        log.debug("Audit log export : Audit log export requested by " + currentUserId);
                    }

                    // Execute asynchronously
                    SysAdminUtils.scheduleOnce(false, "AUDITABLE", Duration.create(0, TimeUnit.MILLISECONDS), new Runnable() {
                        @Override
                        public void run() {
                            // Find the files to be archived
                            String logFilesPathAsString = Play.application().configuration().getString("maf.audit.log.location");
                            File logFilesPath = new File(logFilesPathAsString);
                            File logDirectory = logFilesPath.getParentFile();

                            if (!logDirectory.exists()) {
                                log.error("Log directory " + logDirectory.getAbsolutePath() + " is not found, please check the configuration");
                                return;
                            }

                            final String logFilePrefix = (logFilesPath.getName().indexOf('.') != -1 ? logFilesPath.getName().substring(0,
                                    logFilesPath.getName().indexOf('.')) : logFilesPath.getName());

                            if (log.isDebugEnabled()) {
                                log.debug("Audit log export : Selecting files to archive");
                            }

                            File[] filesToArchive = logDirectory.listFiles(new FilenameFilter() {
                                @Override
                                public boolean accept(File dir, String name) {
                                    return name.startsWith(logFilePrefix);
                                }
                            });

                            INotificationManagerPlugin notificationManagerPlugin = ServiceManager.getService(INotificationManagerPlugin.NAME,
                                    INotificationManagerPlugin.class);
                            if (filesToArchive.length != 0) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Audit log export : zipping the " + filesToArchive.length + " archive files");
                                }

                                // Write to the user personal space
                                IPersonalStoragePlugin personalStoragePlugin = ServiceManager.getService(IPersonalStoragePlugin.NAME,
                                        IPersonalStoragePlugin.class);
                                final String fileName = String.format("auditExport_%1$td_%1$tm_%1$ty_%1$tH-%1$tM-%1$tS.zip", new Date());
                                ZipOutputStream out = null;
                                try {
                                    OutputStream personalOut = personalStoragePlugin.createNewFile(currentUserId, fileName);
                                    out = new ZipOutputStream(personalOut);
                                    for (File fileToArchive : filesToArchive) {
                                        ZipEntry e = new ZipEntry(fileToArchive.getName());
                                        out.putNextEntry(e);
                                        byte[] data = FileUtils.readFileToByteArray(fileToArchive);
                                        out.write(data, 0, data.length);
                                        out.closeEntry();
                                    }
                                    notificationManagerPlugin.sendNotification(currentUserId, NotificationCategory.getByCode(Code.AUDIT), successTitle,
                                            successMessage, controllers.my.routes.MyPersonalStorage.index().url());

                                } catch (Exception e) {
                                    log.error("Fail to export the audit archives", e);
                                    notificationManagerPlugin.sendNotification(currentUserId, NotificationCategory.getByCode(Code.ISSUE), errorTitle,
                                            errorMessage, controllers.admin.routes.AuditableController.listAuditable().url());
                                } finally {
                                    IOUtils.closeQuietly(out);
                                }
                            } else {
                                log.error("No audit archive found in the folder");
                                notificationManagerPlugin.sendNotification(currentUserId, NotificationCategory.getByCode(Code.ISSUE), errorTitle,
                                        notFoundMessage, controllers.admin.routes.AuditableController.listAuditable().url());
                            }
                        }
                    });

                    return ok(Json.newObject());
                } catch (Exception e) {
                    return ControllersUtils.logAndReturnUnexpectedError(e, log);
                }
            }
        });
    }

    /*--------------------------------------------------------------------------------
     objectClass                                   
     --------------------------------------------------------------------------------*/

    /**
     * Return a JSON representation of the picker values.
     */
    public Result singleValuePickerObjectClassValues() {
        PickerHandler<String> singleValuePickerObjectClass = new PickerHandler<String>(String.class, new Handle<String>() {
            @Override
            public ISelectableValueHolderCollection<String> getInitialValueHolders(List<String> values, Map<String, String> context) {
                return AuditableController.getSelectableValuesListForObjectClass(context.get("currentObjectClass"));
            }
        });
        return singleValuePickerObjectClass.handle(request());
    }

    /**
     * Return the values which can be "selected" in the picker for the field
     * objectClass.
     * 
     * @param currentObjectClass
     *            the class name of the current object
     */
    public static ISelectableValueHolderCollection<String> getSelectableValuesListForObjectClass(String currentObjectClass) {
        // Get all the selectable entities from the configuration
        ISelectableValueHolderCollection<String> selectableObjects = new DefaultSelectableValueHolderCollection<String>();
        for (DataType dataType : DataType.getAllAuditableDataTypes()) {
            selectableObjects.add(new DefaultSelectableValueHolder<String>(dataType.getDataTypeClassName(), dataType.getLabel()));
        }
        // Remove the previously selected entities
        List<Auditable> auditables = AuditLoggerUtilities.getInstance().getAllActiveAuditable();
        for (Auditable auditable : auditables) {
            selectableObjects.remove(auditable.objectClass);
        }
        // Add the current value if any (so that it could be displayed and
        // selected again)
        if (!StringUtils.isBlank(currentObjectClass)) {
            selectableObjects
                    .add(new DefaultSelectableValueHolder<String>(currentObjectClass, DataType.getDataTypeFromClassName(currentObjectClass).getLabel()));
        }
        return selectableObjects;
    }

    /**
     * Returns the value which is selected for the specified field.
     * 
     * @param object
     *            the auditable object
     */
    public static ISelectableValueHolder<String> getSelectedValueForObjectClass(Auditable object) {
        return new DefaultSelectableValueHolder<String>(object.objectClass, DataType.getDataTypeFromClassName(object.objectClass).getLabel());
    }
}
