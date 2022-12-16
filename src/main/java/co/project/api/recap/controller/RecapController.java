package co.project.api.recap.controller;

import co.project.api.common.code.CommonCode;
import co.project.api.recap.dto.*;
import co.project.api.recap.service.RecapService;
import co.project.api.recap.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "api/v2/recap")
public class RecapController {

    @Autowired
    RecapService recapService;

    @GetMapping("/test")
    public CommonResDTO test()  throws Exception{

        TestReqDTO testRequest = TestReqDTO.builder()
                .message("테스트입니다.")
                .build();

        return CommonResDTO.builder()
                .data(testRequest)
                .build();

    }

    @GetMapping("/signIn")
    public CommonResDTO signIn(@RequestBody @Valid SignInReqDTO signInRequest) throws Exception{


        recapService.saveUser(User.builder()
                .name(signInRequest.getName())
                .email(signInRequest.getEmail())
                .build());

        return CommonResDTO.builder()
                .code(CommonCode.COMMON_PARAM_ERROR_CODE)
                .build();

    }

    @GetMapping("/list")
    public CommonResDTO<List<DailyReportListResDTO>> list(@RequestBody @Valid SignInReqDTO signInRequest, @RequestBody DailyReportListReqDTO dailyReportListReqDTO,
                                                          @PathVariable("text") String text) throws Exception {


        List<DailyReportListResDTO>  result = recapService.getPlayingLiveList(text);

        return CommonResDTO.<List<DailyReportListResDTO>>builder()
                .data(result)
                .build();

    }

}
