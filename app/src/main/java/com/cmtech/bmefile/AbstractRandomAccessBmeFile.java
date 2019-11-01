package com.cmtech.bmefile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * AbstractRandomAccessBmeFile: 可随机访问的BmeFile
 * Created by Chenm, 2018-12-03
 */

public abstract class AbstractRandomAccessBmeFile extends BmeFile {
    protected RandomAccessFile raf;

    protected AbstractRandomAccessBmeFile(String fileName) throws IOException {
        super(fileName);
    }

    protected AbstractRandomAccessBmeFile(String fileName, BmeFileHead head) throws IOException{
        super(fileName, head);
    }

    @Override
    protected void createIOStream() throws FileNotFoundException{
        raf = new RandomAccessFile(file, "rw");
        in = raf;
        out = raf;
    }

    @Override
    protected boolean isEof() throws IOException {
        return (raf == null || raf.length() == raf.getFilePointer());
    }

    @Override
    public void close() throws IOException {
        try {
            if(raf != null) {
                raf.close();
            }
        } catch(IOException e) {
            throw new IOException(e);
        } finally {
            in = null;
            out = null;
            raf = null;
        }
    }
}