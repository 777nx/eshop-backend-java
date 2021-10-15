package com.eshop.modules.user.dto;


import lombok.Data;

import java.io.Serializable;


/**
 * @ClassName TaskFinishDTO
 * @author wzz
 * @Date 2019/12/6
 **/
@Data
public class TaskFinishDTO implements Serializable {
    private String addTime;
    private String title;
    private Integer number;
}
