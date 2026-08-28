package com.fazhitong.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fazhitong.user.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
