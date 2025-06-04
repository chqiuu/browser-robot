package com.chqiuu.browser.robot.server.common.expand.mybatisplus;

import lombok.Getter;

/**
 * MybatisPlus自定义SQL方法枚举
 *
 * @author chqiu
 */
@Getter
public enum CustomerSqlMethod {
    /**
     * 插入
     */
    INSERT_IGNORE_ONE("insertIgnore", "插入一条数据（选择字段插入），如果中已经存在相同的记录，则忽略当前新数据", "<script>\nINSERT IGNORE INTO %s %s VALUES %s\n</script>"),
    /**
     * 替换
     */
    REPLACE_ONE("replace", "替换一条数据（选择字段插入），存在则替换，不存在则插入", "<script>\nREPLACE INTO %s %s VALUES %s\n</script>");

    private final String method;
    private final String desc;
    private final String sql;

    CustomerSqlMethod(String method, String desc, String sql) {
        this.method = method;
        this.desc = desc;
        this.sql = sql;
    }
}
