package com.kc.utils;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created with IntelliJ IDEA.
 * User: GLEB
 * Date: 07.04.14
 * Time: 23:31
 * To change this template use File | Settings | File Templates.
 */
public class MyFileUtils {

    public static void dellDir(Path target) throws IOException {
        FileUtils.cleanDirectory(target.toFile());
        FileUtils.deleteDirectory(target.toFile());
    }

}
