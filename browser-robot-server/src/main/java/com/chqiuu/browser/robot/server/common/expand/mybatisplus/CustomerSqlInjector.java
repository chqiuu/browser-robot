package com.chqiuu.browser.robot.server.common.expand.mybatisplus;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.chqiuu.spider.common.expand.mybatisplus.methods.InsertIgnore;
import com.chqiuu.spider.common.expand.mybatisplus.methods.InsertIgnoreBatch;
import com.chqiuu.spider.common.expand.mybatisplus.methods.Replace;

import java.util.List;

/**
 * 自定义sql注入器，增加通用方法
 *
 * @author chqiu
 */
public class CustomerSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        // 插入数据，如果中已经存在相同的记录，则忽略当前新数据
        methodList.add(new InsertIgnore());
        // 批量插入数据，如果中已经存在相同的记录，则忽略当前新数据
        methodList.add(new InsertIgnoreBatch());
        // 替换数据，如果中已经存在相同的记录，则覆盖旧数据
        methodList.add(new Replace());
        return methodList;
    }
}