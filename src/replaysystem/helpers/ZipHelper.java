package replaysystem.helpers;

import arc.files.Fi;
import arc.util.io.Streams;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipHelper {

    /**
     * Packs a directory or file into a ZIP archive.
     *
     * @param sourceFolder   The directory to compress.
     * @param destinationZip The output ZIP file.
     */
    public static void zipFolder(Fi sourceFolder, Fi destinationZip) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destinationZip.file()))) {
            compressDirectory(sourceFolder, sourceFolder, zos);
        }
    }

    private static void compressDirectory(Fi rootFolder, Fi currentFile, ZipOutputStream zos) throws IOException {
        if (currentFile.isDirectory()) {
            for (var child : currentFile.list()) {
                compressDirectory(rootFolder, child, zos);
            }
        } else {
            var relativePath = currentFile.path().substring(rootFolder.path().length() + 1);

            var zipEntry = new ZipEntry(relativePath);
            zos.putNextEntry(zipEntry);

            try (var is = currentFile.read()) {
                Streams.copy(is, zos);
            }

            zos.closeEntry();
        }
    }
}
