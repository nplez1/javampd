package org.bff.javampd.integrationdata;

import org.bff.javampd.exception.MPDException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DataLoader {
    private static List<File> testRootFiles;

    public static Collection<File> getTestFiles(File directory) {
        List<File> testFiles = new ArrayList<File>();
        File[] files = directory.listFiles();
        for (File file : files) {
            testFiles.add(file);
        }

        return testFiles;
    }

    public static void loadData(File f) throws MPDException {
        File[] files = f.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                loadData(file);
            } else {
                if (file.getName().endsWith(Songs.EXTENSION)) {
                    Songs.loadSong(file, f);
                }
            }
        }
    }
}
