package com.petschool.controller;

import com.petschool.entity.*;
import com.petschool.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final CourseService courseService;
    private final CourseCategoryService courseCategoryService;
    private final CoursePackageService coursePackageService;
    private final PackageItemService packageItemService;
    private final RoomTypeService roomTypeService;
    private final RoomService roomService;
    private final CouponService couponService;
    private final CertificateService certificateService;
    private final HealthRecordService healthRecordService;
    private final AiService aiService;
    private final VaccineRecordService vaccineRecordService;
    private final DewormingRecordService dewormingRecordService;
    private final SurgeryRecordService surgeryRecordService;
    private final TrainingRecordService trainingRecordService;
    private final TrainingVideoService trainingVideoService;
    private final LiveSessionService liveSessionService;
    private final MessageService messageService;
    private final UserService userService;
    private final PetService petService;
    private final PetOrderService petOrderService;
    private final BoardingOrderService boardingOrderService;

    public AdminController(CourseService courseService, CourseCategoryService courseCategoryService,
                           CoursePackageService coursePackageService, PackageItemService packageItemService,
                           RoomTypeService roomTypeService, RoomService roomService,
                           CouponService couponService, CertificateService certificateService,
                           HealthRecordService healthRecordService, AiService aiService,
                           VaccineRecordService vaccineRecordService, DewormingRecordService dewormingRecordService,
                           SurgeryRecordService surgeryRecordService, TrainingRecordService trainingRecordService,
                           TrainingVideoService trainingVideoService, LiveSessionService liveSessionService,
                           MessageService messageService, UserService userService, PetService petService,
                           PetOrderService petOrderService, BoardingOrderService boardingOrderService) {
        this.courseService = courseService;
        this.courseCategoryService = courseCategoryService;
        this.coursePackageService = coursePackageService;
        this.packageItemService = packageItemService;
        this.roomTypeService = roomTypeService;
        this.roomService = roomService;
        this.couponService = couponService;
        this.certificateService = certificateService;
        this.healthRecordService = healthRecordService;
        this.aiService = aiService;
        this.vaccineRecordService = vaccineRecordService;
        this.dewormingRecordService = dewormingRecordService;
        this.surgeryRecordService = surgeryRecordService;
        this.trainingRecordService = trainingRecordService;
        this.trainingVideoService = trainingVideoService;
        this.liveSessionService = liveSessionService;
        this.messageService = messageService;
        this.userService = userService;
        this.petService = petService;
        this.petOrderService = petOrderService;
        this.boardingOrderService = boardingOrderService;
    }

    private Map<String, Object> ok(Object data) { Map<String, Object> r = new HashMap<>(); r.put("code", 200); r.put("data", data); return r; }
    private Map<String, Object> ok(String msg, Object data) { Map<String, Object> r = new HashMap<>(); r.put("code", 200); r.put("message", msg); r.put("data", data); return r; }
    private Map<String, Object> fail(String msg) { Map<String, Object> r = new HashMap<>(); r.put("code", 500); r.put("message", msg); return r; }

    @GetMapping("/user/list")
    public Map<String, Object> userList() { return ok(userService.listAll()); }

    @GetMapping("/pet/list")
    public Map<String, Object> petListByUser(@RequestParam Long userId) { return ok(petService.getByUserId(userId)); }

    @GetMapping("/course/list")
    public Map<String, Object> courseList() { return ok(courseService.listAll()); }

    @GetMapping("/course/{id}")
    public Map<String, Object> courseDetail(@PathVariable Long id) {
        Course c = courseService.getById(id);
        return c != null ? ok(c) : fail("课程不存在");
    }

    @PostMapping("/course/add")
    public Map<String, Object> addCourse(@RequestBody Course course) {
        int rows = courseService.add(course);
        return rows > 0 ? ok("添加成功", course) : fail("添加失败");
    }

    @PutMapping("/course/update")
    public Map<String, Object> updateCourse(@RequestBody Course course) {
        int rows = courseService.update(course);
        return rows > 0 ? ok("修改成功", course) : fail("修改失败");
    }

    @DeleteMapping("/course/delete/{id}")
    public Map<String, Object> deleteCourse(@PathVariable Long id) {
        int rows = courseService.deleteById(id);
        return rows > 0 ? ok("删除成功", null) : fail("删除失败");
    }

    @GetMapping("/course/category/list")
    public Map<String, Object> categoryList() { return ok(courseCategoryService.listAll()); }

    @PostMapping("/course/category/add")
    public Map<String, Object> addCategory(@RequestBody CourseCategory category) {
        int rows = courseCategoryService.add(category);
        return rows > 0 ? ok("添加成功", category) : fail("添加失败");
    }

    @PutMapping("/course/category/update")
    public Map<String, Object> updateCategory(@RequestBody CourseCategory category) {
        int rows = courseCategoryService.update(category);
        return rows > 0 ? ok("修改成功", category) : fail("修改失败");
    }

    @DeleteMapping("/course/category/delete/{id}")
    public Map<String, Object> deleteCategory(@PathVariable Long id) {
        int rows = courseCategoryService.deleteById(id);
        return rows > 0 ? ok("删除成功", null) : fail("删除失败");
    }

    @GetMapping("/course/package/list")
    public Map<String, Object> packageList(@RequestParam Long courseId) { return ok(coursePackageService.getByCourseId(courseId)); }

    @GetMapping("/course/package/item/list")
    public Map<String, Object> packageItems(@RequestParam Long packageId) { return ok(packageItemService.getByPackageId(packageId)); }

    @PostMapping("/training/record/add")
    public Map<String, Object> addTrainingRecord(@RequestBody TrainingRecord record) {
        int rows = trainingRecordService.add(record);
        return rows > 0 ? ok("添加成功", record) : fail("添加失败");
    }

    @PostMapping("/training/video/add")
    public Map<String, Object> addTrainingVideo(@RequestBody TrainingVideo video) {
        int rows = trainingVideoService.add(video);
        return rows > 0 ? ok("上传成功", video) : fail("上传失败");
    }

    @GetMapping("/training/record/pet/{petId}")
    public Map<String, Object> trainingByPet(@PathVariable Long petId) { return ok(trainingRecordService.getByPetId(petId)); }

    @GetMapping("/certificate/page")
    public Map<String, Object> certificatePage(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ok(certificateService.page(keyword, status, page, pageSize));
    }

    @GetMapping("/certificate/list")
    public Map<String, Object> certificateList() { return ok(certificateService.listAll()); }

    @GetMapping("/certificate/{id}")
    public Map<String, Object> certificateDetail(@PathVariable Long id) {
        Certificate c = certificateService.getById(id);
        return c != null ? ok(c) : fail("证书不存在");
    }

    @PutMapping("/certificate/status")
    public Map<String, Object> certificateUpdateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        int rows = certificateService.updateStatus(id, status);
        return rows > 0 ? ok("状态更新成功", null) : fail("状态更新失败");
    }

    @DeleteMapping("/certificate/delete/{id}")
    public Map<String, Object> certificateDelete(@PathVariable Long id) {
        int rows = certificateService.deleteById(id);
        return rows > 0 ? ok("删除成功", null) : fail("删除失败");
    }

    @DeleteMapping("/certificate/batch")
    public Map<String, Object> certificateBatchDelete(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> ids = ((List<Number>) params.get("ids")).stream().map(Number::longValue).toList();
        int rows = certificateService.deleteBatch(ids);
        return rows > 0 ? ok("批量删除成功，共删除" + rows + "条", null) : fail("批量删除失败");
    }

    @GetMapping("/room/type/list")
    public Map<String, Object> roomTypeList() { return ok(roomTypeService.listAll()); }

    @GetMapping("/room/type/{id}")
    public Map<String, Object> roomTypeDetail(@PathVariable Long id) {
        RoomType rt = roomTypeService.getById(id);
        return rt != null ? ok(rt) : fail("房型不存在");
    }

    @PostMapping("/room/type/add")
    public Map<String, Object> addRoomType(@RequestBody RoomType roomType) {
        int rows = roomTypeService.add(roomType);
        return rows > 0 ? ok("添加成功", roomType) : fail("添加失败");
    }

    @PutMapping("/room/type/update")
    public Map<String, Object> updateRoomType(@RequestBody RoomType roomType) {
        int rows = roomTypeService.update(roomType);
        return rows > 0 ? ok("修改成功", roomType) : fail("修改失败");
    }

    @DeleteMapping("/room/type/delete/{id}")
    public Map<String, Object> deleteRoomType(@PathVariable Long id) {
        int rows = roomTypeService.deleteById(id);
        return rows > 0 ? ok("删除成功", null) : fail("删除失败");
    }

    @GetMapping("/room/list")
    public Map<String, Object> roomList(@RequestParam Long typeId) { return ok(roomService.getByTypeId(typeId)); }

    @PostMapping("/room/add")
    public Map<String, Object> addRoom(@RequestBody Room room) {
        int rows = roomService.add(room);
        return rows > 0 ? ok("添加成功", room) : fail("添加失败");
    }

    @PutMapping("/room/update")
    public Map<String, Object> updateRoom(@RequestBody Room room) {
        int rows = roomService.update(room);
        return rows > 0 ? ok("修改成功", room) : fail("修改失败");
    }

    @DeleteMapping("/room/delete/{id}")
    public Map<String, Object> deleteRoom(@PathVariable Long id) {
        int rows = roomService.deleteById(id);
        return rows > 0 ? ok("删除成功", null) : fail("删除失败");
    }

    @GetMapping("/coupon/list")
    public Map<String, Object> couponList() { return ok(couponService.listAll()); }

    @PostMapping("/coupon/create")
    public Map<String, Object> createCoupon(@RequestBody Coupon coupon) {
        int rows = couponService.create(coupon);
        return rows > 0 ? ok("创建成功", coupon) : fail("创建失败");
    }

    @GetMapping("/health/record/pet/{petId}")
    public Map<String, Object> healthByPet(@PathVariable Long petId) { return ok(healthRecordService.getByPetId(petId)); }

    @PostMapping("/health/record/add")
    public Map<String, Object> addHealthRecord(@RequestBody HealthRecord record) {
        int rows = healthRecordService.add(record);
        return rows > 0 ? ok("添加成功", record) : fail("添加失败");
    }

    @GetMapping("/health/ai/warning/{petId}")
    public Map<String, Object> aiWarning(@PathVariable Long petId) { return ok(aiService.healthWarning(petId)); }

    @GetMapping("/health/ai/vaccine/{petId}")
    public Map<String, Object> aiVaccine(@PathVariable Long petId) { return ok(aiService.vaccineReminder(petId)); }

    @PostMapping("/medical/vaccine/add")
    public Map<String, Object> addVaccine(@RequestBody VaccineRecord record) {
        int rows = vaccineRecordService.add(record);
        return rows > 0 ? ok("添加成功", record) : fail("添加失败");
    }

    @PostMapping("/medical/deworming/add")
    public Map<String, Object> addDeworming(@RequestBody DewormingRecord record) {
        int rows = dewormingRecordService.add(record);
        return rows > 0 ? ok("添加成功", record) : fail("添加失败");
    }

    @PostMapping("/medical/surgery/add")
    public Map<String, Object> addSurgery(@RequestBody SurgeryRecord record) {
        int rows = surgeryRecordService.add(record);
        return rows > 0 ? ok("添加成功", record) : fail("添加失败");
    }

    @GetMapping("/medical/vaccine/pet/{petId}")
    public Map<String, Object> vaccineByPet(@PathVariable Long petId) { return ok(vaccineRecordService.getByPetId(petId)); }

    @GetMapping("/medical/deworming/pet/{petId}")
    public Map<String, Object> dewormingByPet(@PathVariable Long petId) { return ok(dewormingRecordService.getByPetId(petId)); }

    @GetMapping("/medical/surgery/pet/{petId}")
    public Map<String, Object> surgeryByPet(@PathVariable Long petId) { return ok(surgeryRecordService.getByPetId(petId)); }

    @PostMapping("/live/create")
    public Map<String, Object> createLive(@RequestBody LiveSession session) {
        int rows = liveSessionService.create(session);
        return rows > 0 ? ok("创建成功", session) : fail("创建失败");
    }

    @PutMapping("/live/start/{id}")
    public Map<String, Object> startLive(@PathVariable Long id) { liveSessionService.startLive(id); return ok("直播已开始", null); }

    @PutMapping("/live/end/{id}")
    public Map<String, Object> endLive(@PathVariable Long id) { liveSessionService.endLive(id); return ok("直播已结束", null); }

    @GetMapping("/live/list")
    public Map<String, Object> liveList() { return ok(liveSessionService.listAll()); }

    @PostMapping("/message/send")
    public Map<String, Object> sendMessage(@RequestBody Message message) {
        int rows = messageService.send(message);
        return rows > 0 ? ok("发送成功", null) : fail("发送失败");
    }

    @GetMapping("/message/user/{userId}")
    public Map<String, Object> messageByUser(@PathVariable Long userId) { return ok(messageService.getByUserId(userId)); }

    @GetMapping("/order/course/page")
    public Map<String, Object> courseOrderPage(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ok(petOrderService.page(keyword, status, page, pageSize));
    }

    @GetMapping("/order/course/list")
    public Map<String, Object> courseOrderList() { return ok(petOrderService.listAll()); }

    @GetMapping("/order/course/{id}")
    public Map<String, Object> courseOrderDetail(@PathVariable Long id) {
        PetOrder o = petOrderService.getById(id);
        return o != null ? ok(o) : fail("订单不存在");
    }

    @PutMapping("/order/course/status")
    public Map<String, Object> courseOrderUpdateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        if (status == 4) {
            try {
                petOrderService.adminRefund(id);
                return ok("退款成功", null);
            } catch (RuntimeException e) {
                return fail(e.getMessage());
            }
        }
        int rows = petOrderService.updateStatus(id, status);
        return rows > 0 ? ok("状态更新成功", null) : fail("状态更新失败");
    }

    @DeleteMapping("/order/course/delete/{id}")
    public Map<String, Object> courseOrderDelete(@PathVariable Long id) {
        int rows = petOrderService.deleteById(id);
        return rows > 0 ? ok("删除成功", null) : fail("删除失败");
    }

    @DeleteMapping("/order/course/batch")
    public Map<String, Object> courseOrderBatchDelete(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> ids = ((List<Number>) params.get("ids")).stream().map(Number::longValue).toList();
        int rows = petOrderService.deleteBatch(ids);
        return rows > 0 ? ok("批量删除成功，共删除" + rows + "条", null) : fail("批量删除失败");
    }

    @GetMapping("/order/boarding/page")
    public Map<String, Object> boardingOrderPage(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ok(boardingOrderService.page(keyword, status, page, pageSize));
    }

    @GetMapping("/order/boarding/list")
    public Map<String, Object> boardingOrderList() { return ok(boardingOrderService.listAll()); }

    @GetMapping("/order/boarding/{id}")
    public Map<String, Object> boardingOrderDetail(@PathVariable Long id) {
        BoardingOrder o = boardingOrderService.getById(id);
        return o != null ? ok(o) : fail("订单不存在");
    }

    @PutMapping("/order/boarding/status")
    public Map<String, Object> boardingOrderUpdateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        if (status == 4) {
            try {
                boardingOrderService.adminRefund(id);
                return ok("退款成功", null);
            } catch (RuntimeException e) {
                return fail(e.getMessage());
            }
        }
        int rows = boardingOrderService.updateStatus(id, status);
        return rows > 0 ? ok("状态更新成功", null) : fail("状态更新失败");
    }

    @DeleteMapping("/order/boarding/delete/{id}")
    public Map<String, Object> boardingOrderDelete(@PathVariable Long id) {
        int rows = boardingOrderService.deleteById(id);
        return rows > 0 ? ok("删除成功", null) : fail("删除失败");
    }

    @DeleteMapping("/order/boarding/batch")
    public Map<String, Object> boardingOrderBatchDelete(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> ids = ((List<Number>) params.get("ids")).stream().map(Number::longValue).toList();
        int rows = boardingOrderService.deleteBatch(ids);
        return rows > 0 ? ok("批量删除成功，共删除" + rows + "条", null) : fail("批量删除失败");
    }
}
