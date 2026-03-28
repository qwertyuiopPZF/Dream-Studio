package blog.service;

import blog.dto.AnnouncementDTO;
import blog.vo.AnnouncementVO;

import java.util.List;

public interface AnnouncementService
{
    AnnouncementVO createAnnouncement(Long authorId, AnnouncementDTO announcementDTO);

    List<AnnouncementVO> listAnnouncements(boolean publishedOnly);

    AnnouncementVO getAnnouncementById(Long id);

    void deleteAnnouncement(Long id);
}
