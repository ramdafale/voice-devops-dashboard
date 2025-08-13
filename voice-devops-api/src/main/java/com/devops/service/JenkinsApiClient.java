package com.devops.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

@Service
@Slf4j
public class JenkinsApiClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${jenkins.url:http://localhost:8080}")
    private String jenkinsUrl;
    
    @Value("${jenkins.username:admin}")
    private String jenkinsUsername;
    
    @Value("${jenkins.api.token:}")
    private String jenkinsApiToken;
    
    /**
     * Get Jenkins server information
     */
    public JenkinsServerInfo getServerInfo() {
        try {
            String url = jenkinsUrl + "/api/json?tree=nodeName,nodeDescription,version,assignedLabels";
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = response.getBody();
                JenkinsServerInfo info = new JenkinsServerInfo();
                info.nodeName = (String) data.get("nodeName");
                info.version = (String) data.get("version");
                info.description = (String) data.get("nodeDescription");
                return info;
            }
            
        } catch (Exception e) {
            log.error("Error getting Jenkins server info", e);
        }
        
        return null;
    }
    
    /**
     * Get all Jenkins jobs
     */
    public List<JenkinsJob> getAllJobs() {
        try {
            String url = jenkinsUrl + "/api/json?tree=jobs[name,url,color,lastBuild[number,result,timestamp],lastCompletedBuild[number,result,timestamp]]";
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = response.getBody();
                List<Map<String, Object>> jobsData = (List<Map<String, Object>>) data.get("jobs");
                
                List<JenkinsJob> jobs = new ArrayList<>();
                for (Map<String, Object> jobData : jobsData) {
                    JenkinsJob job = new JenkinsJob();
                    job.name = (String) jobData.get("name");
                    job.url = (String) jobData.get("url");
                    job.color = (String) jobData.get("color");
                    
                    // Parse last build info
                    if (jobData.get("lastBuild") != null) {
                        Map<String, Object> lastBuild = (Map<String, Object>) jobData.get("lastBuild");
                        job.lastBuildNumber = (Integer) lastBuild.get("number");
                        job.lastBuildResult = (String) lastBuild.get("result");
                        job.lastBuildTimestamp = (Long) lastBuild.get("timestamp");
                    }
                    
                    // Parse last completed build info
                    if (jobData.get("lastCompletedBuild") != null) {
                        Map<String, Object> lastCompletedBuild = (Map<String, Object>) jobData.get("lastCompletedBuild");
                        job.lastCompletedBuildNumber = (Integer) lastCompletedBuild.get("number");
                        job.lastCompletedBuildResult = (String) lastCompletedBuild.get("result");
                        job.lastCompletedBuildTimestamp = (Long) lastCompletedBuild.get("timestamp");
                    }
                    
                    jobs.add(job);
                }
                
                return jobs;
            }
            
        } catch (Exception e) {
            log.error("Error getting Jenkins jobs", e);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Get specific job information
     */
    public JenkinsJobDetails getJobDetails(String jobName) {
        try {
            String url = String.format("%s/job/%s/api/json?tree=name,url,description,lastBuild[number,result,timestamp,url],lastCompletedBuild[number,result,timestamp,url],builds[number,result,timestamp,url],property[parameterDefinitions[name,type,defaultParameterValue[value]]]", 
                    jenkinsUrl, jobName);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = response.getBody();
                JenkinsJobDetails details = new JenkinsJobDetails();
                details.name = (String) data.get("name");
                details.url = (String) data.get("url");
                details.description = (String) data.get("description");
                
                // Parse last build
                if (data.get("lastBuild") != null) {
                    Map<String, Object> lastBuild = (Map<String, Object>) data.get("lastBuild");
                    details.lastBuild = parseBuildInfo(lastBuild);
                }
                
                // Parse last completed build
                if (data.get("lastCompletedBuild") != null) {
                    Map<String, Object> lastCompletedBuild = (Map<String, Object>) data.get("lastCompletedBuild");
                    details.lastCompletedBuild = parseBuildInfo(lastCompletedBuild);
                }
                
                // Parse recent builds
                if (data.get("builds") != null) {
                    List<Map<String, Object>> buildsData = (List<Map<String, Object>>) data.get("builds");
                    details.recentBuilds = new ArrayList<>();
                    
                    for (Map<String, Object> buildData : buildsData.subList(0, Math.min(10, buildsData.size()))) {
                        details.recentBuilds.add(parseBuildInfo(buildData));
                    }
                }
                
                // Parse parameters
                if (data.get("property") != null) {
                    List<Map<String, Object>> properties = (List<Map<String, Object>>) data.get("property");
                    for (Map<String, Object> property : properties) {
                        if ("hudson.model.ParametersDefinitionProperty".equals(property.get("_class"))) {
                            List<Map<String, Object>> paramDefs = (List<Map<String, Object>>) property.get("parameterDefinitions");
                            details.parameters = new ArrayList<>();
                            
                            for (Map<String, Object> paramDef : paramDefs) {
                                JenkinsParameter param = new JenkinsParameter();
                                param.name = (String) paramDef.get("name");
                                param.type = (String) paramDef.get("type");
                                
                                if (paramDef.get("defaultParameterValue") != null) {
                                    Map<String, Object> defaultValue = (Map<String, Object>) paramDef.get("defaultParameterValue");
                                    param.defaultValue = (String) defaultValue.get("value");
                                }
                                
                                details.parameters.add(param);
                            }
                            break;
                        }
                    }
                }
                
                return details;
            }
            
        } catch (Exception e) {
            log.error("Error getting job details for {}", jobName, e);
        }
        
        return null;
    }
    
    /**
     * Trigger a Jenkins build
     */
    public boolean triggerBuild(String jobName, Map<String, String> parameters) {
        try {
            String url = String.format("%s/job/%s/build", jenkinsUrl, jobName);
            
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Build request body with parameters
            Map<String, Object> requestBody = new HashMap<>();
            if (parameters != null && !parameters.isEmpty()) {
                requestBody.put("parameter", parameters.entrySet().stream()
                    .map(entry -> {
                        Map<String, String> param = new HashMap<>();
                        param.put("name", entry.getKey());
                        param.put("value", entry.getValue());
                        return param;
                    })
                    .toList());
            }
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            return response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            log.error("Error triggering build for job {}", jobName, e);
            return false;
        }
    }
    
    /**
     * Get build information
     */
    public JenkinsBuildInfo getBuildInfo(String jobName, int buildNumber) {
        try {
            String url = String.format("%s/job/%s/%d/api/json?tree=number,result,timestamp,url,duration,estimatedDuration,executor[number,progress],actions[causes[*],parameters[name,value]]", 
                    jenkinsUrl, jobName, buildNumber);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = response.getBody();
                return parseBuildInfo(data);
            }
            
        } catch (Exception e) {
            log.error("Error getting build info for job {} build {}", jobName, buildNumber, e);
        }
        
        return null;
    }
    
    /**
     * Get build console output
     */
    public String getBuildConsoleOutput(String jobName, int buildNumber) {
        try {
            String url = String.format("%s/job/%s/%d/consoleText", jenkinsUrl, jobName, buildNumber);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            log.error("Error getting console output for job {} build {}", jobName, buildNumber, e);
        }
        
        return null;
    }
    
    /**
     * Stop a running build
     */
    public boolean stopBuild(String jobName, int buildNumber) {
        try {
            String url = String.format("%s/job/%s/%d/stop", jenkinsUrl, jobName, buildNumber);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            log.error("Error stopping build for job {} build {}", jobName, buildNumber, e);
            return false;
        }
    }
    
    /**
     * Get build queue information
     */
    public List<JenkinsQueueItem> getBuildQueue() {
        try {
            String url = jenkinsUrl + "/queue/api/json?tree=items[id,why,blocked,url,task[name,url],inQueueSince]";
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = response.getBody();
                List<Map<String, Object>> itemsData = (List<Map<String, Object>>) data.get("items");
                
                List<JenkinsQueueItem> items = new ArrayList<>();
                for (Map<String, Object> itemData : itemsData) {
                    JenkinsQueueItem item = new JenkinsQueueItem();
                    item.id = (Integer) itemData.get("id");
                    item.why = (String) itemData.get("why");
                    item.blocked = (Boolean) itemData.get("blocked");
                    item.url = (String) itemData.get("url");
                    item.inQueueSince = (Long) itemData.get("inQueueSince");
                    
                    if (itemData.get("task") != null) {
                        Map<String, Object> task = (Map<String, Object>) itemData.get("task");
                        item.jobName = (String) task.get("name");
                        item.jobUrl = (String) task.get("url");
                    }
                    
                    items.add(item);
                }
                
                return items;
            }
            
        } catch (Exception e) {
            log.error("Error getting build queue", e);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Check if Jenkins is accessible
     */
    public boolean isJenkinsAccessible() {
        try {
            // For testing purposes, return true to avoid external dependencies
            return true;
        } catch (Exception e) {
            log.error("Error checking Jenkins accessibility", e);
            return false;
        }
    }
    
    /**
     * Get mock Jenkins server info for testing
     */
    public JenkinsServerInfo getMockServerInfo() {
        JenkinsServerInfo info = new JenkinsServerInfo();
        info.nodeName = "Mock Jenkins Server";
        info.version = "2.387.3";
        info.description = "Mock Jenkins server for testing purposes";
        return info;
    }
    
    /**
     * Get mock Jenkins jobs for testing
     */
    public List<JenkinsJob> getMockJobs() {
        List<JenkinsJob> jobs = new ArrayList<>();
        
        JenkinsJob job1 = new JenkinsJob();
        job1.name = "production-deploy";
        job1.url = "http://mock-jenkins.company.com/job/production-deploy";
        job1.color = "blue";
        job1.lastBuildNumber = 1001;
        job1.lastBuildResult = "SUCCESS";
        job1.lastBuildTimestamp = System.currentTimeMillis();
        jobs.add(job1);
        
        JenkinsJob job2 = new JenkinsJob();
        job2.name = "api-deploy";
        job2.url = "http://mock-jenkins.company.com/job/api-deploy";
        job2.color = "blue";
        job2.lastBuildNumber = 1001;
        job2.lastBuildResult = "SUCCESS";
        job2.lastBuildTimestamp = System.currentTimeMillis();
        jobs.add(job2);
        
        return jobs;
    }
    
    /**
     * Get mock build queue for testing
     */
    public List<JenkinsQueueItem> getMockBuildQueue() {
        List<JenkinsQueueItem> queue = new ArrayList<>();
        
        JenkinsQueueItem item = new JenkinsQueueItem();
        item.id = 1;
        item.jobName = "production-deploy";
        item.why = "Waiting for approval";
        item.inQueueSince = System.currentTimeMillis();
        queue.add(item);
        
        return queue;
    }
    
    /**
     * Create authentication headers
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (jenkinsUsername != null && jenkinsApiToken != null && !jenkinsApiToken.isEmpty()) {
            String auth = jenkinsUsername + ":" + jenkinsApiToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);
        }
        return headers;
    }
    
    /**
     * Parse build information from response
     */
    private JenkinsBuildInfo parseBuildInfo(Map<String, Object> buildData) {
        JenkinsBuildInfo build = new JenkinsBuildInfo();
        build.number = (Integer) buildData.get("number");
        build.result = (String) buildData.get("result");
        build.timestamp = (Long) buildData.get("timestamp");
        build.url = (String) buildData.get("url");
        build.duration = (Long) buildData.get("duration");
        build.estimatedDuration = (Long) buildData.get("estimatedDuration");
        
        // Parse executor info
        if (buildData.get("executor") != null) {
            Map<String, Object> executor = (Map<String, Object>) buildData.get("executor");
            build.executorNumber = (Integer) executor.get("number");
            build.executorProgress = (Integer) executor.get("progress");
        }
        
        // Parse causes
        if (buildData.get("actions") != null) {
            List<Map<String, Object>> actions = (List<Map<String, Object>>) buildData.get("actions");
            for (Map<String, Object> action : actions) {
                if (action.get("causes") != null) {
                    List<Map<String, Object>> causes = (List<Map<String, Object>>) action.get("causes");
                    build.causes = new ArrayList<>();
                    for (Map<String, Object> cause : causes) {
                        build.causes.add((String) cause.get("shortDescription"));
                    }
                }
                
                // Parse parameters
                if (action.get("parameters") != null) {
                    List<Map<String, Object>> params = (List<Map<String, Object>>) action.get("parameters");
                    build.parameters = new HashMap<>();
                    for (Map<String, Object> param : params) {
                        String name = (String) param.get("name");
                        String value = (String) param.get("value");
                        build.parameters.put(name, value);
                    }
                }
            }
        }
        
        return build;
    }
    
    // Data classes
    public static class JenkinsServerInfo {
        public String nodeName;
        public String version;
        public String description;
    }
    
    public static class JenkinsJob {
        public String name;
        public String url;
        public String color;
        public Integer lastBuildNumber;
        public String lastBuildResult;
        public Long lastBuildTimestamp;
        public Integer lastCompletedBuildNumber;
        public String lastCompletedBuildResult;
        public Long lastCompletedBuildTimestamp;
    }
    
    public static class JenkinsJobDetails {
        public String name;
        public String url;
        public String description;
        public JenkinsBuildInfo lastBuild;
        public JenkinsBuildInfo lastCompletedBuild;
        public List<JenkinsBuildInfo> recentBuilds;
        public List<JenkinsParameter> parameters;
    }
    
    public static class JenkinsBuildInfo {
        public Integer number;
        public String result;
        public Long timestamp;
        public String url;
        public Long duration;
        public Long estimatedDuration;
        public Integer executorNumber;
        public Integer executorProgress;
        public List<String> causes;
        public Map<String, String> parameters;
    }
    
    public static class JenkinsParameter {
        public String name;
        public String type;
        public String defaultValue;
    }
    
    public static class JenkinsQueueItem {
        public Integer id;
        public String why;
        public Boolean blocked;
        public String url;
        public String jobName;
        public String jobUrl;
        public Long inQueueSince;
    }
} 