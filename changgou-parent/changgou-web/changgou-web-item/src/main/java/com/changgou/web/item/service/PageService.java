package com.changgou.web.item.service;

public interface PageService {
    /**
     * 根据id创建模板页
     * @param id
     */
    public void createPageHtml(Long id);

    public void deleteHtml(Long id);
}
