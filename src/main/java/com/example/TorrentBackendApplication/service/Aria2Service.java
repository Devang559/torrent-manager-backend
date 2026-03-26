package com.example.TorrentBackendApplication.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
public class Aria2Service {

    private final WebClient webClient;

    @Value("${aria2.url:http://localhost:6800/jsonrpc}")
    private String url;

    @Value("${aria2.secret:xxxxxxxxx}")
    private String secret;

    // The @Qualifier must match the method name in your WebClientConfig
    public Aria2Service(@Qualifier("aria2WebClient") WebClient webClient) {
        this.webClient = webClient;
    }


    // Add these to Aria2Service.java

    public String pause(String gid) {
        Map<?, ?> status = (Map<?, ?>) call("tellStatus", List.of(gid)).get("result");
        String state = (String) status.get("status");

        // If it's complete, check if it spawned the real download
        if ("complete".equals(state)) {
            List<String> followedBy = (List<String>) status.get("followedBy");
            if (followedBy != null && !followedBy.isEmpty()) {
                // Recurse using the NEW GID
                return pause(followedBy.get(0));
            }
            return "TASK_ALREADY_FINISHED";
        }

        // Standard pause for active tasks
        Map<?, ?> response = call("pause", List.of(gid));
        return response != null ? response.get("result").toString() : null;
    }

    public String resume(String gid) {
        Map<?, ?> response = call("unpause", List.of(gid));
        if (response != null && response.get("result") != null) {
            return response.get("result").toString();
        }
        return null;
    }



// Inside Aria2Service.java

    public String fullDelete(String gid) {
        // 1. Get the file path before removing the task
        Map<?, ?> statusResponse = (Map<?, ?>) call("tellStatus", List.of(gid)).get("result");
        if (statusResponse == null) return "GID_NOT_FOUND";

        String dir = (String) statusResponse.get("dir");
        List<Map<String, Object>> files = (List<Map<String, Object>>) statusResponse.get("files");

        // 2. Remove the task from Aria2 (Active or Result list)
        String state = (String) statusResponse.get("status");
        if ("active".equals(state) || "waiting".equals(state) || "paused".equals(state)) {
            call("forceRemove", List.of(gid));
        } else {
            call("removeDownloadResult", List.of(gid));
        }

        // 3. Physically delete the files and the .aria2 control file
        if (files != null && !files.isEmpty()) {
            for (Map<String, Object> file : files) {
                String relativePath = (String) file.get("path");
                try {
                    Path filePath = Paths.get(relativePath);
                    Path aria2FilePath = Paths.get(relativePath + ".aria2");

                    // Delete the actual data
                    Files.deleteIfExists(filePath);
                    // Delete the Aria2 control file (This is what prevents starting from 0)
                    Files.deleteIfExists(aria2FilePath);

                    System.out.println("Deleted: " + relativePath);
                } catch (Exception e) {
                    System.err.println("Could not delete physical file: " + e.getMessage());
                }
            }
        }
        return "DELETED_COMPLETELY";
    }


    public Map<?, ?> call(String method, List<Object> params) {
        // 1. MUST create a NEW ArrayList because List.of(...) is immutable
        List<Object> mutableParams = new java.util.ArrayList<>();

        // 2. Add the token first
        mutableParams.add("token:" + secret);

        // 3. Add all other parameters
        if (params != null) {
            mutableParams.addAll(params);
        }

        Map<String, Object> body = Map.of(
                "jsonrpc", "2.0",
                "id", "spring-boot",
                "method", "aria2." + method,
                "params", mutableParams
        );

        try {
            return webClient.post()
                    .uri(url)
                    .bodyValue(body)
                    .retrieve()
                    // Log the error if Aria2 returns 4xx or 5xx
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(new RuntimeException("Aria2 Error: " + errorBody)))
                    )
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            System.err.println("Failed to call Aria2 method [" + method + "]: " + e.getMessage());
            return null;
        }
    }
}