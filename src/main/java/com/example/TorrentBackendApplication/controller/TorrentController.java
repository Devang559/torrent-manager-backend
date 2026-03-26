package com.example.TorrentBackendApplication.controller;
import com.example.TorrentBackendApplication.service.Aria2Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/torrents")
public class TorrentController {

    private final Aria2Service aria2Service;

    public TorrentController(Aria2Service aria2Service) {
        this.aria2Service = aria2Service;
    }

    @PostMapping("/add")
    public Object add(@RequestBody Map<String, String> request) {
        return aria2Service.call("addUri", List.of(List.of(request.get("url"))));
    }

    @GetMapping("/status/{gid}")
    public Object status(@PathVariable String gid) {
        return aria2Service.call("tellStatus", new ArrayList<>(List.of(gid)));
    }

    @GetMapping("/active")
    public Object listActive() {
        return aria2Service.call("tellActive", new ArrayList<>());
    }
    // Add these to TorrentController.java

    @PostMapping("/pause/{gid}")
    public ResponseEntity<?> pause(@PathVariable String gid) {
        String result = aria2Service.pause(gid);

        if (result == null) {
            return ResponseEntity.status(500).body(Map.of("error", "GID not found or RPC failure"));
        }

        if (result.startsWith("ALREADY_")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Download is " + result.replace("ALREADY_", "")
                            .toLowerCase() + " and cannot be paused."
            ));
        }

        return ResponseEntity.ok(Map.of("success", true, "gid", result));
    }

    @PostMapping("/resume/{gid}")
    public Map<String, String> resume(@PathVariable String gid) {
        String result = aria2Service.resume(gid);
        return Map.of("gid", result != null ? result : "error", "action", "resumed");
    }

    @DeleteMapping("/remove/{gid}")
    public ResponseEntity<?> remove(@PathVariable String gid) {
        String result = aria2Service.fullDelete(gid);
        if ("GID_NOT_FOUND".equals(result)) {
            return ResponseEntity.status(404).body(Map.of("error", "Torrent not found"));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Files wiped, ready to restart from 0"));
    }
}