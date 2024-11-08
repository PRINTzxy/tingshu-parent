package com.atguigu.tingshu.dispatch.job;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.dispatch.mapper.XxlJobLogMapper;
import com.atguigu.tingshu.model.dispatch.XxlJobLog;
import com.atguigu.tingshu.search.client.SearchFeignClient;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DispatchJobHandler {
    @Autowired
    private XxlJobLogMapper xxlJobLogMapper;
    @Qualifier("com.atguigu.tingshu.user.client.UserInfoFeignClient")
    @Autowired
    private UserInfoFeignClient userInfoFeignClient;
    @Resource
    private SearchFeignClient searchFeignClient;

    @XxlJob("updateExpiredVipStatusJob")
    public void firstJobHandler() {
        log.info("更新过期vip用户的状态：{}", XxlJobHelper.getJobId());
        long startTime = System.currentTimeMillis();

        XxlJobLog xxlJobLog = new XxlJobLog();
        xxlJobLog.setJobId(XxlJobHelper.getJobId());
        try {
            Result result = userInfoFeignClient.updateExpiredVipStatus();
            //  如果result为空，则说明调用失败
            if (result == null){
                xxlJobLog.setStatus(0);
                xxlJobLog.setError("远程调用更新接口失败！");
                log.error("更新过期Vip用户状态失败！任务id：{}",XxlJobHelper.getJobId());
                //任务处理失败
                XxlJobHelper.handleFail();
            }else {
                xxlJobLog.setStatus(1);
                XxlJobHelper.handleSuccess();
            }
        }catch (Exception e){
            xxlJobLog.setStatus(0); //失败
            xxlJobLog.setError("任务执行失败！");
            log.error("更新过期Vip用户状态失败！任务ID：{}",XxlJobHelper.getJobId());
            //任务处理失败
            XxlJobHelper.handleFail();
        }finally {
            //记录执行时间
            xxlJobLog.setTimes((int)(System.currentTimeMillis() - startTime));
            this.xxlJobLogMapper.insert(xxlJobLog);
        }
    }

    @XxlJob("updateLatelyAlbumStatJob")
    public void updateLatelyAlbumStatJob(){
        log.info("更新专辑统计到es：{}", XxlJobHelper.getJobId());
        long startTime = System.currentTimeMillis();
        XxlJobLog xxlJobLog = new XxlJobLog();
        xxlJobLog.setJobId(XxlJobHelper.getJobId());
        try {
            Result result = this.searchFeignClient.updateLatelyAlbumStat();
            //如果result为空，说明远程调用失败
            if (result == null){
                xxlJobLog.setStatus(0); //失败
                xxlJobLog.setError("远程调用更新接口失败！");
                log.error("更新专辑统计到es失败！任务id：{}",XxlJobHelper.getJobId());
                //任务处理失败
                XxlJobHelper.handleFail();
            }else {
                xxlJobLog.setStatus(1);
                XxlJobHelper.handleSuccess();
            }
        }catch (Exception e){
            xxlJobLog.setStatus(0); //执行失败
            xxlJobLog.setError("任务执行失败");
            log.error("更新专辑统计到es失败！任务id：{}", XxlJobHelper.getJobId());
            //任务处理失败
            XxlJobHelper.handleFail();
        }finally {
            //记录执行时间
            xxlJobLog.setTimes((int)(System.currentTimeMillis() - startTime));
            this.xxlJobLogMapper.insert(xxlJobLog);

        }
    }
}