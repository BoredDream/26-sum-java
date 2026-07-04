package com.training.system.info.service.impl;

import com.training.system.common.PageResult;
import com.training.system.common.ResultCode;
import com.training.system.info.dto.NoticeCreateDTO;
import com.training.system.info.dto.NoticeQueryDTO;
import com.training.system.info.dto.NoticeUpdateDTO;
import com.training.system.info.entity.Notice;
import com.training.system.exception.BusinessException;
import com.training.system.info.mapper.NoticeMapper;
import com.training.system.info.service.NoticeService;
import com.training.system.info.vo.NoticeVO;
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
import java.util.List;
import java.util.UUID;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    @Transactional
    public NoticeVO createNotice(NoticeCreateDTO dto, Long publisherId, MultipartFile file) {
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setType(dto.getType());
        notice.setTopFlag(dto.getTopFlag() != null ? dto.getTopFlag() : 0);
        notice.setPublisherId(publisherId);

        if (file != null && !file.isEmpty()) {
            notice.setAttachPath(saveFile(file));
        }

        noticeMapper.insert(notice);
        return getNoticeDetail(notice.getNoticeId());
    }

    @Override
    @Transactional
    public NoticeVO updateNotice(Long noticeId, NoticeUpdateDTO dto, MultipartFile file) {
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "公告不存在");
        }
        if (dto.getTitle() != null) notice.setTitle(dto.getTitle());
        if (dto.getContent() != null) notice.setContent(dto.getContent());
        if (dto.getType() != null) notice.setType(dto.getType());
        if (dto.getTopFlag() != null) notice.setTopFlag(dto.getTopFlag());

        if (file != null && !file.isEmpty()) {
            if (notice.getAttachPath() != null) deleteFile(notice.getAttachPath());
            notice.setAttachPath(saveFile(file));
        }

        noticeMapper.update(notice);
        return getNoticeDetail(noticeId);
    }

    @Override
    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "公告不存在");
        }
        noticeMapper.deleteById(noticeId);
    }

    @Override
    public NoticeVO getNoticeDetail(Long noticeId) {
        NoticeVO vo = noticeMapper.selectById(noticeId);
        if (vo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "公告不存在");
        }
        return vo;
    }

    @Override
    public PageResult<NoticeVO> pageNotices(NoticeQueryDTO query) {
        int offset = (query.getPageNum() - 1) * query.getPageSize();
        List<NoticeVO> records = noticeMapper.selectPage(query.getKeyword(), query.getType(), offset, query.getPageSize());
        long total = noticeMapper.countPage(query.getKeyword(), query.getType());
        return new PageResult<>(records, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    @Transactional
    public void toggleTop(Long noticeId) {
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "公告不存在");
        }
        int newFlag = notice.getTopFlag() == 1 ? 0 : 1;
        noticeMapper.updateTopFlag(noticeId, newFlag);
    }

    @Override
    public String getAttachmentPath(Long noticeId) {
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "公告不存在");
        }
        return notice.getAttachPath();
    }

    private String saveFile(MultipartFile file) {
        try {
            String subDir = uploadDir + File.separator + "notices";
            Files.createDirectories(Paths.get(subDir));
            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path filepath = Paths.get(subDir, filename);
            file.transferTo(filepath.toFile());
            return "notices/" + filename;
        } catch (IOException e) {
            throw new BusinessException(ResultCode.ERROR, "文件上传失败");
        }
    }

    private void deleteFile(String relativePath) {
        try {
            Files.deleteIfExists(Paths.get(uploadDir, relativePath));
        } catch (IOException ignored) {}
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
