/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.mclauncher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.atlauncher.App;
import com.atlauncher.data.Account;
import com.atlauncher.data.Instance;
import com.atlauncher.data.mojang.auth.AuthenticationResponse;
import com.atlauncher.utils.Utils;
import com.google.gson.Gson;

public class MinecraftEduTeacherLauncher {

    public static Process launch(Account account, Instance instance, AuthenticationResponse response)
            throws IOException {
        StringBuilder cpb = new StringBuilder("");

        cpb.append(instance.getMinecraftEduLauncher());

        List<String> arguments = new ArrayList<String>();

        String path = App.settings.getJavaPath() + File.separator + "bin" + File.separator + "java";
        if (Utils.isWindows()) {
            path += "w";
        }
        arguments.add(path);
        arguments.add("-jar");
        arguments.add(instance.getMinecraftEduLauncher().getAbsolutePath());

        if (App.settings.getMemory() < instance.getMemory()) {
            if (Utils.getMaximumRam() < instance.getMemory()) {
                arguments.add("-Xmx" + App.settings.getMemory() + "M");
            } else {
                arguments.add("-Xmx" + instance.getMemory() + "M");
            }
        } else {
            arguments.add("-Xmx" + App.settings.getMemory() + "M");
        }
        if (App.settings.getPermGen() < instance.getPermGen()) {
            arguments.add("-XX:PermSize=" + instance.getPermGen() + "M");
        } else {
            arguments.add("-XX:PermSize=" + App.settings.getPermGen() + "M");
        }

        App.settings.log("Launching Minecraft Edu with the following arguments: " + arguments);
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(instance.getRootDirectory());
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }
}