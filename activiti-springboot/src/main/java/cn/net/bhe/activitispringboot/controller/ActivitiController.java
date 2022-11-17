package cn.net.bhe.activitispringboot.controller;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("/activiti")
public class ActivitiController {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private TaskService taskService;

    @RequestMapping("/deploy")
    public void deploy() {
        String resourceName = "processes/diagram.bpmn";
        repositoryService.createDeployment()
                .addClasspathResource(resourceName)
                .deploy();
    }

    @RequestMapping("/start")
    public void start() {
        Authentication.setAuthenticatedUserId(RandomStringUtils.randomAlphabetic(5));
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "Process_1",
                RandomStringUtils.randomAlphabetic(10),
                Map.of(
                        "key1", RandomStringUtils.randomAlphabetic(3),
                        "key2", RandomStringUtils.randomAlphabetic(3)));
        System.out.println(processInstance);
        claim(taskListDo());
    }

    private void claim(List<Task> taskList) {
        for (Task ele : taskList) {
            if (ObjectUtils.isEmpty(ele.getAssignee())) {
                taskService.claim(ele.getId(), RandomStringUtils.randomAlphabetic(5));
            }
        }
    }

    @RequestMapping("/taskList")
    public void taskList() {
        taskListDo();
    }

    private List<Task> taskListDo() {
        List<Task> taskList = taskService.createTaskQuery().orderByTaskCreateTime().asc().list();
        System.out.println(taskList);
        return taskList;
    }

    @RequestMapping("/complete")
    public void complete() {
        List<Task> taskList = taskListDo();
        Assert.notEmpty(taskList, "流程已结束");
        Task task = taskList.get(0);
        taskService.setAssignee(task.getId(), RandomStringUtils.randomAlphabetic(5));
        taskService.complete(
                task.getId(),
                Map.of(
                        "gatewayVal", "1",
                        "key1", RandomStringUtils.randomAlphabetic(3),
                        "key2", RandomStringUtils.randomAlphabetic(3)));
        taskList = taskListDo();
        Assert.notEmpty(taskList, "流程已结束");
        claim(taskList);
    }

}
