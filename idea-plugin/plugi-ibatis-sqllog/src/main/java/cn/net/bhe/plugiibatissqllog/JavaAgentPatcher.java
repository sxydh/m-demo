package cn.net.bhe.plugiibatissqllog;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.projectRoots.Sdk;

import java.io.File;
import java.util.List;

/**
 * @author Administrator
 */
public class JavaAgentPatcher extends JavaProgramPatcher {

    @Override
    public void patchJavaParameters(Executor executor, RunProfile configuration, JavaParameters javaParameters) {
        if (!(configuration instanceof RunConfiguration)) {
            return;
        }
        Sdk sdk = javaParameters.getJdk();
        if (sdk == null) {
            return;
        }
        JavaSdkVersion javaSdkVersion = JavaSdk.getInstance().getVersion(sdk);
        if (javaSdkVersion == null) {
            return;
        }
        if (javaSdkVersion.compareTo(JavaSdkVersion.JDK_1_8) < 0) {
            return;
        }
        String agentIbatisSqllogJarPath = getAgentIbatisSqllogJarPath();
        if (StrUtil.isBlank(agentIbatisSqllogJarPath)) {
            return;
        }
        ParametersList vmParametersList = javaParameters.getVMParametersList();
        vmParametersList.addParametersString("-javaagent:" + agentIbatisSqllogJarPath);
    }

    public static String getAgentIbatisSqllogJarPath() {
        IdeaPluginDescriptor ideaPluginDescriptor = PluginManagerCore.getPlugin(PluginId.getId("cn.net.bhe.plugi-ibatis-sqllog"));
        if (ideaPluginDescriptor != null) {
            List<File> files = FileUtil.loopFiles(ideaPluginDescriptor.getPath(), null);
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.startsWith("agen-ibatis-sqllog")) {
                    String canonicalPath = FileUtil.getCanonicalPath(file);
                    if (canonicalPath.contains(StrUtil.SPACE)) {
                        canonicalPath = "\"" + canonicalPath + "\"";
                    }
                    return canonicalPath;
                }
            }
        }
        return StrUtil.EMPTY;
    }

}
