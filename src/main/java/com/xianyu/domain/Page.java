package com.xianyu.domain;

/**
 * 封装页面的信息
 */
public class Page {
    //当前页的数据量
    private int limit = 10;
    //当前页
    private int current = 1;
    //数据总数
    private int rows;
    //连接到下一页(复用分页连接)
    private String path;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current > 0) {
            this.current = current;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows >= 0){
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取起始行
     *
     * @return
     */
    public int getOffset() {
        return (current - 1) * limit;
    }

    /**
     * 获取页面总数
     * @return
     */
    public int getTotal() {
        if (rows % limit == 0) {
            return rows / limit;
        }
        return rows / limit + 1;
    }

    /**
     * 获取当前页的前两页
     * @return
     */
    public int getFrom() {
        int from = current - 2;
        return from <= 0 ? 1 : from;
    }

    /**
     * 获取当前页的后两页
     * @return
     */
    public int getTo(){
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }
}
