package blog.controller.user;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userPostController")
@RequestMapping("/api/post")
@Slf4j
@ApiOperation("评论管理")
public class ForumController
{

    /*
    获取帖子列表postlist
    发布帖子createpost
    删除帖子deletepost
    获取帖子详细信息getpostdetail

    创建评论
    删除评论
    获取评论列表
    点赞/点踩/收藏功能
     */
}
