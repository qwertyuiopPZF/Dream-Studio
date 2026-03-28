package blog.service;

import blog.dto.MomentDTO;
import blog.dto.MomentQueryDTO;
import blog.vo.MomentVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MomentService
{


    List<MomentVO> listAllMoments();

<<<<<<< HEAD
=======
    List<MomentVO> listMomentsByAuthorId(Long authorId);

>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
    void createMoment(MomentDTO momentDTO);

    MomentVO getMomentById(Long id);

    void deleteMoment(Long id);

    void updateMoment(Integer id, MomentDTO momentDTO);

    /**
     * 统计动态总数
     */
    Long countTotal();
}
