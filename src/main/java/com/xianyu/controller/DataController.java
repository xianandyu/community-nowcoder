package com.xianyu.controller;

import com.xianyu.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    /**
     * 转发需要相同的请求==>设置两请求
     * @return
     */
    @RequestMapping (path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage(){
        return "/site/admin/data";
    }

    /**
     * DateTimeFormat==>将页面日期格式转换为相应格式
     * @param start
     * @param end
     * @param model
     * @return
     */
    @PostMapping(path = "/data/uv")
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")Date end, Model model){
        long uvCount = dataService.getUVCount(start, end);


        model.addAttribute("uvCount",uvCount);
        model.addAttribute("uvStartDate", start);
        model.addAttribute("uvEndDate", end);

        return "forward:/data";//转发参数会携带过去，且双方请求方式相同
    }

    /**
     * DateTimeFormat==>将页面日期格式转换为相应格式
     * @param start
     * @param end
     * @param model
     * @return
     */
    @PostMapping(path = "/data/dvu")
    public String getDVU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")Date end, Model model){
        long dauCount = dataService.getDAUCount(start, end);


        model.addAttribute("dvuCount",dauCount);
        model.addAttribute("dvuStartDate", start);
        model.addAttribute("dvuEndDate", end);

        return "forward:/data";//转发参数会携带过去，且双方请求方式相同
    }
}
