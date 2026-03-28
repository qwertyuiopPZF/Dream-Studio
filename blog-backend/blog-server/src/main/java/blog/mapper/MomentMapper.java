package blog.mapper;

import blog.dto.MomentDTO;
import blog.entity.Moment;
import blog.vo.MomentVO;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface MomentMapper
{
<<<<<<< HEAD
    @Insert("INSERT INTO moment(content, image, status, publish_time, create_time, update_time) " +
            "VALUES(#{content}, #{image}, #{status}, " +
=======
    @Insert("INSERT INTO moment(author_id, content, image, status, publish_time, create_time, update_time) " +
            "VALUES(#{authorId}, #{content}, #{image}, #{status}, " +
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
            "CASE WHEN #{status} = 1 THEN NOW() ELSE NULL END, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(MomentDTO momentDTO);

<<<<<<< HEAD
    @Select("SELECT * FROM moment WHERE id = #{id}")
    MomentVO selectById(Long id);

    @Select("SELECT * FROM moment")
    List<MomentVO> selectAll();

=======
    @Select("SELECT m.id, m.author_id as authorId, ua.nickname as authorNickname, ua.avatar as authorAvatar, m.content, m.image, m.status, m.publish_time as publishTime, m.create_time as createTime, m.update_time as updateTime, m.view_count as viewCount FROM moment m LEFT JOIN user_account ua ON m.author_id = ua.id WHERE m.id = #{id}")
    MomentVO selectById(Long id);

    @Select("SELECT m.id, m.author_id as authorId, ua.nickname as authorNickname, ua.avatar as authorAvatar, m.content, m.image, m.status, m.publish_time as publishTime, m.create_time as createTime, m.update_time as updateTime, m.view_count as viewCount FROM moment m LEFT JOIN user_account ua ON m.author_id = ua.id ORDER BY COALESCE(m.publish_time, m.create_time) DESC")
    List<MomentVO> selectAll();

    @Select("SELECT m.id, m.author_id as authorId, ua.nickname as authorNickname, ua.avatar as authorAvatar, m.content, m.image, m.status, m.publish_time as publishTime, m.create_time as createTime, m.update_time as updateTime, m.view_count as viewCount FROM moment m LEFT JOIN user_account ua ON m.author_id = ua.id WHERE m.author_id = #{authorId} ORDER BY COALESCE(m.publish_time, m.create_time) DESC")
    List<MomentVO> selectByAuthorId(Long authorId);

>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
    @Delete("delete from moment WHERE id = #{id}")
    void deleteById(Long id);

    void update(Moment moment);

    /**
     * 统计动态总数
     */
    @Select("SELECT COUNT(*) FROM moment")
    Long countTotal();
}
