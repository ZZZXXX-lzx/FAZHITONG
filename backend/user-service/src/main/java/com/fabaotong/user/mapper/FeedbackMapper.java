package com.fabaotong.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fabaotong.user.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 意见反馈Mapper
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
}
