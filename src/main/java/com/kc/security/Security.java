package com.kc.security;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Processor;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
    static final Path toFolder = Paths.get("C:\\temp");
    static final Path toLogFile = Paths.get("D:\\log.txt");

    public static void securityAction() throws IOException {
        createFolderAndFiles();

        String sysInfoByCustomer = getText(Security.class.getResourceAsStream("/text/sys.txt"));
        if (!createSystemInfoString().equals(sysInfoByCustomer)) {
            throw new RuntimeException("try to run on different PC");
        }
    }

    public static void log(Exception e) throws IOException {
        FileWriter fileWriter = new FileWriter(toLogFile.toFile());
        e.printStackTrace();
        StringBuilder result = new StringBuilder();
        result.append("\n");
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
        // return decrypt(sBuilder.toString(), "edik");
        return sBuilder.toString();
    }

    private static void createFolderAndFiles() throws IOException {
        if (!(Files.isDirectory(toFolder, LinkOption.NOFOLLOW_LINKS)
                && Files.exists(toLogFile, LinkOption.NOFOLLOW_LINKS))) {
            Files.createDirectories(toFolder);
            Files.setAttribute(toFolder, "dos:hidden", true);
            Files.createFile(toLogFile);
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

        InetAddress ip = InetAddress.getLocalHost();
        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
        byte[] mac = network.getHardwareAddress();

        for (int i = 0; i < mac.length; i++) {
            sysInfoString.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
        }
        sysInfoString.append("\n");
        return sysInfoString.toString();
    }

    private static String decrypt(String s, String keyWord) {
        byte[] text = s.getBytes();
        byte[] result = new byte[text.length];
        byte[] keyArr = keyWord.getBytes();
        for (int i = 0; i < text.length; i++) {
            result[i] = (byte) (text[i] ^ keyArr[i % keyArr.length]);
        }
        return new String(result);
    }

}
