package blog.controller.admin;

import blog.dto.AnnouncementDTO;
import blog.dto.SiteSettingsDTO;
import blog.entity.UserAccount;
import blog.result.Result;
import blog.service.AccessControlService;
import blog.service.AnnouncementService;
import blog.service.SiteSettingsService;
import blog.vo.AnnouncementVO;
import blog.vo.SiteSettingsVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("adminSiteController")
@RequestMapping("/admin/site")
public class SiteController
{
    @Autowired
    private SiteSettingsService siteSettingsService;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private AccessControlService accessControlService;

    @GetMapping("/info")
    @ApiOperation("获取后台站点信息")
    public Result<SiteSettingsVO> getSiteInfo(Authentication authentication)
    {
        try {
            accessControlService.requireAdmin(requireUsername(authentication));
            return Result.success(siteSettingsService.getSiteSettings());
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @PutMapping("/info")
    @ApiOperation("更新站点信息")
    public Result<SiteSettingsVO> updateSiteInfo(@RequestBody SiteSettingsDTO siteSettingsDTO,
                                                 Authentication authentication)
    {
        try {
            accessControlService.requireAdmin(requireUsername(authentication));
            return Result.success(siteSettingsService.updateSiteSettings(siteSettingsDTO));
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @GetMapping("/announcements")
    @ApiOperation("获取公告列表")
    public Result<List<AnnouncementVO>> getAnnouncements(Authentication authentication)
    {
        try {
            accessControlService.requireAdmin(requireUsername(authentication));
            return Result.success(announcementService.listAnnouncements(false));
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @PostMapping("/announcements")
    @ApiOperation("发布公告")
    public Result<AnnouncementVO> createAnnouncement(@RequestBody AnnouncementDTO announcementDTO,
                                                     Authentication authentication)
    {
        try {
            UserAccount currentUser = accessControlService.requireAdmin(requireUsername(authentication));
            return Result.success(announcementService.createAnnouncement(currentUser.getId(), announcementDTO));
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @DeleteMapping("/announcements/{id}")
    @ApiOperation("删除公告")
    public Result<Void> deleteAnnouncement(@PathVariable Long id,
                                           Authentication authentication)
    {
        try {
            accessControlService.requireAdmin(requireUsername(authentication));
            announcementService.deleteAnnouncement(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    private String requireUsername(Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("请先登录");
        }
        return authentication.getName();
    }
}
