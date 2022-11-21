package cn.net.bhe.activitispringboot.controller;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

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
    public String deploy(@RequestParam("bpmn") MultipartFile bpmn) throws Exception {
        Deployment deployment = repositoryService.createDeployment()
                // 任意取
                .name(bpmn.getOriginalFilename())
                // resourceName后缀必须是bpmn
                .addInputStream(
                        Objects.requireNonNull(bpmn.getOriginalFilename())
                                .substring(0, bpmn.getOriginalFilename().indexOf(".")) + ".bpmn",
                        bpmn.getInputStream())
                .deploy();
        System.out.println(deployment);
        return deployment.getId();
    }

    @RequestMapping("/start")
    public String start(@RequestParam String processDefinitionKey) {
        // setAuthenticatedUserId设置流程实例的start_user_id_
        Authentication.setAuthenticatedUserId(RandomStringUtils.randomAlphabetic(5));
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                // 流程key
                processDefinitionKey,
                // 业务唯一标识
                RandomStringUtils.randomAlphabetic(10),
                // 流程变量
                Map.of(
                        "key1", RandomStringUtils.randomAlphabetic(3),
                        "key2", RandomStringUtils.randomAlphabetic(3)));
        System.out.println(processInstance);
        return processInstance.getId();
    }

    @RequestMapping("/complete")
    public String complete() {
        // 执行最近的任务
        Task task = taskService.createTaskQuery().orderByTaskCreateTime().desc().list().get(0);
        // 设置执行人
        taskService.setAssignee(task.getId(), RandomStringUtils.randomAlphabetic(5));
        taskService.complete(
                task.getId(),
                Map.of(
                        "gatewayVal", "1",
                        "key1", RandomStringUtils.randomAlphabetic(3),
                        "key2", RandomStringUtils.randomAlphabetic(3)));
        return task.getId();
    }

}
