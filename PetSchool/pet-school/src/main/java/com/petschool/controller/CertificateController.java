package com.petschool.controller;

import com.petschool.entity.Certificate;
import com.petschool.service.CertificateService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/certificate")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    private Map<String, Object> ok(Object data) { Map<String, Object> r = new HashMap<>(); r.put("code", 200); r.put("data", data); return r; }
    private Map<String, Object> ok(String msg, Object data) { Map<String, Object> r = new HashMap<>(); r.put("code", 200); r.put("message", msg); r.put("data", data); return r; }
    private Map<String, Object> fail(String msg) { Map<String, Object> r = new HashMap<>(); r.put("code", 500); r.put("message", msg); return r; }

    @GetMapping("/my")
    public Map<String, Object> myCertificates(@RequestParam Long userId) {
        return ok(certificateService.getByUserId(userId));
    }

    @GetMapping("/page")
    public Map<String, Object> page(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ok(certificateService.page(keyword, status, page, pageSize));
    }

    @PostMapping("/generate/{orderId}")
    public Map<String, Object> generate(@PathVariable Long orderId) {
        try {
            Certificate cert = certificateService.generateFromOrder(orderId);
            return ok("证书生成成功", cert);
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @PostMapping("/generate-test")
    public Map<String, Object> generateTest(@RequestBody Map<String, String> params) {
        try {
            String petName = params.get("petName");
            String ownerName = params.get("ownerName");
            String courseName = params.get("courseName");
            Certificate cert = certificateService.generateTest(petName, ownerName, courseName);
            return ok("测试证书生成成功", cert);
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @PostMapping("/regenerate/{id}")
    public Map<String, Object> regenerate(@PathVariable Long id) {
        try {
            Certificate cert = certificateService.regenerate(id);
            return ok("证书重新生成成功", cert);
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        Certificate cert = certificateService.getById(id);
        return cert != null ? ok(cert) : fail("证书不存在");
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        int rows = certificateService.deleteById(id);
        return rows > 0 ? ok("删除成功", null) : fail("删除失败");
    }

    @DeleteMapping("/batch")
    public Map<String, Object> batchDelete(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> ids = ((List<Number>) params.get("ids")).stream().map(Number::longValue).toList();
        int rows = certificateService.deleteBatch(ids);
        return rows > 0 ? ok("批量删除成功，共删除" + rows + "条", null) : fail("批量删除失败");
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Certificate cert = certificateService.getById(id);
        if (cert == null || cert.getCertificateUrl() == null) return ResponseEntity.notFound().build();
        String filePath = System.getProperty("user.dir") + cert.getCertificateUrl();
        File file = new File(filePath);
        if (!file.exists()) return ResponseEntity.notFound().build();
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cert.getCertificateNo() + ".png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }

    @GetMapping("/verify/{certificateNo}")
    public Map<String, Object> verify(@PathVariable String certificateNo) {
        Certificate cert = certificateService.getByCertificateNo(certificateNo);
        if (cert == null) return fail("证书不存在");
        if (cert.getStatus() != null && cert.getStatus() == 1) return fail("证书已作废");
        return ok(cert);
    }

    @PutMapping("/status")
    public Map<String, Object> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        int rows = certificateService.updateStatus(id, status);
        return rows > 0 ? ok("状态更新成功", null) : fail("状态更新失败");
    }
}
