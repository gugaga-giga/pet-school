package com.petschool.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.petschool.entity.*;
import com.petschool.mapper.*;
import com.petschool.service.CertificateService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CertificateServiceImpl implements CertificateService {

    private final CertificateMapper certificateMapper;
    private final PetOrderMapper petOrderMapper;
    private final PetMapper petMapper;
    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final CoursePackageMapper coursePackageMapper;
    private final CertificateTemplateMapper certificateTemplateMapper;

    public CertificateServiceImpl(CertificateMapper certificateMapper,
                                  PetOrderMapper petOrderMapper,
                                  PetMapper petMapper,
                                  UserMapper userMapper,
                                  CourseMapper courseMapper,
                                  CoursePackageMapper coursePackageMapper,
                                  CertificateTemplateMapper certificateTemplateMapper) {
        this.certificateMapper = certificateMapper;
        this.petOrderMapper = petOrderMapper;
        this.petMapper = petMapper;
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
        this.coursePackageMapper = coursePackageMapper;
        this.certificateTemplateMapper = certificateTemplateMapper;
    }

    @Override
    public Certificate generateFromOrder(Long orderId) {
        PetOrder order = petOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 1) {
            throw new RuntimeException("订单未支付，无法生成证书");
        }

        Certificate existing = certificateMapper.selectByOrderId(orderId);
        if (existing != null) {
            throw new RuntimeException("证书已存在");
        }

        Pet pet = petMapper.selectById(order.getPetId());
        User user = userMapper.selectById(order.getUserId());
        CoursePackage pkg = coursePackageMapper.selectById(order.getPackageId());
        Course course = null;
        if (pkg != null) {
            course = courseMapper.selectById(pkg.getCourseId());
        }

        LocalDate graduateDate = null;
        if (order.getCreateTime() != null && order.getMonths() != null) {
            graduateDate = order.getCreateTime().plusMonths(order.getMonths()).toLocalDate();
        }

        String certificateNo = generateCertificateNo();

        Certificate cert = new Certificate();
        cert.setCertificateNo(certificateNo);
        cert.setUserId(order.getUserId());
        cert.setPetId(order.getPetId());
        cert.setOrderId(orderId);
        cert.setCourseId(course != null ? course.getId() : null);
        cert.setCourseName(order.getCourseName());
        cert.setPetName(order.getPetName());
        cert.setOwnerName(user != null ? user.getUsername() : "");
        cert.setGraduateDate(graduateDate);
        cert.setIssueDate(LocalDate.now());
        cert.setStatus(0);

        CertificateTemplate activeTemplate = certificateTemplateMapper.selectActive();
        if (activeTemplate != null) {
            cert.setTemplateId(activeTemplate.getId());
        }

        String certificateUrl = generateCertificateImage(cert);
        cert.setCertificateUrl(certificateUrl);

        certificateMapper.insert(cert);
        return cert;
    }

    @Override
    public Certificate generateTest(String petName, String ownerName, String courseName) {
        String certificateNo = generateCertificateNo();

        Certificate cert = new Certificate();
        cert.setCertificateNo(certificateNo);
        cert.setPetName(petName != null ? petName : "测试宠物");
        cert.setOwnerName(ownerName != null ? ownerName : "测试主人");
        cert.setCourseName(courseName != null ? courseName : "测试课程");
        cert.setGraduateDate(LocalDate.now());
        cert.setIssueDate(LocalDate.now());
        cert.setStatus(0);

        CertificateTemplate activeTemplate = certificateTemplateMapper.selectActive();
        if (activeTemplate != null) {
            cert.setTemplateId(activeTemplate.getId());
        }

        String certificateUrl = generateCertificateImage(cert);
        cert.setCertificateUrl(certificateUrl);

        certificateMapper.insert(cert);
        return cert;
    }

    @Override
    public Certificate regenerate(Long id) {
        Certificate cert = certificateMapper.selectById(id);
        if (cert == null) {
            throw new RuntimeException("证书不存在");
        }

        if (cert.getCertificateUrl() != null) {
            String oldFilePath = System.getProperty("user.dir") + cert.getCertificateUrl();
            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }

        String certificateUrl = generateCertificateImage(cert);
        cert.setCertificateUrl(certificateUrl);

        Certificate updateCert = new Certificate();
        updateCert.setId(cert.getId());
        updateCert.setCertificateUrl(certificateUrl);
        certificateMapper.update(updateCert);

        return certificateMapper.selectById(id);
    }

    @Override
    public Certificate getById(Long id) {
        return certificateMapper.selectById(id);
    }

    @Override
    public Certificate getByCertificateNo(String certificateNo) {
        return certificateMapper.selectByCertificateNo(certificateNo);
    }

    @Override
    public List<Certificate> getByUserId(Long userId) {
        return certificateMapper.selectByUserId(userId);
    }

    @Override
    public Map<String, Object> page(String keyword, Integer status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Certificate> list = certificateMapper.selectPage(keyword, status, offset, pageSize);
        int total = certificateMapper.selectPageCount(keyword, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (total + pageSize - 1) / pageSize);
        return result;
    }

    @Override
    public List<Certificate> listAll() {
        return certificateMapper.selectAll();
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        return certificateMapper.updateStatus(id, status);
    }

    @Override
    public int deleteById(Long id) {
        return certificateMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        return certificateMapper.deleteBatch(ids);
    }

    @Override
    public String getCertificateFilePath(Long id) {
        Certificate cert = certificateMapper.selectById(id);
        if (cert == null || cert.getCertificateUrl() == null) return null;
        return System.getProperty("user.dir") + cert.getCertificateUrl();
    }

    private String generateCertificateNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "CERT" + date + random;
    }

    private String generateCertificateImage(Certificate cert) {
        int width = 1200;
        int height = 850;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setColor(new Color(0xFFF8F0));
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(new Color(0xC5A55A));
        g2d.setStroke(new BasicStroke(3f));
        g2d.draw(new RoundRectangle2D.Double(10, 10, width - 20, height - 20, 20, 20));

        g2d.setStroke(new BasicStroke(1f));
        g2d.draw(new RoundRectangle2D.Double(25, 25, width - 50, height - 50, 15, 15));

        drawCornerFlourish(g2d, 30, 30, 1, 1);
        drawCornerFlourish(g2d, width - 30, 30, -1, 1);
        drawCornerFlourish(g2d, 30, height - 30, 1, -1);
        drawCornerFlourish(g2d, width - 30, height - 30, -1, -1);

        Font brandFont = new Font("Microsoft YaHei", Font.PLAIN, 16);
        g2d.setFont(brandFont);
        g2d.setColor(new Color(0xC5A55A));
        FontMetrics brandFm = g2d.getFontMetrics();
        String brandText = "PETSCHOOL";
        int brandX = (width - brandFm.stringWidth(brandText)) / 2;
        g2d.drawString(brandText, brandX, 80);

        Font titleFont = new Font("Serif", Font.BOLD, 42);
        g2d.setFont(titleFont);
        g2d.setColor(new Color(0x2C1810));
        FontMetrics titleFm = g2d.getFontMetrics();
        String titleText = "Graduation Certificate";
        int titleX = (width - titleFm.stringWidth(titleText)) / 2;
        g2d.drawString(titleText, titleX, 140);

        Font subtitleFont = new Font("Microsoft YaHei", Font.PLAIN, 22);
        g2d.setFont(subtitleFont);
        g2d.setColor(new Color(0x8B7355));
        FontMetrics subFm = g2d.getFontMetrics();
        String subtitleText = "宠物毕业证书";
        int subtitleX = (width - subFm.stringWidth(subtitleText)) / 2;
        g2d.drawString(subtitleText, subtitleX, 180);

        int bodyY = 260;
        int bodyLeftX = 120;
        Font bodyFont = new Font("Microsoft YaHei", Font.PLAIN, 20);
        Font bodyBoldFont = new Font("Microsoft YaHei", Font.BOLD, 20);
        Font petNameFont = new Font("Microsoft YaHei", Font.BOLD, 28);
        Font courseNameFont = new Font("Microsoft YaHei", Font.BOLD, 22);

        g2d.setFont(bodyFont);
        g2d.setColor(new Color(0x2C1810));
        g2d.drawString("兹证明", bodyLeftX, bodyY);

        int afterZiZheng = bodyLeftX + g2d.getFontMetrics().stringWidth("兹证明") + 10;
        g2d.setFont(petNameFont);
        g2d.setColor(new Color(0x2C1810));
        String petName = cert.getPetName() != null ? cert.getPetName() : "";
        g2d.drawString(petName, afterZiZheng, bodyY);

        int afterPetName = afterZiZheng + g2d.getFontMetrics().stringWidth(petName) + 10;
        g2d.setFont(bodyFont);
        g2d.drawString("已顺利完成", afterPetName, bodyY);

        int afterYiWanCheng = afterPetName + g2d.getFontMetrics().stringWidth("已顺利完成") + 10;
        g2d.setFont(courseNameFont);
        String courseName = cert.getCourseName() != null ? cert.getCourseName() : "";
        g2d.drawString(courseName, afterYiWanCheng, bodyY);

        bodyY += 50;
        g2d.setFont(bodyFont);
        g2d.setColor(new Color(0x2C1810));
        g2d.drawString("在训练期间表现优秀，特授予毕业认证。", bodyLeftX, bodyY);

        bodyY += 60;
        g2d.setFont(bodyBoldFont);
        g2d.setColor(new Color(0x2C1810));
        String ownerLine = "主人：" + (cert.getOwnerName() != null ? cert.getOwnerName() : "");
        g2d.drawString(ownerLine, bodyLeftX, bodyY);

        bodyY += 45;
        g2d.setFont(bodyFont);
        String dateLine = "毕业日期：" + (cert.getGraduateDate() != null ? cert.getGraduateDate().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")) : "");
        g2d.drawString(dateLine, bodyLeftX, bodyY);

        int signY = bodyY + 80;
        g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        g2d.setColor(new Color(0x8B7355));
        g2d.drawString("校长签名", bodyLeftX, signY);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(new Color(0xC5A55A));
        g2d.drawLine(bodyLeftX, signY + 10, bodyLeftX + 200, signY + 10);
        g2d.setFont(new Font("Serif", Font.ITALIC, 18));
        g2d.setColor(new Color(0x8B7355));
        g2d.drawString("PetSchool Principal", bodyLeftX + 30, signY + 35);

        try {
            String verifyUrl = "http://localhost:8080/certificate/verify/" + cert.getCertificateNo();
            BufferedImage qrImage = generateQRCode(verifyUrl, 120, 120);
            g2d.drawImage(qrImage, width - 200, signY - 60, null);
        } catch (Exception e) {
            System.err.println("生成二维码失败: " + e.getMessage());
        }

        Font certNoFont = new Font("Microsoft YaHei", Font.PLAIN, 14);
        g2d.setFont(certNoFont);
        g2d.setColor(Color.GRAY);
        String certNoText = "证书编号：" + (cert.getCertificateNo() != null ? cert.getCertificateNo() : "");
        FontMetrics certNoFm = g2d.getFontMetrics();
        int certNoX = (width - certNoFm.stringWidth(certNoText)) / 2;
        g2d.drawString(certNoText, certNoX, height - 40);

        g2d.dispose();

        String dirPath = System.getProperty("user.dir") + "/uploads/certificate/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filename = cert.getCertificateNo() + ".png";
        String filePath = dirPath + filename;
        try {
            ImageIO.write(image, "PNG", new File(filePath));
        } catch (IOException e) {
            throw new RuntimeException("证书图片保存失败: " + e.getMessage());
        }

        return "/uploads/certificate/" + filename;
    }

    private void drawCornerFlourish(Graphics2D g2d, int x, int y, int dx, int dy) {
        g2d.setColor(new Color(0xC5A55A));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(x, y, x + 40 * dx, y);
        g2d.drawLine(x, y, x, y + 40 * dy);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(x + 10 * dx, y + 10 * dy, x + 30 * dx, y + 10 * dy);
        g2d.drawLine(x + 10 * dx, y + 10 * dy, x + 10 * dx, y + 30 * dy);
    }

    private BufferedImage generateQRCode(String text, int width, int height) throws WriterException {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}
