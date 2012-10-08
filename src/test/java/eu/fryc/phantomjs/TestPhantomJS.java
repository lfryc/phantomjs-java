package eu.fryc.phantomjs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

public class TestPhantomJS {

    private PhantomJS phantomJS = new PhantomJS();

    @Test
    public void testResourceForCurrentArchitecture() {

        assertNotNull("current architecture must have correcponding executable resource",
                phantomJS.getResourceStreamByArchitecture());
    }

    @Test
    public void testResourceFileExtraction() {
        File executable = phantomJS.getExecutableFile();

        assertTrue(executable.exists());
        assertTrue(executable.canExecute());
    }

    @Test
    public void testWritingOutputStreamToFile() {
        final String stringToWrite = "xyz";

        try {
            File file = File.createTempFile("phantomjs-test", "");
            file.deleteOnExit();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(stringToWrite.getBytes());

            phantomJS.writeStreamToFile(inputStream, file);

            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line = bufferedReader.readLine();

            assertEquals(line, stringToWrite);

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    @Test
    public void testExecution() throws IOException {
        File script = new File("src/test/resources/test.js");
        
        Process process = phantomJS.execute(script);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
        String line = reader.readLine();
        
        assertEquals("testing", line);
    }
}
