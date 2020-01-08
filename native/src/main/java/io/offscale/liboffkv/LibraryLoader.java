package io.offscale.liboffkv;

import java.io.*;

public class LibraryLoader {
    public static void load(String name) throws IOException {
        String filename = System.mapLibraryName(name);
        File file;

        try (InputStream in = LibraryLoader.class.getClassLoader().getResourceAsStream(filename)) {
            if (in == null)
                throw new FileNotFoundException(filename);

            int pos = filename.lastIndexOf('.');
            file = File.createTempFile(filename.substring(0, pos), filename.substring(pos));
            file.deleteOnExit();

            try (OutputStream out = new FileOutputStream(file)) {
                byte[] buf = new byte[1024 * 16];

                int len;
                while ((len = in.read(buf)) >= 0) {
                    out.write(buf, 0, len);
                }
            }
        }

        System.load(file.getAbsolutePath());
    }
}
