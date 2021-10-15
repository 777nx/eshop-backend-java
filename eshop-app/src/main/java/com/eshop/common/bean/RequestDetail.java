
package com.eshop.common.bean;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * 请求详情信息
 * @author zhonghui
 * @date 2020-04-30
 */
@Data
@Accessors(chain = true)
public class RequestDetail implements Serializable {
	private static final long serialVersionUID = 2543641512850125440L;

	/**
     * 请求ip地址
     */
    private String ip;

    /**
     * 请求路径
     */
    private String path;

}
