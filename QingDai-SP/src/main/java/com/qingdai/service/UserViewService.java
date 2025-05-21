package com.qingdai.service;

public interface UserViewService {
    /**
     * 获取网站总浏览量
     * @return 总浏览量
     */
    long getTotalViewCount();

    /**
     * 增加网站浏览量
     * @param ip 访问者IP
     * @return 更新后的总浏览量
     */
    long incrementViewCount(String ip);
} 