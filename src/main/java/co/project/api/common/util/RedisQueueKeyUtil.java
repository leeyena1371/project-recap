package co.project.api.common.util;

import co.dearu.live.api.common.code.RedisKey;
import co.dearu.live.api.common.config.redis.RedisRepository;
import co.dearu.live.api.common.config.redis.RedisServerNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Redis Queue Bean
 * method 작명 추천 (필수는 아님)
 * - get [Cluster이름] [조회할 데이터] By [조회Key]
 *
 * 수정 필요
 *  - redis 서버 오류로 인한 exception 발생 시 공통 처리 방안 필요
 */
@Slf4j
@Component
public class RedisQueueKeyUtil {

    @Autowired
    RedisRepository redisRepository;

    @Value("${app-group.default}")
    private String defaultAppGroup;

    /* 라이브 채팅 QUEUE 저장 */
    public void setQueueMessage(String liveID, String chat) {
        try {
            String queue = String.format(RedisKey.LIVE_CHAT_QUEUE, liveID);
            log.info(">>> redis lpush(" + RedisServerNames.QUEUE_LIVE +", " + queue+", " + chat + ")");
            redisRepository.lpush(RedisServerNames.QUEUE_LIVE, queue, chat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* 구독자의 live 참여 정보 queue 전달  */
    public void pushLiveParticipateQueue(String liveID, String userIdx, String queueInfo){
        String keyShard = "" + (Integer.parseInt(userIdx) % 10);
        String redisKey = "live.joinuser." + liveID + "." + keyShard;

        redisRepository.rpush(RedisServerNames.QUEUE_LIVE, redisKey, queueInfo);
    }

    /**
     * 라이브톡 종료정보 전달
     * @param liveID 종료된 라이브톡의 라이브ID
     */
    public void setLiveEnd(String liveID) {
        String redisKey = RedisKey.LIVE_END;
        redisRepository.rpush(RedisServerNames.QUEUE_LIVE, redisKey, liveID);
    }

    /**
     * 생성된 채널(status 'PREPARE')의 LiveID 전달
     * @param liveID 생성된 채널(status PREPARE)의 LiveID
     */
    public void setPrepareLiveIDQueue(String liveID) {

        try {
            String redisKey = RedisKey.LIVE_PREPARE;
            redisRepository.rpush(RedisServerNames.QUEUE_LIVE, redisKey, liveID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
