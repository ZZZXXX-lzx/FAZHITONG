package com.fabaotong.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.common.exception.BusinessException;
import com.fabaotong.payment.entity.Order;
import com.fabaotong.payment.entity.Member;
import com.fabaotong.payment.mapper.OrderMapper;
import com.fabaotong.payment.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderMapper orderMapper;
    private final MemberMapper memberMapper;

    public Order createOrder(Long userId, String orderType, BigDecimal amount) {
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo("FBT" + UUID.randomUUID().toString().substring(0, 16).toUpperCase());
        order.setOrderType(orderType);
        order.setAmount(amount);
        order.setStatus(0);
        orderMapper.insert(order);
        return order;
    }

    public Order pay(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() != 0) throw new BusinessException("订单已支付");
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
        // activate member if it's a member purchase
        if ("MEMBER".equals(order.getOrderType())) {
            activateMember(order.getUserId(), order.getAmount());
        }
        return order;
    }

    private void activateMember(Long userId, BigDecimal amount) {
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>().eq(Member::getUserId, userId));
        if (member == null) {
            member = new Member();
            member.setUserId(userId);
            member.setMemberType(amount.compareTo(new BigDecimal("6999")) >= 0 ? "PRO" : "STANDARD");
            member.setExpireDate(LocalDateTime.now().plusYears(1));
            member.setStatus(1);
            memberMapper.insert(member);
        } else {
            member.setMemberType(amount.compareTo(new BigDecimal("6999")) >= 0 ? "PRO" : "STANDARD");
            member.setExpireDate(member.getExpireDate().isBefore(LocalDateTime.now())
                    ? LocalDateTime.now().plusYears(1)
                    : member.getExpireDate().plusYears(1));
            member.setStatus(1);
            memberMapper.updateById(member);
        }
    }

    public PageResult<Order> listOrders(Long userId, PageParam pageParam) {
        Page<Order> page = orderMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreateTime));
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public PageResult<Order> listAllOrders(PageParam pageParam) {
        Page<Order> page = orderMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<Order>()
                        .orderByDesc(Order::getCreateTime));
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public Member getMember(Long userId) {
        return memberMapper.selectOne(
                new LambdaQueryWrapper<Member>().eq(Member::getUserId, userId));
    }
}
