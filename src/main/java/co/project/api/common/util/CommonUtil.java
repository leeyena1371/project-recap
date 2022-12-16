package co.project.api.common.util;

import org.apache.logging.log4j.util.Strings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 공통 static 로직 관리 객체
 * 참고 : static parameter 사용 X
 *      static method를 통한 변환 작업만 사용
 * */
public class CommonUtil {

    // token 유효 기간, 300 초(5 분)
    private static int EXPIRATION_PERIOD = 300;

    private static final int keyDivisionNumber = 1000;

    public static final String dateFormat = "yyyyMMddHHmmss";

    public static final String dateFormatForRDB = "yyyy-MM-dd HH:mm:ss";

    public static final String defaultTimeZone = "Asia/Seoul";

    /* useridx, roomidx k1 생성 */
    public static int getIdxKeySplitDivide(String userIdx){
        int k1 = Integer.parseInt(userIdx) / keyDivisionNumber;
        return k1;
    }

    /* useridx, roomidx k2 생성 */
    public static int getIdxKeySplitModular(String userIdx){
        int k2 = Integer.parseInt(userIdx) % keyDivisionNumber;
        return k2;
    }

    public static String getFormattedCurrentDate(){
        /**
         * UTC 기반 시간 String type으로 변환
         * */

        SimpleDateFormat formatedNow = new SimpleDateFormat(dateFormat);
        return formatedNow.format(getCurrentDate());
    }

    public static Date getCurrentDate(){
        /**
         * UTC 기반 시간
         * */
//        Instant ins = Instant.now();
//
//        OffsetDateTime odt = ins.atOffset(ZoneOffset.UTC);
//        ZonedDateTime zdt = ins.atZone(ZoneId.of("UTC"));
//
//        return Date.from(ins);

        return new Date();
    }

    public static String calculateTimeFromNow(int type, int interval){
        Date nowDate = getCurrentDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(nowDate);
        cal.add(type, interval);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);

        return simpleDateFormat.format(cal.getTime());
    }

    public static String substractHourInfo(String dateInfo){
        /**
         * dateInfo : global dateFormat 형식의 시간 정보 (14자리)
         * */
        return dateInfo.substring(0, 8) + "000000";
    }

    public static String getEpochCurrentDate(){
        Instant ins = Instant.now();
        long ts = ins.toEpochMilli();

        Instant ins2 = Instant.ofEpochMilli(ts);
        return ins2.toString();
    }

    /**
     * 날짜 차이 계산
     * @param startDT 포맷 : {yyyyMMddHHmmss}
     * @param endDT 포맷 : {yyyyMMddHHmmss}
     * @return 포맷 : {HHmmss}
     */
    public static String getDateDiff(String startDT, String endDT) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CommonUtil.dateFormat);
        if (endDT == null || endDT.equals("") || startDT == null || startDT.equals(""))
            return "";

        Date FormattedStartDT = simpleDateFormat.parse(startDT);
        Date FormattedEndDT = simpleDateFormat.parse(endDT);

        long DateDiff = FormattedEndDT.getTime() - FormattedStartDT.getTime();

        long hours = (DateDiff / 1000) / 60 / 60 % 24;
        long minutes = (DateDiff / 1000) / 60 % 60;
        long seconds = (DateDiff / 1000) % 60;

        return String.format("%02d"+"%02d"+"%02d", hours, minutes, seconds);
    }

    public static boolean convertTinyIntToBool(int num){
        boolean result = false;
        if(num == 0){
            result = false;
        }
        else{
            result = true;
        }
        return result;
    }

    public static int convertBoolToTinyInt(boolean bool){
        int result = 0;
        if(bool == true){
            result = 1;
        }
        else{
            result = 0;
        }
        return result;
    }

//    /**
//     * PlaybackUrl에 서명된 토큰 추가
//     * @throws IOException
//     * @throws NoSuchAlgorithmException
//     * @throws SignatureException
//     * @throws InvalidKeyException
//     * @throws InvalidKeySpecException
//     */
//    public static String appendTokenToPlaybackUrl(String playbackUrl, String channelArn, String ecPrivateKeyStr) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException {
//
////        token.header
//        final ObjectMapper objectMapper = new ObjectMapper();
//        final Map<String, Object> header = Maps.newLinkedHashMap();
//        header.put("alg", "ES384");
//        header.put("typ", "JWT");
//        final String headerStr = Base64.encodeBase64URLSafeString(objectMapper.writeValueAsBytes(header));
//
////        token.payload
//        final Map<String, Object> payload = Maps.newLinkedHashMap();
//        payload.put("aws:channel-arn", channelArn);
//        payload.put("aws:access-control-allow-origin", "*");
//        payload.put("exp", System.currentTimeMillis()/1000 + EXPIRATION_PERIOD);
//        final String payloadStr = Base64.encodeBase64URLSafeString(objectMapper.writeValueAsBytes(payload));
//
////        token.signature
//        KeyFactory keyFactory = KeyFactory.getInstance("EC");
//
//        ECPrivateKey ecPrivateKey = (ECPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decodeBase64(ecPrivateKeyStr)));
//
//        final Signature signature = Signature.getInstance("SHA384withECDSAinP1363Format");
//        signature.initSign(ecPrivateKey);
//        signature.update((headerStr + "." + payloadStr).getBytes());
//        byte[] signatureBytes = signature.sign();
//        final String signatureStr = Base64.encodeBase64URLSafeString(signatureBytes);
//        final String jwt = headerStr + "." + payloadStr + "." + signatureStr;
//
//        return playbackUrl+ "?token="+jwt;
//
//    }

    public static String getFormattedMillisecondToDate(String millisecond){

        String date = "";
        if(Strings.isBlank(millisecond)){
            return date;
        }

        LocalDateTime cvDate = LocalDateTime.now();
        Long milliSec = Long.parseLong(millisecond);

        // IOS 는 16자리의 microsecond 로 넘어오기 때문에 변환 필요
        if(millisecond.length()==16){
            milliSec = TimeUnit.MILLISECONDS.convert(milliSec, TimeUnit.MICROSECONDS);
        }

        cvDate = Instant.ofEpochMilli(milliSec).atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        date = cvDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return date;
    }

    /**
     * DB에서 조회 해온 시간 값 포맷 변환
     * @param RdbDT 포맷 : {yyyy-MM-dd HH:mm:ss}
     * @return 포맷 : {yyyyMMddHHmmss}
     * @throws ParseException
     */
    public static String convertDTFromatRdbToString(String RdbDT) throws ParseException {
        if (RdbDT == null || RdbDT.equals(""))
            return "";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CommonUtil.dateFormat);
        SimpleDateFormat simpleDateFormatForRDB = new SimpleDateFormat(CommonUtil.dateFormatForRDB);
        Date RdbDateTime = simpleDateFormatForRDB.parse(RdbDT);
        return simpleDateFormat.format(RdbDateTime);
    }

    /**
     * byte[] to BitSet
     * offset 값을 얻기 위해 reverse 필요
     *  - redis의 bitset을 저장할 때, offset 0 이 의미하는 것은 바로 첫번째 1byte의 가장 최상위 bit를 의미
     *  - 0 offset true를 지정하고 get해서 BitSet으로 얻어오면 7, 1 offset에 true를 저장하면 6으로 값을 얻음
     * @param bytes
     * @return BitSet
     */
    public static BitSet fromByteArrayReverse(byte[] bytes) {
        BitSet bits = new BitSet();
        if (bytes != null) {
            for (int i = 0; i < bytes.length * 8; i++) {
                if ((bytes[i / 8] & (1 << (7 - (i % 8)))) != 0) {
                    bits.set(i);
                }
            }
        }
        return bits;
    }

    public static boolean isAfterDate(String baseDT, String term, String targetDT){

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseDate = LocalDateTime.parse(baseDT, DateTimeFormatter.ofPattern(dateFormat));
        LocalDateTime date = LocalDateTime.parse(targetDT, DateTimeFormatter.ofPattern(dateFormat));

        if(now.isAfter(baseDate)) {
            if(now.isAfter(date.plusHours(Long.parseLong(term)))){
                return false;
            }
        }
        return true;
    }


}
