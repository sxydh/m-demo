package cn.net.bhe.ideaplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

/**
 * @author Administrator
 */
public class HelloWorld extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        Messages.showInfoMessage("Hello World !", "Message");
    }
}
