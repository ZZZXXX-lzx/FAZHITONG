package com.fabaotong.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fabaotong.user.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
