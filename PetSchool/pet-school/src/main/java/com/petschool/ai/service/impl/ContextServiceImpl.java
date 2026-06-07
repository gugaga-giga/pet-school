package com.petschool.ai.service.impl;

import com.petschool.ai.context.CourseContext;
import com.petschool.ai.context.PetContext;
import com.petschool.ai.context.UserContext;
import com.petschool.ai.service.ContextService;
import com.petschool.entity.*;
import com.petschool.mapper.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 上下文构建服务实现
 */
@Service
public class ContextServiceImpl implements ContextService {

    private final PetMapper petMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final VaccineRecordMapper vaccineRecordMapper;
    private final PetOrderMapper petOrderMapper;
    private final CourseMapper courseMapper;
    private final CourseCategoryMapper courseCategoryMapper;
    private final UserMapper userMapper;

    public ContextServiceImpl(PetMapper petMapper,
                              HealthRecordMapper healthRecordMapper,
                              VaccineRecordMapper vaccineRecordMapper,
                              PetOrderMapper petOrderMapper,
                              CourseMapper courseMapper,
                              CourseCategoryMapper courseCategoryMapper,
                              UserMapper userMapper) {
        this.petMapper = petMapper;
        this.healthRecordMapper = healthRecordMapper;
        this.vaccineRecordMapper = vaccineRecordMapper;
        this.petOrderMapper = petOrderMapper;
        this.courseMapper = courseMapper;
        this.courseCategoryMapper = courseCategoryMapper;
        this.userMapper = userMapper;
    }

    @Override
    public UserContext buildUserContext(HttpServletRequest request) {
        UserContext ctx = UserContext.fromRequest(request);
        // 补充phone字段
        if (ctx.getUserId() != null) {
            try {
                User user = userMapper.selectById(ctx.getUserId());
                if (user != null && user.getPhone() != null) {
                    ctx.setPhone(user.getPhone());
                }
            } catch (Exception ignored) {
                // 查询失败不影响主流程
            }
        }
        return ctx;
    }

    @Override
    public List<PetContext> buildPetContext(Long userId) {
        List<Pet> pets = petMapper.selectByUserId(userId);
        if (pets == null || pets.isEmpty()) {
            return Collections.emptyList();
        }

        // 预加载所有课程分类
        List<CourseCategory> categories = courseCategoryMapper.selectAll();
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(CourseCategory::getId, CourseCategory::getName, (a, b) -> a));

        return pets.stream().map(pet -> {
            PetContext ctx = new PetContext();
            ctx.setPetId(pet.getId());
            ctx.setPetName(pet.getName());
            ctx.setBreed(pet.getBreed());
            ctx.setAge(pet.getAge());
            ctx.setWeight(pet.getWeight());
            ctx.setGender(pet.getGender());
            ctx.setSterilized(pet.getSterilized());
            ctx.setAllergyInfo(pet.getAllergyInfo());

            // 从健康记录推断健康状态
            HealthRecord latestHealth = healthRecordMapper.selectLatestByPetId(pet.getId());
            if (latestHealth != null) {
                List<String> issues = new ArrayList<>();
                if (latestHealth.getTemperature() != null && latestHealth.getTemperature().compareTo(new BigDecimal("39")) > 0) {
                    issues.add("体温偏高");
                }
                if (latestHealth.getAppetite() != null && latestHealth.getAppetite() < 2) {
                    issues.add("食欲下降");
                }
                if (latestHealth.getSpirit() != null && latestHealth.getSpirit() < 2) {
                    issues.add("精神低落");
                }
                ctx.setHealthStatus(issues.isEmpty() ? "健康" : String.join("、", issues));
            } else {
                ctx.setHealthStatus("暂无记录");
            }

            // 从疫苗记录推断疫苗状态
            List<VaccineRecord> vaccines = vaccineRecordMapper.selectByPetId(pet.getId());
            if (vaccines != null && !vaccines.isEmpty()) {
                LocalDate today = LocalDate.now();
                boolean hasOverdue = vaccines.stream()
                        .anyMatch(v -> v.getNextDueDate() != null && v.getNextDueDate().isBefore(today));
                boolean hasUpcoming = vaccines.stream()
                        .anyMatch(v -> v.getNextDueDate() != null && !v.getNextDueDate().isBefore(today)
                                && ChronoUnit.DAYS.between(today, v.getNextDueDate()) <= 30);
                if (hasOverdue) {
                    ctx.setVaccineStatus("有逾期疫苗");
                } else if (hasUpcoming) {
                    ctx.setVaccineStatus("有待接种");
                } else {
                    ctx.setVaccineStatus("疫苗齐全");
                }
            } else {
                ctx.setVaccineStatus("暂无记录");
            }

            return ctx;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CourseContext> buildCourseContext(Long userId) {
        List<PetOrder> orders = petOrderMapper.selectByUserId(userId);
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        // 预加载所有课程分类
        List<CourseCategory> categories = courseCategoryMapper.selectAll();
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(CourseCategory::getId, CourseCategory::getName, (a, b) -> a));

        return orders.stream().map(order -> {
            CourseContext ctx = new CourseContext();
            ctx.setCourseName(order.getCourseName());

            // 尝试获取课程详情
            if (order.getPackageId() != null) {
                // 通过订单中的课程名查找课程
                List<Course> allCourses = courseMapper.selectAll();
                for (Course course : allCourses) {
                    if (course.getName() != null && course.getName().equals(order.getCourseName())) {
                        ctx.setCourseId(course.getId());
                        ctx.setDescription(course.getDescription());
                        ctx.setPrice(course.getMonthlyPrice());
                        ctx.setCategoryName(categoryMap.getOrDefault(course.getCategoryId(), "未分类"));
                        break;
                    }
                }
            }

            // 根据订单状态设置课程状态
            if (order.getStatus() != null) {
                switch (order.getStatus()) {
                    case 0 -> ctx.setStatus("purchased");    // 已购买
                    case 1 -> ctx.setStatus("learning");     // 学习中
                    case 2 -> ctx.setStatus("completed");    // 已完成
                    default -> ctx.setStatus("purchased");
                }
            }

            return ctx;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> buildFullContext(HttpServletRequest request) {
        Map<String, Object> context = new HashMap<>();

        UserContext userContext = buildUserContext(request);
        context.put("user", userContext);

        if (userContext.getUserId() != null) {
            List<PetContext> petContexts = buildPetContext(userContext.getUserId());
            context.put("pets", petContexts);

            List<CourseContext> courseContexts = buildCourseContext(userContext.getUserId());
            context.put("courses", courseContexts);

            Map<String, Object> orderContext = buildOrderContext(userContext.getUserId());
            context.put("orders", orderContext);
        }

        return context;
    }

    @Override
    public List<CourseContext> buildCourseCatalog() {
        List<Course> courses = courseMapper.selectAll();
        // 预加载所有课程分类
        List<CourseCategory> categories = courseCategoryMapper.selectAll();
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(CourseCategory::getId, CourseCategory::getName, (a, b) -> a));

        return courses.stream().map(course -> {
            CourseContext ctx = new CourseContext();
            ctx.setCourseId(course.getId());
            ctx.setCourseName(course.getName());
            ctx.setDescription(course.getDescription());
            ctx.setPrice(course.getMonthlyPrice());
            ctx.setCategoryName(categoryMap.getOrDefault(course.getCategoryId(), "未分类"));
            ctx.setStatus("available");
            return ctx;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> buildOrderContext(Long userId) {
        Map<String, Object> orderCtx = new HashMap<>();
        List<PetOrder> orders = petOrderMapper.selectByUserId(userId);
        orderCtx.put("totalOrders", orders.size());
        orderCtx.put("pendingPayment", orders.stream().filter(o -> o.getStatus() != null && o.getStatus() == 0).count());
        orderCtx.put("completed", orders.stream().filter(o -> o.getStatus() != null && o.getStatus() == 2).count());
        orderCtx.put("cancelled", orders.stream().filter(o -> o.getStatus() != null && o.getStatus() == 3).count());
        return orderCtx;
    }
}
