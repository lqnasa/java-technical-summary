package com.onemt.news.crawler.web.util;


import com.onemt.news.crawler.web.domain.ResultVO;
import com.onemt.news.crawler.web.enums.ResultCodeEnums;

/**
 * 
 * 项目名称：crawler-task
 * 类名称：ResultUtils
 * 类描述： 
 * 创建人：Administrator 
 * 创建时间：2018年1月15日 下午4:10:51
 * 修改人：Administrator 
 * 修改时间：2018年1月15日 下午4:10:51
 * 修改备注： 
 * @version
 */
public class ResultUtils {

    public static ResultVO success(Object data){
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(ResultCodeEnums.OK.getCode());
        resultVO.setData(data);
        resultVO.setMsg("成功");
        return resultVO;
    }

    public static ResultVO success() {
        return success(null);
    }

    public static ResultVO error(Integer code , String msg) {
        ResultVO resultVO = new ResultVO();
        resultVO.setMsg(msg);
        resultVO.setCode(code);
        return resultVO;
    }

    public static ResultVO error(ResultCodeEnums resultCodeEnums) {
        ResultVO resultVO = new ResultVO();
        resultVO.setMsg(resultCodeEnums.getMsg());
        resultVO.setCode(resultCodeEnums.getCode());
        return resultVO;
    }
}
