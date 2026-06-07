package com.petschool.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/video")
public class VideoController {

    private static final long CHUNK_SIZE = 1024 * 1024;

    private static String getUploadDir() {
        String userDir = System.getProperty("user.dir");
        return Paths.get(userDir, "uploads").toAbsolutePath().toString();
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("code", 400);
            result.put("message", "文件不能为空");
            return result;
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".mp4")) {
            result.put("code", 400);
            result.put("message", "仅支持MP4格式视频");
            return result;
        }

        try {
            File uploadDir = new File(getUploadDir());
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String filename = UUID.randomUUID().toString().replace("-", "") + ".mp4";
            File dest = new File(uploadDir, filename);
            file.transferTo(dest.getAbsoluteFile());

            String url = "/video/file/" + filename;
            result.put("code", 0);
            result.put("message", "ok");
            result.put("data", Map.of("url", url, "filename", filename, "originalName", originalName));
        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "上传失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        File uploadDir = new File(getUploadDir());
        if (!uploadDir.exists()) {
            result.put("code", 0);
            result.put("data", java.util.Collections.emptyList());
            return result;
        }
        File[] files = uploadDir.listFiles((dir, name) -> name.endsWith(".mp4"));
        java.util.List<Map<String, Object>> videoList = new java.util.ArrayList<>();
        if (files != null) {
            for (File f : files) {
                Map<String, Object> info = new HashMap<>();
                info.put("filename", f.getName());
                info.put("url", "/video/file/" + f.getName());
                info.put("size", f.length());
                videoList.add(info);
            }
        }
        result.put("code", 0);
        result.put("data", videoList);
        return result;
    }

    @GetMapping("/file/{filename}")
    public ResponseEntity<Resource> stream(@PathVariable String filename,
                                           @RequestHeader(value = "Range", required = false) String range) {
        File file = new File(getUploadDir(), filename);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        long fileSize = file.length();
        Resource resource = new FileSystemResource(file);

        if (range == null || range.isEmpty()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("video/mp4"))
                    .contentLength(fileSize)
                    .header("Accept-Ranges", "bytes")
                    .body(resource);
        }

        String[] ranges = range.replace("bytes=", "").split("-");
        long start = Long.parseLong(ranges[0]);
        long end = ranges.length > 1 && !ranges[1].isEmpty()
                ? Long.parseLong(ranges[1])
                : Math.min(start + CHUNK_SIZE - 1, fileSize - 1);

        if (start >= fileSize) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header("Content-Range", "bytes */" + fileSize)
                    .build();
        }

        long contentLength = end - start + 1;

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.valueOf("video/mp4"))
                .contentLength(contentLength)
                .header("Accept-Ranges", "bytes")
                .header("Content-Range", "bytes " + start + "-" + end + "/" + fileSize)
                .body(new org.springframework.core.io.InputStreamResource(
                        getFileInputStream(file, start, end)));
    }

    private java.io.InputStream getFileInputStream(File file, long start, long end) {
        try {
            java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "r");
            raf.seek(start);
            return new java.io.FilterInputStream(null) {
                long remaining = end - start + 1;
                boolean closed = false;

                @Override
                public int read() throws IOException {
                    if (remaining <= 0) return -1;
                    int b = raf.read();
                    if (b != -1) remaining--;
                    return b;
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    if (remaining <= 0) return -1;
                    int toRead = (int) Math.min(len, remaining);
                    int read = raf.read(b, off, toRead);
                    if (read != -1) remaining -= read;
                    return read;
                }

                @Override
                public void close() throws IOException {
                    if (!closed) {
                        closed = true;
                        raf.close();
                    }
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
