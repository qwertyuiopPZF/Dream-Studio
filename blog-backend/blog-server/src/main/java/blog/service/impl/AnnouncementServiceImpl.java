package blog.service.impl;

import blog.dto.AnnouncementDTO;
import blog.entity.Announcement;
import blog.mapper.AnnouncementMapper;
import blog.service.AnnouncementService;
import blog.vo.AnnouncementVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService
{
    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    @Transactional
    public AnnouncementVO createAnnouncement(Long authorId, AnnouncementDTO announcementDTO)
    {
        if (authorId == null) {
            throw new IllegalArgumentException("发布人不能为空");
        }
        validateAnnouncement(announcementDTO);

        LocalDateTime now = LocalDateTime.now();
        Announcement announcement = new Announcement();
        announcement.setAuthorId(authorId);
        announcement.setTitle(announcementDTO.getTitle().trim());
        announcement.setContent(announcementDTO.getContent().trim());
        announcement.setStatus(normalizeStatus(announcementDTO.getStatus()));
        announcement.setPublishTime(announcement.getStatus() == 1 ? now : null);
        announcement.setCreateTime(now);
        announcement.setUpdateTime(now);
        announcementMapper.insert(announcement);
        return announcementMapper.selectById(announcement.getId());
    }

    @Override
    public List<AnnouncementVO> listAnnouncements(boolean publishedOnly)
    {
        return publishedOnly ? announcementMapper.selectPublished() : announcementMapper.selectAll();
    }

    @Override
    public AnnouncementVO getAnnouncementById(Long id)
    {
        return announcementMapper.selectById(id);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id)
    {
        if (announcementMapper.selectById(id) == null) {
            throw new IllegalArgumentException("公告不存在");
        }
        announcementMapper.deleteById(id);
    }

    private void validateAnnouncement(AnnouncementDTO announcementDTO)
    {
        if (announcementDTO == null) {
            throw new IllegalArgumentException("公告内容不能为空");
        }
        if (!StringUtils.hasText(announcementDTO.getTitle())) {
            throw new IllegalArgumentException("公告标题不能为空");
        }
        if (!StringUtils.hasText(announcementDTO.getContent())) {
            throw new IllegalArgumentException("公告内容不能为空");
        }
    }

    private int normalizeStatus(Integer status)
    {
        return status != null && status == 0 ? 0 : 1;
    }
}
