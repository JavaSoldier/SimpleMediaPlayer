package com.kc.security;

import com.kc.utils.MyFileUtils;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Processor;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import sun.misc.BASE64Decoder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: GLEB
 * Date: 26.03.14
 * Time: 21:10
 * To change this template use File | Settings | File Templates.
 */
public class Security {
    public static final Path TO_FOLDER = Paths.get("C:\\tempFolder");
    public static final Path TO_LOG_FILE = Paths.get("D:\\log.txt");
    static final BASE64Decoder BASE_64_ENCODER = new BASE64Decoder();
    static final String DEFAULT_ENCODING = "UTF-8";

    public static void securityAction() throws IOException {
        createFolderAndFiles();

        String sysInfoByCustomer = getText(Security.class.getResourceAsStream("/text/sys.txt"));
        if (!createSystemInfoString().equals(sysInfoByCustomer)) {
            throw new RuntimeException("try to run on different PC");
        }
    }

    public static void log(Exception e) throws IOException {
        FileWriter fileWriter = new FileWriter(TO_LOG_FILE.toFile(), true);
        StringBuilder result = new StringBuilder();
        result.append("\n");
        result.append(e.getMessage());
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            result.append(stackTraceElement.getClassName()).append(" ").
                    append(stackTraceElement.getMethodName()).append(" ")
                    .append(stackTraceElement.getLineNumber()).append(" ");
        }
        result.append("\n");
        fileWriter.write(e.toString() + " at " + result);
        fileWriter.flush();
        fileWriter.close();
    }

    private static String getText(InputStream stream) throws IOException {
        StringBuilder sBuilder = new StringBuilder();
        Scanner scanner = new Scanner(stream);
        while (scanner.hasNextLine()) {
            sBuilder.append(scanner.nextLine());
            sBuilder.append("\n");
        }
        scanner.close();
        stream.close();
        return base64decode(sBuilder.toString());
    }

    private static void createFolderAndFiles() throws IOException {
        if (Files.exists(TO_FOLDER)) {
            MyFileUtils.dellDir(TO_FOLDER);
        }
        Files.createDirectories(TO_FOLDER);
        Files.setAttribute(TO_FOLDER, "dos:hidden", true);
        if (!Files.exists(TO_LOG_FILE)) {
            Files.createFile(TO_LOG_FILE);
        }
    }

    private static String createSystemInfoString() throws IOException {
        StringBuilder sysInfoString = new StringBuilder();
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        sysInfoString.append(os.toString());
        sysInfoString.append("\n");

        HardwareAbstractionLayer hal = si.getHardware();
        for (Processor cpu : hal.getProcessors()) {
            sysInfoString.append(cpu.toString());
            sysInfoString.append("\n");
        }

        sysInfoString.append(FormatUtil.formatBytes(hal.getMemory().getTotal()));
        sysInfoString.append("\n");

//        InetAddress ip = InetAddress.getLocalHost();
//        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
//        byte[] mac = network.getHardwareAddress();
//
//        for (int i = 0; i < mac.length; i++) {
//            sysInfoString.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
//        }
        sysInfoString.append("\n");
        return sysInfoString.toString();
    }

    private static String base64decode(String text) {

        try {
            return new String(BASE_64_ENCODER.decodeBuffer(text), DEFAULT_ENCODING);
        } catch (IOException e) {
            return null;
        }

    }
}
