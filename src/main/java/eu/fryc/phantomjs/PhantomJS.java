package eu.fryc.phantomjs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PhantomJS {

    private static final String PREFIX = "phantomjs_";

    private static final File EXECUTABLE_TMP_PATH = initializeExecutable();
    private static boolean executableWritten = false;

    private Logger log = Logger.getLogger(PhantomJS.class.getName());

    static String getArchitecture() {
        String osType = System.getProperty("os.arch");
        return osType;
    }

    InputStream getResourceStreamByArchitecture() {
        String architecture = getArchitecture();
        String resourceName = PREFIX + architecture;

        return this.getClass().getResourceAsStream(resourceName);
    }

    static String getSuffixByArchitecture() {
        return "";
    }

    static File initializeExecutable() {
        try {
            File executable = File.createTempFile("phantomjs", getSuffixByArchitecture());
            executable.deleteOnExit();
            return executable;
        } catch (IOException e) {
            throw new IllegalStateException("Can't initialize PhantomJS executable", e);
        }
    }

    File getExecutableFile() {
        if (!executableWritten) {
            synchronized (EXECUTABLE_TMP_PATH) {
                writeStreamToFile(getResourceStreamByArchitecture(), EXECUTABLE_TMP_PATH);
                EXECUTABLE_TMP_PATH.setReadable(true);
                EXECUTABLE_TMP_PATH.setExecutable(true);
                if (!EXECUTABLE_TMP_PATH.canExecute()) {
                    throw new IllegalStateException("Cannot set phantomjs file as executable");
                }
                executableWritten = true;
            }
        }
        return EXECUTABLE_TMP_PATH;
    }

    void writeStreamToFile(InputStream inputStream, File file) {
        try {
            byte[] buffer = new byte[8192];
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            int r;
            while ((r = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, r);
            }
            fileOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            throw new IllegalStateException("Can't write stream to file", e);
        }

    }

    public Process execute(File scriptFile, String... parameters) {
        File executable = getExecutableFile();

        List<String> paramList = new LinkedList<String>();
        paramList.add(executable.getAbsolutePath());
        paramList.add(scriptFile.getAbsolutePath());
        paramList.addAll(Arrays.asList(parameters));

        parameters = paramList.toArray(new String[paramList.size()]);

        try {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Executing phantomjs: " + paramList);
            }
            
            return Runtime.getRuntime().exec(parameters);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot execute script " + scriptFile, e);
        }
    }
}
