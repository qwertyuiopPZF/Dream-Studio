package blog.controller.admin;

import blog.dto.MomentDTO;
import blog.entity.UserAccount;
import blog.result.Result;
import blog.service.AccessControlService;
import blog.service.MomentService;
import blog.vo.MomentVO;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController("adminMomentController")
@RequestMapping("/admin/moment")
@Slf4j
@ApiOperation("动态管理页面")
public class MomentController
{
    @Autowired
    private MomentService momentService;

    @Autowired
    private AccessControlService accessControlService;

    @PostMapping
    @ApiOperation("创建动态")
    public Result<Void> creatMoment(@RequestBody MomentDTO momentDTO,
                                    Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        UserAccount currentUser = accessControlService.requireUser(authentication.getName());
        momentDTO.setAuthorId(currentUser.getId());
        momentService.createMoment(momentDTO);
        return Result.success();
    }

    @GetMapping
    @ApiOperation("获取动态列表")
    public Result<PageInfo<MomentVO>> getMoment(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        try {
            UserAccount currentUser = accessControlService.requireUser(authentication.getName());
            List<MomentVO> moments = accessControlService.isAdmin(currentUser)
                    ? momentService.listAllMoments()
                    : momentService.listMomentsByAuthorId(currentUser.getId());

            int safePage = Math.max(page, 1);
            int safeSize = Math.max(size, 1);
            int total = moments.size();
            int startIndex = (safePage - 1) * safeSize;
            int endIndex = Math.min(startIndex + safeSize, total);
            List<MomentVO> records = startIndex < total ? moments.subList(startIndex, endIndex) : Collections.emptyList();

            PageInfo<MomentVO> pageInfo = new PageInfo<>(records);
            pageInfo.setPageNum(safePage);
            pageInfo.setPageSize(safeSize);
            pageInfo.setTotal(total);
            pageInfo.setPages((int) Math.ceil((double) total / safeSize));
            return Result.success(pageInfo);
        } catch (Exception e) {
            log.error("获取动态列表异常", e);
            return Result.success(new PageInfo<>(Collections.emptyList()));
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询动态")
    public Result<MomentVO> getMoment(@PathVariable Long id,
                                      Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        MomentVO moment = momentService.getMomentById(id);
        if (moment == null) {
            return Result.error("动态不存在");
        }

        try {
            accessControlService.requireAdminOrOwner(authentication.getName(), moment.getAuthorId(), "该动态");
            return Result.success(moment);
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除动态")
    public Result<Void> deleteMoment(@PathVariable Long id,
                                     Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        MomentVO moment = momentService.getMomentById(id);
        if (moment == null) {
            return Result.error("动态不存在");
        }

        try {
            accessControlService.requireAdminOrOwner(authentication.getName(), moment.getAuthorId(), "该动态");
            momentService.deleteMoment(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新动态")
    public Result<Void> updateMoment(@PathVariable Long id,
                                     @RequestBody MomentDTO momentDTO,
                                     Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        MomentVO existingMoment = momentService.getMomentById(id);
        if (existingMoment == null) {
            return Result.error("动态不存在");
        }

        try {
            accessControlService.requireAdminOrOwner(authentication.getName(), existingMoment.getAuthorId(), "该动态");
            momentDTO.setAuthorId(existingMoment.getAuthorId());
            momentService.updateMoment(id.intValue(), momentDTO);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }
}
