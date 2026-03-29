package blog.mapper;

import blog.dto.MomentDTO;
import blog.entity.Moment;
import blog.vo.MomentVO;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface MomentMapper
{
    @Insert("INSERT INTO moment(author_id, content, image, status, publish_time, create_time, update_time) " +
            "VALUES(#{authorId}, #{content}, #{image}, #{status}, " +
            "CASE WHEN #{status} = 1 THEN NOW() ELSE NULL END, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(MomentDTO momentDTO);

    @Select("SELECT m.id, m.author_id as authorId, ua.nickname as authorNickname, ua.avatar as authorAvatar, m.content, m.image, m.status, m.publish_time as publishTime, m.create_time as createTime, m.update_time as updateTime, m.view_count as viewCount FROM moment m LEFT JOIN user_account ua ON m.author_id = ua.id WHERE m.id = #{id}")
    MomentVO selectById(Long id);

    @Select("SELECT m.id, m.author_id as authorId, ua.nickname as authorNickname, ua.avatar as authorAvatar, m.content, m.image, m.status, m.publish_time as publishTime, m.create_time as createTime, m.update_time as updateTime, m.view_count as viewCount FROM moment m LEFT JOIN user_account ua ON m.author_id = ua.id ORDER BY COALESCE(m.publish_time, m.create_time) DESC")
    List<MomentVO> selectAll();

    @Select("SELECT m.id, m.author_id as authorId, ua.nickname as authorNickname, ua.avatar as authorAvatar, m.content, m.image, m.status, m.publish_time as publishTime, m.create_time as createTime, m.update_time as updateTime, m.view_count as viewCount FROM moment m LEFT JOIN user_account ua ON m.author_id = ua.id WHERE m.status = 1 ORDER BY COALESCE(m.publish_time, m.create_time) DESC")
    List<MomentVO> selectPublished();

    @Select("SELECT m.id, m.author_id as authorId, ua.nickname as authorNickname, ua.avatar as authorAvatar, m.content, m.image, m.status, m.publish_time as publishTime, m.create_time as createTime, m.update_time as updateTime, m.view_count as viewCount FROM moment m LEFT JOIN user_account ua ON m.author_id = ua.id WHERE m.author_id = #{authorId} ORDER BY COALESCE(m.publish_time, m.create_time) DESC")
    List<MomentVO> selectByAuthorId(Long authorId);

    @Delete("delete from moment WHERE id = #{id}")
    void deleteById(Long id);

    void update(Moment moment);

    /**
     * 统计动态总数
     */
    @Select("SELECT COUNT(*) FROM moment")
    Long countTotal();
}
