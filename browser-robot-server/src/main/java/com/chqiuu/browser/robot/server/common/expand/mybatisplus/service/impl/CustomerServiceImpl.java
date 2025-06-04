package com.chqiuu.browser.robot.server.common.expand.mybatisplus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chqiuu.spider.common.expand.mybatisplus.mapper.CustomerMapper;
import com.chqiuu.spider.common.expand.mybatisplus.service.CustomerService;

import java.util.List;

/**
 * 自定义通用的Service
 *
 * @param <M>
 * @param <T>
 * @author chqiu
 */
public class CustomerServiceImpl<M extends CustomerMapper<T>, T> extends ServiceImpl<CustomerMapper<T>, T> implements CustomerService<T> {

    @Override
    public int insertIgnore(T entity) {
        return baseMapper.insertIgnore(entity);
    }

    @Override
    public int insertIgnoreBatch(List<T> entityList) {
        return baseMapper.insertIgnoreBatch(entityList);
    }

    @Override
    public int replace(T entity) {
        return baseMapper.replace(entity);
    }
}
