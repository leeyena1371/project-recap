package co.project.api.common.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;

public class VersionHelperUtil {

    private static String runningVersion = "";

    public String getRunningVersion(ServletConfig servletConfig) {
        // version
        if (StringUtils.isEmpty(runningVersion)) {
            runningVersion = StringUtils.substringBefore(
                    StringUtils.substringAfter(
                            servletConfig.getServletContext().getRealPath("/"),
                            "##"),
                    "/");
        }
        return runningVersion;
    }
}
