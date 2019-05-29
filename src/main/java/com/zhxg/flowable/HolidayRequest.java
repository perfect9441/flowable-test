package com.zhxg.flowable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

public class HolidayRequest {
	public static void main(String[] args) {
		ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
											.setJdbcUrl("jdbc:mysql://localhost:3306/test")
											.setJdbcUsername("root")
											.setJdbcPassword("root")
											.setJdbcDriver("com.mysql.cj.jdbc.Driver")
//											确保在JDBC参数连接的数据库中，数据库表结构不存在时，会创建相应的表结构。
											.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		ProcessEngine processEngine = cfg.buildProcessEngine();
		
		RepositoryService repositoryService = processEngine.getRepositoryService();
		Deployment deployment = repositoryService.createDeployment()
								.addClasspathResource("holiday-request.bpmn20.xml")
								.deploy();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
												.deploymentId(deployment.getId())
												.singleResult();
		System.out.println("Found process definition:" + processDefinition.getName());
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("Who are you(你是谁)? ");
		String employee = scanner.nextLine();
		System.out.println("How many holidays do you want to request(请几天假)?");
		Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());
		System.out.println("Why do you need them(为什么请假)?");
		String descrpition = scanner.nextLine();
		
		RuntimeService runtimeService = processEngine.getRuntimeService();
		
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("employee", employee);
		variables.put("orOfHolidays", nrOfHolidays);
		variables.put("description", descrpition);
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holidayRequest",variables);
		
		TaskService taskService = processEngine.getTaskService();
		List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
		System.out.println("You have " + tasks.size() + " tasks:");
		for(int i = 0;i<tasks.size();i++) {
			System.out.println((i+1)+")"+tasks.get(i).getName());
		}
		System.out.println("Which task would you like to complete?");
		int taskIndex = Integer.valueOf(scanner.nextLine());
		Task task =tasks.get(taskIndex -1);
		Map<String, Object> processVariables = taskService.getVariables(task.getId());
		System.out.println(processVariables.get("employee")+"wants"+processVariables.get("nrOfHolidays")+ "of holidays. Do you approve this?");
		boolean approved = scanner.nextLine().toLowerCase().equals("y");
		variables = new HashMap<String, Object>();
		variables.put("approved", approved);
		taskService.complete(task.getId(),variables);
	}
}
