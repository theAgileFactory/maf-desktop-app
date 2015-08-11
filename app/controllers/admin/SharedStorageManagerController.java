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
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import play.Logger;
import play.Play;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.ControllersUtils;
import framework.commons.IFrameworkConstants;
import framework.services.storage.ISharedStorageService;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Table.ColumnDef.SorterType;
import framework.utils.Utilities;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.StringFormatFormatter;
import framework.utils.formats.StringFormatFormatter.Hook;

/**
 * The controller which is managing the access to the shared storage.<br/>
 * The shared storage is used to "exchange" (input and output) some files with
 * BizDock.
 * 
 * @author Pierre-Yves Cloux
 */
@Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
public class SharedStorageManagerController extends Controller {
    @Inject
    private ISharedStorageService sharedStorageService;
    
    public static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    /**
     * Default constroller.
     */
    public SharedStorageManagerController() {
    }

    private static Logger.ALogger log = Logger.of(SharedStorageManagerController.class);

    private static Table<SharedStorageFile> tableFileTemplate = new Table<SharedStorageFile>() {
        {
            this.addColumn("name", "name", "admin.shared_storage.view.file.name.label", SorterType.STRING_SORTER, true);
            this.setColumnCssClass("name", IMafConstants.BOOTSTRAP_COLUMN_4);

            this.addColumn("lastModified", "lastModified", "admin.shared_storage.view.file.last_modified.label", SorterType.DATE_TIME_SORTER, true);
            this.setJavaColumnFormatter("lastModified", new DateFormatter<SharedStorageFile>("dd/MM/yyyy HH:mm"));
            this.setColumnCssClass("lastModified", IMafConstants.BOOTSTRAP_COLUMN_3);

            this.addColumn("length", "size", "admin.shared_storage.view.file.size.label", SorterType.NONE, true);
            this.setColumnCssClass("length", IMafConstants.BOOTSTRAP_COLUMN_3);

            this.addColumn("downloadActionLink", "id", "", SorterType.NONE);
            this.setJavaColumnFormatter("downloadActionLink", new StringFormatFormatter<SharedStorageFile>(IMafConstants.DISPLAY_URL_FORMAT,
                    new Hook<SharedStorageFile>() {
                        @Override
                        public String convert(SharedStorageFile value) {
                            return routes.SharedStorageManagerController.download(value.getId()).url();
                        }
                    }));
            this.setColumnCssClass("downloadActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            this.setColumnValueCssClass("downloadActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

            this.addColumn("deleteActionLink", "id", "", SorterType.NONE);
            setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<SharedStorageFile>() {
                @Override
                public String apply(SharedStorageFile sharedStorageFile, Object value) {
                    String deleteConfirmationMessage =
                            MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION, Msg.get("default.delete.confirmation.message"));
                    String url = routes.SharedStorageManagerController.delete(sharedStorageFile.getId()).url();
                    return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                }
            });
            this.setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            this.setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

            this.setIdFieldName("id");
        }
    };

    /**
     * Display the content of the shared space for the current user.
     * 
     * @return a help page
     */
    public Result index() {
        try {
            
            Table<SharedStorageFile> loadedInputFileTable = getSharedStorageFiles(getSharedStorageService(), IMafConstants.INPUT_FOLDER_NAME);
            Table<SharedStorageFile> loadedOutputFileTable = getSharedStorageFiles(getSharedStorageService(), IMafConstants.OUTPUT_FOLDER_NAME);
            return ok(views.html.admin.plugin.sharedstorage_display.render(Msg.get("admin.plugin_manager.sidebar.shared_storage"), loadedInputFileTable,
                    loadedOutputFileTable));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Get the files from the specified shared storage folder.
     * 
     * @param sharedStorageService
     *            a shared storage service
     * @param sharedFolder
     *            a shared folder
     * @return a table of {@link SharedStorageFile}
     * @throws IOException
     */
    private static Table<SharedStorageFile> getSharedStorageFiles(ISharedStorageService sharedStorageService, String sharedFolder) throws IOException {
        String[] someFileNames = sharedStorageService.getFileList("/" + sharedFolder);
        List<SharedStorageFile> someFiles = new ArrayList<SharedStorageFile>();
        for (String someFileName : someFileNames) {
            File aFile = sharedStorageService.getFile(someFileName);
            if (aFile.isFile()) {
                someFiles.add(new SharedStorageFile(aFile, someFileName));
            }
        }
        Table<SharedStorageFile> someInputFileTable = tableFileTemplate.fill(someFiles);
        return someInputFileTable;
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
                    
                    String fileName = new String(Base64.decodeBase64(id));
                    response().setContentType("application/x-download");
                    response().setHeader("Content-disposition", "attachment; filename=" + getSharedStorageService().getFile(fileName).getName());
                    return ok(getSharedStorageService().getFileAsStream(fileName));
                } catch (Exception e) {
                    return ControllersUtils.logAndReturnUnexpectedError(e, log);
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
                    
                    String fileName = new String(Base64.decodeBase64(id));
                    getSharedStorageService().deleteFile(fileName);
                    Utilities.sendSuccessFlashMessage(Msg.get("admin.shared_storage.delete.success"));
                    return redirect(routes.SharedStorageManagerController.index());
                } catch (Exception e) {
                    return ControllersUtils.logAndReturnUnexpectedError(e, log);
                }
            }
        });
    }

    /**
     * Display the upload form.
     * 
     * @param folderName
     *            the folder name
     * @param isInput
     *            settled to true for input, to false for ouput
     */
    public Result uploadForm(String folderName, Boolean isInput) {
        return ok(views.html.admin.plugin.sharedstorage_upload.render(folderName, isInput,
                Play.application().configuration().getInt("maf.sftp.store.maxfilenumber")));
    }

    /**
     * Upload a new file into the shared storage.
     * 
     * @param isInput
     *            if true the field name containing the file uploaded is
     *            IFrameworkConstants.INPUT_FOLDER_NAME
     *            (IFrameworkConstants.OUTPUT_FOLDER_NAME otherwise)
     * @return
     */
    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = MAX_FILE_SIZE)
    public Promise<Result> upload(final boolean isInput) {
        final String folderName = isInput ? IFrameworkConstants.INPUT_FOLDER_NAME : IFrameworkConstants.OUTPUT_FOLDER_NAME;
        final 

        // Test if the max number of files is not exceeded
        String[] files;
        try {
            files = getSharedStorageService().getFileList("/" + folderName);
        } catch (IOException e1) {
            return redirectToIndexAsPromiseWithErrorMessage(null);
        }
        int numberOfFiles = files != null ? files.length : 0;
        if (numberOfFiles >= Play.application().configuration().getInt("maf.sftp.store.maxfilenumber")) {
            return redirectToIndexAsPromiseWithErrorMessage(Msg.get("admin.shared_storage.upload.error.max_number"));
        }

        // Perform the upload       
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try {
                    MultipartFormData body = request().body().asMultipartFormData();
                    FilePart filePart = body.getFile(folderName);
                    if (filePart != null) {
                        IOUtils.copy(new FileInputStream(filePart.getFile()),
                                getSharedStorageService().writeFile("/" + folderName + "/" + filePart.getFilename(), true));
                        Utilities.sendSuccessFlashMessage(Msg.get("admin.shared_storage.upload.success"));
                    } else {
                        Utilities.sendErrorFlashMessage(Msg.get("admin.shared_storage.upload.no_file"));
                    }
                } catch (Exception e) {
                    Utilities.sendErrorFlashMessage(Msg.get("admin.shared_storage.upload.file.size.invalid",
                            FileUtils.byteCountToDisplaySize(MAX_FILE_SIZE)));
                    String message = String.format("Failure while uploading a new file in %s", folderName);
                    log.error(message);
                    throw new IOException(message, e);
                }
                return redirect(routes.SharedStorageManagerController.index());
            }
        });
    }

    /**
     * Return a promise of redirect to the home page.
     * 
     * @param message
     *            a flash message to be displayed as an error (if not null)
     */
    private Promise<Result> redirectToIndexAsPromiseWithErrorMessage(final String message) {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                if (message != null) {
                    Utilities.sendErrorFlashMessage(message);
                }
                return redirect(routes.SharedStorageManagerController.index());
            }
        });
    }

    /**
     * A class which encapsulate the {@link File} object (and expose appropriate
     * accessors).
     * 
     * @author Pierre-Yves Cloux
     */
    public static class SharedStorageFile {
        private String relativeFilePath;
        private File file;

        /**
         * Default constructor.
         */
        public SharedStorageFile() {
        }

        /**
         * Constructor with the corresponding files.
         * 
         * @param file
         *            the file (holds meta-information about the file)
         * @param relativeFilePath
         *            the relative file path (the one used by the
         *            {@link ISharedStorageService})
         */
        public SharedStorageFile(File file, String relativeFilePath) {
            super();
            this.file = file;
            this.relativeFilePath = relativeFilePath;
        }

        /**
         * Getter for the id.
         */
        public String getId() {
            return new String(Base64.encodeBase64(getRelativeFilePath().getBytes(), false, true));
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
         * Get the relative file path.
         */
        public String getRelativeFilePath() {
            return relativeFilePath;
        }

        /**
         * Getter for the file.
         */
        private File getFile() {
            return this.file;
        }
    }

    private ISharedStorageService getSharedStorageService() {
        return sharedStorageService;
    }
}
