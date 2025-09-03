package com.myproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.myproject.dto.MealRecordCreateDTO;
import com.myproject.entity.MealImage;
import com.myproject.entity.MealRecord;
import com.myproject.enums.MealType;
import com.myproject.mapper.MealImageMapper;
import com.myproject.mapper.MealRecordMapper;
import com.myproject.service.MealRecordService;
import com.myproject.vo.MealRecordVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final String IMAGE_UPLOAD_DIR = "src/main/resources/static/";

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
                int order = 0;
                for (MultipartFile image : createDTO.getImages()) {
                    if (!image.isEmpty()) {
                        try {
                            String imageUrl = saveImage(image, userId, mealRecord.getId(), order);
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

    /**
     * 保存图片到本地文件系统
     */
    private String saveImage(MultipartFile image, Long userId, Long recordId, int order) throws IOException {
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

        String filename = String.format("%d_%s_%d_%d%s",
                userId, dateStr, recordId, order, fileExtension);

        // 保存文件
        Path filePath = Paths.get(IMAGE_UPLOAD_DIR + filename);
        Files.write(filePath, image.getBytes());

        // 返回相对路径，用于前端访问
        return "/" + filename;
    }

    @Override
    public List<MealRecordVO> getMyRecords(Long userId) {
        try {
            QueryWrapper<MealRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByDesc("record_date")
                    .orderByDesc("created_at");

            List<MealRecord> records = mealRecordMapper.selectList(queryWrapper);
            List<MealRecordVO> result = new ArrayList<>();

            for (MealRecord record : records) {
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

                result.add(vo);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取饮食记录失败: " + e.getMessage(), e);
        }
    }
}