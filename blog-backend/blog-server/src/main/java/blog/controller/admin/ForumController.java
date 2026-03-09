package blog.controller.admin;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminForumController")
@RequestMapping("/admin/Forum")
@Slf4j
@ApiOperation("论坛管理")
public class ForumController
{

    /*
        删除评论/帖子
        审核帖子/评论
        是否置顶/加精，帖子/评论
     */
}
