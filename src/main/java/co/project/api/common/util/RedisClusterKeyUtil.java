package co.project.api.common.util;

import co.dearu.live.api.common.code.*;
import co.dearu.live.api.common.config.redis.*;
import co.dearu.live.api.common.exception.*;
import co.dearu.live.api.common.model.vo.TeenagerTimeLimitVO;
import co.dearu.live.api.domain.like.*;
import co.dearu.live.api.domain.like.code.*;
import co.dearu.live.api.domain.live.model.entity.*;
import co.dearu.live.api.domain.live.model.vo.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Redis Cluster 조회용 Bean
 * method 작명 추천 (필수는 아님)
 * - get [Cluster이름] [조회할 데이터] By [조회Key]
 * 수정 필요
 * - redis 서버 오류로 인한 exception 발생 시 공통 처리 방안 필요
 */
@Slf4j
@Component
public class RedisClusterKeyUtil {

    @Autowired
    RedisRepository redisRepository;

    @Value("${app-group.default}")
    private String defaultAppGroup;

    /* certikey 기반 userIdx 조회 */
    public String getAcntUserIdxByCertiKey(String appGroup, String certiKey) {
        /**
         * 조회 타입 : hget
         * Redis Key Format : [appGroup.]certiKeys.useridx.[certiKey 앞 4글자] [certiKey]
         * 반환 : 데이터 있음 - String : useridx
         *       데이터 없음 - null
         * */
        try {
            // 1. appGroup별 key 생성
            String key = "";
            if (defaultAppGroup.equals(appGroup)) {
                key = "certiKeys.useridx." + certiKey.substring(0, 4);
            } else {
                key = appGroup + ".certiKeys.useridx." + certiKey.substring(0, 4);
            }

            // 2. key 기준 데이터 조회
            String userIdx = null;
            userIdx = redisRepository.hget(RedisServerNames.CLUSTER_ACNT_READ, key, certiKey);
            if (null == userIdx || "".equals(userIdx)) {
                userIdx = null;
            }
            return userIdx;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /* certikey 기반 deviceKey 조회 */
    public String getAcntDeviceKeyByCertiKey(String appGroup, String certiKey) {
        /**
         * 조회 타입 : hget
         * Redis Key Format : [appGroup.]certiKeys.deviceKey.[certiKey 앞 4글자] [certiKey]
         * 반환 : 데이터 있음 - String : deviceKey
         *       데이터 없음 - null
         * */
        try {
            // 1. appGroup별 key 생성
            String key = "";
            if (defaultAppGroup.equals(appGroup)) {
                key = "certiKeys.deviceKey." + certiKey.substring(0, 4);
            } else {
                key = appGroup + ".certiKeys.deviceKey." + certiKey.substring(0, 4);
            }

            // 2. key 기준 데이터 조회
            String deviceKey = null;
            deviceKey = redisRepository.hget(RedisServerNames.CLUSTER_ACNT_READ, key, certiKey);
            if (null == deviceKey || "".equals(deviceKey)) {
                deviceKey = null;
            }
            return deviceKey;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* useridx 기반 idType 조회 */
    public String getAcntIdTypeByUserIdx(String userIdx) {
        /**
         * 조회 타입 : hget
         * Redis Key Format : user.idType.[useridx / 1000] [useridx % 1000]
         * 반환 : 데이터 있음 - String : deviceKey
         *       데이터 없음 - null
         * */
        try {
            int k1 = CommonUtil.getIdxKeySplitDivide(userIdx);
            int k2 = CommonUtil.getIdxKeySplitModular(userIdx);

            String idType = null;
            idType = redisRepository.hget(RedisServerNames.CLUSTER_ACNT_READ, "user.idType." + k1, String.valueOf(k2));
            if (null == idType || "".equals(idType)) {
                idType = null;
            }
            return idType;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* starUserIdx, userIdx 기반 채팅 금지 여부 조회 */
    public boolean checkChatBanUser(String starUserIdx, String userIdx) {
        /**
         * 조회 타입 : isMember
         * Redis Key Format : live.chatBanList.${staruseridx}
         * 반환 : 데이터 있음 - true
         *       데이터 없음 - false
         * */
        try {
            String key = String.format(RedisKey.LIVE_CHAT_BAN_LIST, starUserIdx);
            return redisRepository.isMember(RedisServerNames.CLUSTER_LIVE_READ, key, userIdx);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* starUserIdx 기반 채팅 금지 유저 리스트 조회 */
    public Set<String> getChatBanUserListByStarUserIdx(String starUserIdx) {
        /**
         * 조회 타입 : smembers
         * Redis Key Format : live.chatBanList.${staruseridx}
         * 반환 : 데이터 있음 - Set<String>
         *       데이터 없음 - null
         * */
        try {
            String key = String.format(RedisKey.LIVE_CHAT_BAN_LIST, starUserIdx);
            return redisRepository.sMember(RedisServerNames.CLUSTER_LIVE_READ, key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* starUserIdx 기반 라이브 금지 유저 리스트 조회 */
    public Set<String> getLiveBanUserListByStarUserIdx(String starUserIdx) {
        /**
         * 조회 타입 : smembers
         * Redis Key Format : live.banList.${staruseridx}
         * 반환 : 데이터 있음 - Set<String>
         *       데이터 없음 - null
         * */
        Set liveBanTotalList = new HashSet<String>();
        try {
            String key = String.format(RedisKey.LIVE_BAN_LIST, starUserIdx);
            Set liveBanList = redisRepository.sMember(RedisServerNames.CLUSTER_LIVE_READ, key);
            log.info("liveBanList >> " + liveBanList);
            if(liveBanList != null){
                liveBanTotalList.addAll(liveBanList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            String key = RedisKey.BUBBLE_BLOCKUSER_LIST;
            Set bubbleBlockUserList = redisRepository.sMember(RedisServerNames.CLUSTER_ACNT_READ, key);
            log.info("bubbleBlockUserList >> " + bubbleBlockUserList);
            if(bubbleBlockUserList != null){
                liveBanTotalList.addAll(bubbleBlockUserList);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return liveBanTotalList;
    }


    /* liveID 기반 live info 조회 */
    public String getLiveInfo(String liveID) {
        /**
         * 명령 타입 : get
         * Redis Key Format : live.info.${liveID}
         * 반환 : 데이터 있음 - String
         *       데이터 없음 - null
         * */
        String key = String.format(RedisKey.LIVE_INFO, liveID);
        String liveInfo = null;
        try {
            liveInfo = redisRepository.get(RedisServerNames.CLUSTER_LIVE_READ, key);
            if (liveInfo == null || "".equals(liveInfo)) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            liveInfo = null;
        }

        return liveInfo;
    }

    /* liveID 기반 숨김처리 해야할 메시지 cKey 목록 조회 */
    public Set getHiddenCkeyList(String liveID) {
        /**
         * 명령 타입 : smembers
         * Redis Key Format : live.hiddenCkeyList.${liveID}
         * 반환 : 데이터 있음 - Set<String>
         *       데이터 없음 - null
         * */

        String key = String.format(RedisKey.LIVE_HIDDEN_CKEY_LIST, liveID);

        Set<String> hiddenCKeyList = new HashSet<String>();
        try {
            hiddenCKeyList = redisRepository.sMember(RedisServerNames.CLUSTER_LIVE_READ, key);
        } catch (Exception e) {
            e.printStackTrace();
            hiddenCKeyList = new HashSet<String>();
        }
        return hiddenCKeyList;
    }

    /* liveID 기반 좋아요 수치 조회 */
    public Map<String, Integer> getLikeSum(String liveID) {

        String freeKey = "live.currentLikesFree." + liveID + ".";
        String payKey = "live.currentLikesPay." + liveID + ".";

        Map<String, Integer> result = new HashMap<String, Integer>();

        List<String> freeKeyList = new ArrayList<String>();
        List<String> payKeyList = new ArrayList<String>();

        for (int i = 0; i < 10; i++) {
            freeKeyList.add(freeKey + i);
            payKeyList.add(payKey + i);
        }
        int freeLikeSum = 0;
        int payLikeSum = 0;

        List<Object> freeLikeInfoList = new ArrayList<Object>();
        List<Object> payLikeInfoList = new ArrayList<Object>();

        try {
            freeLikeInfoList = redisRepository.getPipeline(RedisServerNames.CLUSTER_LIVE_READ, freeKeyList);
            payLikeInfoList = redisRepository.getPipeline(RedisServerNames.CLUSTER_LIVE_READ, freeKeyList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Object likeInfo : freeLikeInfoList) {
            if (likeInfo != null) {
                try {
                    freeLikeSum += Integer.parseInt(likeInfo.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for (Object likeInfo : payLikeInfoList) {
            if (likeInfo != null) {
                try {
                    payLikeSum += Integer.parseInt(likeInfo.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        result.put("paySum", payLikeSum);
        result.put("freeSum", freeLikeSum);

        return result;

    }

    /* useridx 기반 isTeenager 조회 */
    public boolean isUnderFifteen(String starUserIdx) {
        /**
         * 명령 타입 : isMember
         * Redis Key Format : bubble.staruser.isUnderFifteen.${staruseridx}
         * 반환 : 데이터 있음 - true
         *       데이터 없음 - false
         * */
        String key = RedisKey.BUBBLE_STARUSER_IS_UNDER_FIFTEEN;
        return redisRepository.isMember(RedisServerNames.CLUSTER_ACNT_READ, key, String.valueOf(starUserIdx));
    }

    /* 메시지 문구 조회 */
    public String getMsgForCode(String lang, int code) {
        /**
         * 명령 타입 : hget
         * Redis Key Format : dontalk.msg.${lang}
         * 반환 : 데이터 있음 - String
         *       데이터 없음 - null
         * */

        String key = String.format(RedisKey.DONTALK_MSG, lang);
        return redisRepository.hget(RedisServerNames.CLUSTER_ACNT_READ, key, String.valueOf(code));
    }

    /* Current LiveTalk Like (유/무료 좋아요, 시청자 수 조회) */
    public Map<String, Integer> getLiveCurrentLikes(String liveID) {
        String keyPay = String.format(RedisKey.LIVE_PAY_LIKE_SUM, liveID);
        String keyFree = String.format(RedisKey.LIVE_FREE_LIKE_SUM, liveID);

        String key = "live.currentLikes." + liveID;

        int likePay = 0;
        int likeFree = 0;

        try {
            likePay = Integer.parseInt(redisRepository.get(RedisServerNames.CLUSTER_LIVE_READ, keyPay));
        } catch (Exception e) {
            likePay = 0;
        }

        try {
            likeFree = Integer.parseInt(redisRepository.get(RedisServerNames.CLUSTER_LIVE_READ, keyFree));
        } catch (Exception e) {
            likeFree = 0;
        }

        Map result = new HashMap<String, Integer>();
        result.put("countLikesFree", likeFree);
        result.put("countLikesPay", likePay);

        return result;
    }

    /* Current LiveTalk Viewer Count (현재 시청자수) 조회 */
    public int getLiveCurrentCountViewer(String liveID) {
        /**
         * 명령 타입 : get
         * Redis Key Format : live.currentViewer.%s
         *
         * 반환 : 데이터 있음 - String
         *       데이터 없음 - null
         * */
        String key = String.format(RedisKey.LIVE_CURRENT_VIEWER, liveID);
        String currentCountViewers = redisRepository.get(RedisServerNames.CLUSTER_LIVE_READ, key);

        int result = 0;
        if (currentCountViewers != null) {
            result = Integer.parseInt(currentCountViewers);
        }

        return result;
    }

    /* liveID 기반 live info 저장 */
    public String setLiveInfo(LiveInfoVO liveInfoVO, String liveID) throws JsonProcessingException {
        /**
         * 명령 타입 : set
         * Redis Key Format : live.info.${liveID}
         * 반환 : 데이터 있음 - String("OK")
         *       데이터 없음 - null
         * */
        String key = String.format(RedisKey.LIVE_INFO, liveID);
        ObjectMapper objectMapper = new ObjectMapper();
        String liveInfoVOString = objectMapper.writeValueAsString(liveInfoVO);

        try {
            return redisRepository.set(RedisServerNames.CLUSTER_LIVE, key, String.valueOf(liveInfoVOString));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* 라이브 설정값 저장 */
    public void setLiveOption(String liveID, LiveSettings liveSettings) {

        String chatFlagKey = "chatFlag";

        int chatFlag = liveSettings.getChatFlag();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(chatFlagKey, chatFlag);

        // 1. 방송 설정값 저장
        try {
            String key = String.format(RedisKey.LIVE_OPTION, liveID);
            redisRepository.set(RedisServerNames.CLUSTER_LIVE, key, jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. 통합 조회용 liveInfo 조회 후 수정
        try {
            String liveInfoStr = getLiveInfo(liveID);
            JSONObject liveInfoJson = null;
            if (liveInfoStr != null) {
                liveInfoJson = new JSONObject(liveInfoStr);
            } else {
                liveInfoJson = new JSONObject();
            }

            liveInfoJson.put(chatFlagKey, jsonObject.get(chatFlagKey));

            String key = String.format(RedisKey.LIVE_INFO, liveID);
            redisRepository.set(RedisServerNames.CLUSTER_LIVE, key, liveInfoJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /* 유료아이템 사용 여부 조회 */
    public int getUseTypeLikesPay(String liveID, String userIdx) throws Exception {
        int authorityFlag = 0;
        try {
            String keyShard = "" + (Integer.parseInt(userIdx) % 10);
            String redisKey = String.format(RedisKey.LIVE_ITEM_USE_TYPE, liveID, keyShard);
            String hgetData = redisRepository.hget(RedisServerNames.CLUSTER_LIVE_READ, redisKey, userIdx);

            authorityFlag = Integer.parseInt(hgetData == null ? "0" : hgetData);
            return authorityFlag;
        } catch (Exception e) {
            throw new CommonException.RedisExecutionException("Redis 조회 오류가 발생했습니다.");
        }
    }

    /* 유료아이템 사용 여부 셋팅 */
    public void setUseTypeLikesPay(String liveID, String userIdx, String authorityFlag) throws Exception {
        try {
            String keyShard = "" + (Integer.parseInt(userIdx) % 10);
            String redisKey = String.format(RedisKey.LIVE_ITEM_USE_TYPE, liveID, keyShard);
            redisRepository.hset(RedisServerNames.CLUSTER_LIVE, redisKey, userIdx, authorityFlag);
        } catch (Exception e) {
            throw new CommonException.RedisExecutionException("Redis 저장 오류가 발생했습니다.");
        }
    }

    /* 좋아요 이벤트 설정값 저장 */
    public String setLikeEvent(String liveID, String likeEventValues) {
        /**
         * 명령 타입 : set
         * Redis Key Format : live.likeEvent.${liveID}
         * 반환 : 데이터 있음 - String("OK")
         *       데이터 없음 - null
         * */
        // live.likeEvent.${liveID}
        String key = String.format(RedisKey.LIVE_LIKE_EVENT, liveID);
        String result = null;
        try {
            result = redisRepository.set(RedisServerNames.CLUSTER_LIVE, key, String.valueOf(likeEventValues));
            if (result == null || "".equals(result)) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }

        return result;
    }

    /* 구독자의 live 참여 여부 저장 */
    public void setLiveParticipate(String liveID, String userIdx) {
        String keyShard = "" + (Integer.parseInt(userIdx) % 10);
        String redisKey = "live.participatedViewer." + liveID + "." + keyShard;
        redisRepository.setBit(RedisServerNames.CLUSTER_LIVE, redisKey, userIdx, true);
    }

    /* 구독자의 live 참여 여부 조회 */
    public boolean getLiveParticipate(String liveID, String userIdx) {
        String keyShard = "" + (Integer.parseInt(userIdx) % 10);
        String redisKey = String.format(RedisKey.LIVE_PARTICIPATEDVIEWER, liveID, keyShard);
        boolean result = redisRepository.getBit(RedisServerNames.CLUSTER_LIVE, redisKey, userIdx);
        return result;
    }

    /* 참여 유저 조회 */
    public byte[] getBytesParticipatedViewer(String liveID, int keyShard){
        /**
         * 명령 타입 : get
         * Redis Key Format : live.participatedViewer.${liveID}.${keySartd}
         * 반환 : 데이터 있음 - byte
         *       데이터 없음 - null
         * */
        String redisKey = String.format(RedisKey.LIVE_PARTICIPATEDVIEWER, liveID, keyShard);
        return redisRepository.getBytes(RedisServerNames.CLUSTER_LIVE, redisKey);
    }

    /* Redis liveInfo 삭제 */
    public boolean delLiveInfo(String liveID) {
        /**
         * 명령 타입 : del
         * Redis Key Format : live.info.${liveID}
         * 반환 : 데이터 있음 - true
         *       데이터 없음 - false
         * */
        String key = String.format(RedisKey.LIVE_INFO, liveID);
        return redisRepository.del(RedisServerNames.CLUSTER_LIVE, key);
    }

    /* starUserIdx 기반 채팅 금지 유저 리스트 조회 */
    public Set getManagerListByStarUserIdx(String starUserIdx) {
        /**
         * 조회 타입 : smembers
         * Redis Key Format : live.managerList.${staruseridx}
         * 반환 : 데이터 있음 - Set<String>
         *       데이터 없음 - null
         * */
        try {
            return redisRepository.sMember(RedisServerNames.CLUSTER_LIVE_READ, String.format(RedisKey.LIVE_MANAGER_LIST, starUserIdx));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* liveID, userIdx 라이브 매니저 여부 */
    public boolean checkLiveManager(String starUserIdx, String userIdx) {
        /**
         * 조회 타입 : isMember
         * Redis Key Format : live.managerList.${staruseridx}
         * 반환 : 데이터 있음 - true
         *       데이터 없음 - false
         * */
        try {
            String key = String.format(RedisKey.LIVE_MANAGER_LIST, starUserIdx);
            return redisRepository.isMember(RedisServerNames.CLUSTER_LIVE_READ, key, userIdx);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* userIdx 모니터링 계정 여부 */
    public boolean checkMonitoringUser(String starUserIdx, String userIdx) {
        /**
         * 조회 타입 : isMember
         * Redis Key Format : bubble.adminStarTalkMapping.{staruseridx}
         * 반환 : 데이터 있음 - true
         *       데이터 없음 - false
         * */
        try {
            String key = String.format(RedisKey.BUBBLE_MONITORING_LIST, starUserIdx);
            return redisRepository.isMember(RedisServerNames.CLUSTER_ACNT_READ, key, userIdx);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* starUserIdx 라이브 채팅 금지 유저 추가 */
    public void saddChatBanUser(String starUserIdx, String userIdx) {
        /**
         * 조회 타입 : smembers
         * Redis Key Format : live.chatBanList.${staruseridx}
         * 반환 : 데이터 있음 - Set<String>
         *       데이터 없음 - null
         * */
        try {
            String key = String.format(RedisKey.LIVE_CHAT_BAN_LIST, starUserIdx);
            redisRepository.sadd(RedisServerNames.CLUSTER_LIVE, key, userIdx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* starUserIdx 라이브 채팅 금지 유저 추가 */
    public void sremChatBanUser(String starUserIdx, String userIdx) {
        /**
         * 조회 타입 : smembers
         * Redis Key Format : live.chatBanList.${staruseridx}
         * 반환 : 데이터 있음 - Set<String>
         *       데이터 없음 - null
         * */
        try {
            String key = String.format(RedisKey.LIVE_CHAT_BAN_LIST, starUserIdx);
            redisRepository.srem(RedisServerNames.CLUSTER_LIVE, key, userIdx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* 해당 라이브톡 아티스트 화면에서 숨김 처리되어야 하는 채팅 리스트 */
    public void saddHiddenCkey(String liveID, String ckey) {
        /**
         * 조회 타입 : smembers
         * Redis Key Format : live.hiddenCkeyList.${liveID}
         * 반환 : 데이터 있음 - Set<String>
         *       데이터 없음 - null
         * */
        try {
            String key = String.format(RedisKey.LIVE_HIDDEN_CKEY_LIST, liveID);
            redisRepository.sadd(RedisServerNames.CLUSTER_LIVE, key, ckey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /* 채팅 제한 신고 개수 저장 */
    public String setChatLimit(int chatLimit, String liveID) {
        /**
         * 명령 타입 : set
         * Redis Key Format : live.chatBanList.${staruseridx}
         * 반환 : 데이터 있음 - String
         *       데이터 없음 - null
         * */
        // live.chatLimit${liveID}
        String key = String.format(RedisKey.LIVE_CHAT_LIMIT, liveID);
        String result = null;
        try {
            result = redisRepository.set(RedisServerNames.CLUSTER_LIVE, key, String.valueOf(chatLimit));
            if (result == null || "".equals(result)) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }

        return result;
    }

    /* 라이브 유료/무료 좋아요 저장 */
    public void setLikeCnt(String userIdx, String liveID, String likeType, int cnt) {
        try {
            String key = String.format(LikeVO.builder().build().getLikeRedisKey(likeType), liveID, Integer.parseInt(userIdx) % LikeCode.LIKE_USER_DIVISION_NUM);
            log.info(">>> redis increment(" + RedisServerNames.CLUSTER_LIVE + ", " + key + ", " + cnt + ")");
            redisRepository.increment(RedisServerNames.CLUSTER_LIVE, key, cnt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* 해당 라이브톡 아티스트 화면에서 숨김 처리되어야 하는 채팅 리스트 삭제 */
    public void sremHiddenCkey(String liveID, String ckey) {
        /**
         * 조회 타입 : smembers
         * Redis Key Format : live.hiddenCkeyList.${liveID}
         * 반환 : 데이터 있음 - Set<String>
         *       데이터 없음 - null
         * */
        try {
            String key = String.format(RedisKey.LIVE_HIDDEN_CKEY_LIST, liveID);
            redisRepository.srem(RedisServerNames.CLUSTER_LIVE, key, ckey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* 해당 라이브톡 채팅 신고 제한 개수 */
    public int getChatReportLimitSize(String liveID) {
        /**
         * 조회 타입 : get
         * Redis Key Format : live.chatLimit.${liveID}
         * 반환 : 데이터 있음 - int
         *       데이터 없음 - null
         * */
        String key = String.format(RedisKey.LIVE_CHAT_REPORT_LIMIT, liveID);
        String cnt = redisRepository.get(RedisServerNames.CLUSTER_LIVE, key);
        if (StringUtils.isBlank(cnt)) {
            return 0;
        }
        return Integer.parseInt(cnt);
    }

    /* 라이브톡 닉네임 금칙어 버전 조회 */
    public String getLiveNickNameBanVersion(String appGroup) {
        /**
         * 조회 타입 : get
         * Redis Key Format : {appGroup}.ban.livenick.version
         * 반환 : 데이터 있음 - string
         *       데이터 없음 - null
         * */
        String key = String.format(RedisKey.LIVE_NICKNAME_BAN_VERSION, appGroup);
        String version = redisRepository.get(RedisServerNames.CLUSTER_LIVE, key);
        if (version == null) {
            return "1";
        }
        return version;
    }

    /* 라이브톡 닉네임 금칙어 목록 조회 */
    public List<String> getLiveNickNameBanList(String appGroup, String version) {
        /**
         * 조회 타입 : smembers
         * Redis Key Format : {appGroup}.ban.livenick.version
         * 반환 : 데이터 있음 - set
         *       데이터 없음 - null
         * */
        List<String> nickNameBanList = new ArrayList<>();
        try {
            String key = String.format(RedisKey.LIVE_NICKNAME_BAN_LIST, appGroup, version);
            nickNameBanList = redisRepository.range(RedisServerNames.CLUSTER_LIVE_READ, key);
            if(nickNameBanList == null){
                nickNameBanList = new ArrayList<String>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<String>();
        }
        return nickNameBanList;
    }

    /* 라이브콜 traceIdx incr */
    public Long incrLiveTraceIdx(String liveID) {
        String key = String.format(RedisKey.LIVE_TRACEIDX, liveID);
        return redisRepository.increment(RedisServerNames.CLUSTER_LIVE, key);
    }

    /* 라이브콜 traceIdx 조회 */
    public Long getLiveTraceIdx(String liveID) {
        String key = String.format(RedisKey.LIVE_TRACEIDX, liveID);
        Long traceIdx = 0L;
        try{
            String resultStr = redisRepository.get(RedisServerNames.CLUSTER_LIVE, key);
            if(resultStr != null){
                traceIdx = Long.parseLong(resultStr);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            traceIdx = 0L;
        }
        return traceIdx;
    }

    /* 버블 구독 상태 정보 조회 */
    public JSONObject getBubbleMappingInfo(String userIdx){

        String key = String.format(RedisKey.BUBBLE_MAPPING_INFO, userIdx);

        String bubbleMappingInfo = "";
        try{
            bubbleMappingInfo = redisRepository.get(RedisServerNames.CLUSTER_ACNT_READ, key);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

        JSONObject bubbleMappingInfoJson = new JSONObject();
        try{
            bubbleMappingInfoJson = new JSONObject(bubbleMappingInfo);
        }
        catch (Exception e){
            e.printStackTrace();
            bubbleMappingInfoJson = null;
        }

        return bubbleMappingInfoJson;
    }

    /* 미성년자 제한 시간 정보 조회 */
    public TeenagerTimeLimitVO getTeenagerTimeLimitInfo(){
        String key = RedisKey.BUBBLE_TEENAGER_TIMERANGE;

        String timeLimitInfoStr = redisRepository.get(RedisServerNames.CLUSTER_ACNT_READ, key);

        TeenagerTimeLimitVO teenagerTimeLimitVO = new TeenagerTimeLimitVO();
        try{

            if(timeLimitInfoStr == null){
                teenagerTimeLimitVO.init();
            }
            else{
                ObjectMapper mapper = new ObjectMapper();
                teenagerTimeLimitVO = mapper.readValue(timeLimitInfoStr, TeenagerTimeLimitVO.class);
            }

        }
        catch (Exception e){
            e.printStackTrace();
            teenagerTimeLimitVO.init();
        }

        return teenagerTimeLimitVO;
    }

    /* bubble artist인지 확인 */
    public boolean isBubbleArtist(String appGroup, String userIdx){
        /**
         * 조회 타입 : ismember
         * Redis Key Format : [appGroup.]starTalkArtistList
         * 반환 : boolean (set 검색)
         * */
        try {
            // 1. appGroup별 key 생성
            String key = "";
            if (defaultAppGroup.equals(appGroup)) {
                key = "starTalkArtistList";
            } else {
                key = appGroup + ".starTalkArtistList";
            }
            return redisRepository.isMember(RedisServerNames.CLUSTER_CACHE_READ, key, userIdx);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}

