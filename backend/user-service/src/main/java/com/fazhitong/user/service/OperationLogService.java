package com.fazhitong.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.user.entity.OperationLog;
import com.fazhitong.user.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /** 记录一条操作日志 */
    public void log(Long userId, String username, String action, String ip, String result) {
        OperationLog l = new OperationLog();
        l.setUserId(userId);
        l.setUsername(username);
        l.setAction(action);
        l.setIp(ip);
        l.setResult(result);
        operationLogMapper.insert(l);
    }

    /** 分页查询：按时间倒序，支持关键字(action/username)与结果(result)筛选 */
    public PageResult<OperationLog> pageList(PageParam pageParam, String keyword, String result) {
        LambdaQueryWrapper<OperationLog> qw = new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(OperationLog::getAction, keyword)
                    .or().like(OperationLog::getUsername, keyword));
        }
        if (result != null && !result.isBlank()) {
            qw.eq(OperationLog::getResult, result);
        }
        Page<OperationLog> page = operationLogMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), qw);
        return PageResult.of(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize());
    }

    /** 从请求中取客户端 IP：优先 X-Forwarded-For，退化 remoteAddr */
    public static String clientIp(HttpServletRequest request) {
        if (request == null) return null;
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
