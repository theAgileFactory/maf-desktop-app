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
package controllers.my;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.ControllersUtils;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IPersonalStoragePlugin;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Table.ColumnDef.SorterType;
import framework.utils.formats.DateFormatter;
import play.Configuration;
import play.Logger;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Each user of MAF is allocated a personal storage space.<br/>
 * This one can be use as a temporary storage for data (Example: generation of a
 * report asynchronously).<br/>
 * The personal space is "cleaned" every 12 hours so it cannot be used as a long
 * term storage.
 * 
 * @author Pierre-Yves Cloux
 */
@Restrict({ @Group(IMafConstants.PERSONAL_SPACE_READ_PERMISSION) })
public class MyPersonalStorage extends Controller {
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private IPersonalStoragePlugin personalStoragePlugin;
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private Configuration configuration;

    private static Logger.ALogger log = Logger.of(MyPersonalStorage.class);

    private static Table<PersonalStorageFile> tableFileTemplate = new Table<PersonalStorageFile>() {
        {
            this.addColumn("name", "name", "my.personalstorage.file.name.label", SorterType.STRING_SORTER, true);

            this.addColumn("lastModified", "lastModified", "my.personalstorage.file.lastmodified.label", SorterType.DATE_TIME_SORTER, true);
            this.setJavaColumnFormatter("lastModified", new DateFormatter<PersonalStorageFile>("dd/MM/yyyy HH:mm"));

            this.addColumn("length", "size", "my.personalstorage.file.size.label", SorterType.NONE, true);

            this.addColumn("deleteActionLink", "id", "", SorterType.NONE);
            setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<PersonalStorageFile>() {
                @Override
                public String apply(PersonalStorageFile personalStorageFile, Object value) {
                    String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                            Msg.get("default.delete.confirmation.message"));
                    String url = routes.MyPersonalStorage.delete(personalStorageFile.getId()).url();
                    return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                }
            });
            this.setColumnValueCssClass("deleteActionLink", "rowlink-skip");

            this.setLineAction(new IColumnFormatter<PersonalStorageFile>() {
                @Override
                public String apply(PersonalStorageFile personalStorageFile, Object value) {
                    return routes.MyPersonalStorage.download(personalStorageFile.getId()).url();
                }
            });

            this.setIdFieldName("id");
        }
    };

    /**
     * Display the content of the personal space for the current user.
     * 
     * @return a help page
     */
    public Result index() {
        try {
            String currentUserUid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            List<PersonalStorageFile> files = new ArrayList<PersonalStorageFile>();
            for (File file : getPersonalStoragePlugin().getContentView(currentUserUid)) {
                files.add(new PersonalStorageFile(file));
            }
            Table<PersonalStorageFile> loadedTable = tableFileTemplate.fill(files);
            return ok(views.html.my.personalstorage_display.render(loadedTable));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }

    /**
     * Return the file associated with the specified id.
     * 
     * @param id
     *            the file id to download
     */
    public Promise<Result> download(final String id) {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try {
                    String currentUserUid = getUserSessionManagerPlugin().getUserSessionId(ctx());
                    String fileName = new String(Base64.decodeBase64(id));
                    response().setContentType("application/x-download");
                    response().setHeader("Content-disposition", "attachment; filename=" + fileName);
                    return ok(getPersonalStoragePlugin().readFile(currentUserUid, fileName));
                } catch (Exception e) {
                    return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
                }
            }
        });
    }

    /**
     * Delete the file associated with the specified id.
     * 
     * @param id
     *            the entry id to delete
     */
    public Promise<Result> delete(final String id) {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try {

                    String currentUserUid = getUserSessionManagerPlugin().getUserSessionId(ctx());

                    String fileName = new String(Base64.decodeBase64(id));
                    getPersonalStoragePlugin().deleteFile(currentUserUid, fileName);
                    return redirect(routes.MyPersonalStorage.index());
                } catch (Exception e) {
                    return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
                }
            }
        });
    }

    /**
     * A class which encapsulate the {@link File} object (and expose appropriate
     * accessors).
     * 
     * @author Pierre-Yves Cloux
     */
    public static class PersonalStorageFile {
        private File file;

        /**
         * Default constructor.
         */
        public PersonalStorageFile() {
        }

        /**
         * Constructor with the corresponding file.
         * 
         * @param file
         *            the file.
         */
        public PersonalStorageFile(File file) {
            super();
            this.file = file;
        }

        /**
         * Getter for the id.
         */
        public String getId() {
            return new String(Base64.encodeBase64(getName().getBytes(), false, true));
        }

        /**
         * Getter for the name.
         */
        public String getName() {
            return getFile().getName();
        }

        /**
         * Get as a date the last update of the file.
         */
        public Date getLastModified() {
            return new Date(getFile().lastModified());
        }

        /**
         * Get the size of the file.
         */
        public String getSize() {
            return FileUtils.byteCountToDisplaySize(getFile().length());
        }

        /**
         * Getter for the file.
         */
        private File getFile() {
            return file;
        }
    }

    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    private IPersonalStoragePlugin getPersonalStoragePlugin() {
        return personalStoragePlugin;
    }

    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    private Configuration getConfiguration() {
        return configuration;
    }
}
