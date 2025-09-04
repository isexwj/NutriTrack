package com.myproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.myproject.dto.MealRecordCreateDTO;
import com.myproject.dto.MealRecordQueryDTO;
import com.myproject.dto.MealRecordUpdateDTO;
import com.myproject.entity.MealImage;
import com.myproject.entity.MealRecord;
import com.myproject.enums.MealType;
import com.myproject.mapper.MealImageMapper;
import com.myproject.mapper.MealRecordMapper;
import com.myproject.service.MealRecordService;
import com.myproject.vo.MealRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MealRecordServiceImpl implements MealRecordService {

    @Autowired
    private MealRecordMapper mealRecordMapper;

    @Autowired
    private MealImageMapper mealImageMapper;

    // 图片存储路径
    @Value("${file.save-path}") // 注入配置文件中定义的文件保存路径
    private String IMAGE_UPLOAD_DIR;
    //private final String IMAGE_UPLOAD_DIR = "src/main/resources/static.images/";

    @Override
    @Transactional
    public MealRecordVO createMealRecord(MealRecordCreateDTO createDTO, Long userId) {
        try {
            // 1. 创建并保存饮食记录
            MealRecord mealRecord = new MealRecord();
            BeanUtils.copyProperties(createDTO, mealRecord);
            mealRecord.setUserId(userId);

            // 处理记录日期，如果为空则设置为当天
            if (createDTO.getRecordDate() == null) {
                mealRecord.setRecordDate(LocalDate.now());
            }

            // 处理是否分享到社区
            mealRecord.setIsShared(createDTO.getIsShared() ? 1 : 0);

            mealRecordMapper.insert(mealRecord);

            // 2. 处理图片上传
            List<String> imageUrls = new ArrayList<>();
            if (createDTO.getImages() != null && !createDTO.getImages().isEmpty()) {
                int order = 1;
                for (MultipartFile image : createDTO.getImages()) {
                    if (!image.isEmpty()) {
                        try {
                            String imageUrl = saveImage(image, mealRecord.getId(), order);
                            imageUrls.add(imageUrl);

                            // 保存图片信息到数据库
                            MealImage mealImage = new MealImage();
                            mealImage.setRecordId(mealRecord.getId());
                            mealImage.setImageUrl(imageUrl);
                            mealImage.setUploadOrder(order);
                            mealImageMapper.insert(mealImage);

                            order++;
                        } catch (IOException e) {
                            // 记录错误日志，但不中断整个操作
                            // log.error("图片保存失败: {}", e.getMessage());
                        }
                    }
                }
            }

            // 3. 构建返回结果
            MealRecordVO result = new MealRecordVO();
            BeanUtils.copyProperties(mealRecord, result);
            result.setIsShared(mealRecord.getIsShared() == 1);
            result.setImageUrls(imageUrls);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("创建饮食记录失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MealRecordVO> getMealRecordsByUserId(Long userId) {
        QueryWrapper<MealRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("record_date")
                .orderByDesc("created_at");

        List<MealRecord> records = mealRecordMapper.selectList(queryWrapper);
        return convertToVOList(records);
    }

    @Override
    public List<MealRecordVO> getMealRecordsByQuery(Long userId, MealRecordQueryDTO queryDTO) {
        QueryWrapper<MealRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        if (queryDTO.getMealType() != null) {
            queryWrapper.eq("meal_type", queryDTO.getMealType());
        }

        if (queryDTO.getStartDate() != null) {
            queryWrapper.ge("record_date", queryDTO.getStartDate());
        }

        if (queryDTO.getEndDate() != null) {
            queryWrapper.le("record_date", queryDTO.getEndDate());
        }

        if (queryDTO.getIsShared() != null) {
            queryWrapper.eq("is_shared", queryDTO.getIsShared() ? 1 : 0);
        }

        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().trim().isEmpty()) {
            queryWrapper.like("description", queryDTO.getKeyword());
        }

        queryWrapper.orderByDesc("record_date").orderByDesc("created_at");

        List<MealRecord> records = mealRecordMapper.selectList(queryWrapper);
        return convertToVOList(records);
    }

    @Override
    public MealRecordVO getMealRecordById(Long id, Long userId) {
        MealRecord record = mealRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new RuntimeException("记录不存在或无权访问");
        }
        return convertToVO(record);
    }

    @Override
    @Transactional
    public MealRecordVO updateMealRecord(Long id, MealRecordUpdateDTO updateDTO, Long userId) {
        MealRecord record = mealRecordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改");
        }

        // 更新基本字段
        if (updateDTO.getMealType() != null) {
            record.setMealType(updateDTO.getMealType());
        }
        if (updateDTO.getDescription() != null) {
            record.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getCalories() != null) {
            record.setCalories(updateDTO.getCalories());
        }
        if (updateDTO.getRating() != null) {
            record.setRating(updateDTO.getRating());
        }
        if (updateDTO.getRecordDate() != null) {
            record.setRecordDate(updateDTO.getRecordDate());
        }
        if (updateDTO.getIsShared() != null) {
            record.setIsShared(updateDTO.getIsShared() ? 1 : 0);
        }

        mealRecordMapper.updateById(record);

        // 处理删除的图片 - 根据URL而不是ID
        if (updateDTO.getDeletedImageUrls() != null && !updateDTO.getDeletedImageUrls().isEmpty()) {
            for (String imageUrl : updateDTO.getDeletedImageUrls()) {
                // 根据URL查找图片记录
                QueryWrapper<MealImage> imageQueryWrapper = new QueryWrapper<>();
                imageQueryWrapper.eq("record_id", id)
                        .eq("image_url", imageUrl);
                MealImage image = mealImageMapper.selectOne(imageQueryWrapper);

                if (image != null) {
                    // 删除物理文件
                    deleteImageFile(image.getImageUrl());
                    // 删除数据库记录
                    mealImageMapper.deleteById(image.getId());
                }
            }
        }

        // 处理新上传的图片
        List<String> newImageUrls = new ArrayList<>();
        if (updateDTO.getNewImages() != null && !updateDTO.getNewImages().isEmpty()) {
            // 获取当前最大的upload_order
            QueryWrapper<MealImage> imageQueryWrapper = new QueryWrapper<>();
            imageQueryWrapper.eq("record_id", id)
                    .orderByDesc("upload_order")
                    .last("LIMIT 1");
            MealImage lastImage = mealImageMapper.selectOne(imageQueryWrapper);
            int startOrder = lastImage != null ? lastImage.getUploadOrder() + 1 : 1;

            int order = startOrder;
            for (MultipartFile image : updateDTO.getNewImages()) {
                if (!image.isEmpty()) {
                    try {
                        String imageUrl = saveImage(image, id, order);
                        newImageUrls.add(imageUrl);

                        // 保存图片信息到数据库
                        MealImage mealImage = new MealImage();
                        mealImage.setRecordId(id);
                        mealImage.setImageUrl(imageUrl);
                        mealImage.setUploadOrder(order);
                        mealImageMapper.insert(mealImage);

                        order++;
                    } catch (IOException e) {
                        // 记录错误日志，但不中断整个操作
                        // log.error("图片保存失败: {}", e.getMessage());
                    }
                }
            }
        }

        MealRecordVO result = convertToVO(record);
        return result;
    }

    @Override
    @Transactional
    public void deleteMealRecord(Long id, Long userId) {
        MealRecord record = mealRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new RuntimeException("记录不存在或无权删除");
        }

        // 删除关联的图片记录和物理文件
        QueryWrapper<MealImage> imageQueryWrapper = new QueryWrapper<>();
        imageQueryWrapper.eq("record_id", id);
        List<MealImage> images = mealImageMapper.selectList(imageQueryWrapper);

        for (MealImage image : images) {
            deleteImageFile(image.getImageUrl());
            mealImageMapper.deleteById(image.getId());
        }

        // 删除饮食记录
        mealRecordMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleShareStatus(Long id, Boolean isShared, Long userId) {
        MealRecord record = mealRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new RuntimeException("记录不存在或无权修改");
        }

        record.setIsShared(isShared ? 1 : 0);
        mealRecordMapper.updateById(record);
    }

    /**
     * 将MealRecord实体列表转换为VO列表
     */
    private List<MealRecordVO> convertToVOList(List<MealRecord> records) {
        List<MealRecordVO> result = new ArrayList<>();
        for (MealRecord record : records) {
            result.add(convertToVO(record));
        }
        return result;
    }

    /**
     * 将MealRecord实体转换为VO
     */
    private MealRecordVO convertToVO(MealRecord record) {
        MealRecordVO vo = new MealRecordVO();
        BeanUtils.copyProperties(record, vo);
        vo.setIsShared(record.getIsShared() == 1);

        // 查询关联的图片
        QueryWrapper<MealImage> imageQueryWrapper = new QueryWrapper<>();
        imageQueryWrapper.eq("record_id", record.getId())
                .orderByAsc("upload_order");

        List<MealImage> images = mealImageMapper.selectList(imageQueryWrapper);
        List<String> imageUrls = new ArrayList<>();
        for (MealImage image : images) {
            imageUrls.add(image.getImageUrl());
        }
        vo.setImageUrls(imageUrls);

        return vo;
    }

    /**
     * 保存图片到本地文件系统
     */
    private String saveImage(MultipartFile image, Long recordId, int order) throws IOException {
        // 确保目录存在
        File directory = new File(IMAGE_UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 生成文件名: {用户id_日期年月日_记录id_序号.jpg}
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String originalFilename = image.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".") ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";

        String filename = String.format("%d_%s_%d%s",
                recordId, dateStr, order, fileExtension);


        // 保存文件
        Path filePath = Paths.get(IMAGE_UPLOAD_DIR + filename);
        Files.write(filePath, image.getBytes());

        // 返回相对路径，用于前端访问
        return filename;
    }

    /**
     * 删除图片文件
     */
    private void deleteImageFile(String imageUrl) {
        System.out.println("awefawefffffffffffffffffffff");
        System.out.println(imageUrl);
        System.out.println(imageUrl);
        if (imageUrl != null) {
            File file = new File(IMAGE_UPLOAD_DIR + imageUrl);
            if (file.exists()) {
                file.delete();
            }
        }
    }


}