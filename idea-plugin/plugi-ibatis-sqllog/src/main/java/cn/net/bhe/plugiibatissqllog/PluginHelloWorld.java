package cn.net.bhe.plugiibatissqllog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class PluginHelloWorld extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        Messages.showMessageDialog(
                JavaAgentPatcher.getAgentIbatisSqllogJarPath(),
                "PluginHelloWorld",
                Messages.getInformationIcon());
    }
}
