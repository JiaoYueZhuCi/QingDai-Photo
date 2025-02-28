package com.qingdai.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//返回给前端的通用json数据串
@Data   //set/get方法
@AllArgsConstructor //有参构造器
@NoArgsConstructor  //无参构造器
public class CommonResult<T> {
    //状态码
    private Integer code;
    //提示消息
    private String message;
    //泛型，对应类型的json数据
    private T data;

    //自定义两个参数的构造方法
    public CommonResult(Integer code, String message) {
        this(code, message, null);
    }

}