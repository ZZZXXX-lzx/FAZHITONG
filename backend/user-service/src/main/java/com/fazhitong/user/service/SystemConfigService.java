package com.fazhitong.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fazhitong.user.entity.SystemConfigEntity;
import com.fazhitong.user.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    /** 全量读取：key -> value */
    public Map<String, String> getMap() {
        Map<String, String> map = new LinkedHashMap<>();
        systemConfigMapper.selectList(null).forEach(c -> map.put(c.getConfigKey(), c.getConfigValue()));
        return map;
    }

    /** 单键读取，带默认值 */
    public String get(String key, String defaultValue) {
        SystemConfigEntity c = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfigEntity>().eq(SystemConfigEntity::getConfigKey, key));
        return c == null || c.getConfigValue() == null ? defaultValue : c.getConfigValue();
    }

    /** 批量写入（存在更新，不存在插入） */
    @Transactional
    public void saveAll(Map<String, String> config) {
        if (config == null) return;
        config.forEach((key, value) -> upsert(key, value));
    }

    private void upsert(String key, String value) {
        SystemConfigEntity exist = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfigEntity>().eq(SystemConfigEntity::getConfigKey, key));
        if (exist == null) {
            SystemConfigEntity c = new SystemConfigEntity();
            c.setConfigKey(key);
            c.setConfigValue(value);
            systemConfigMapper.insert(c);
        } else {
            exist.setConfigValue(value);
            systemConfigMapper.updateById(exist);
        }
    }
}