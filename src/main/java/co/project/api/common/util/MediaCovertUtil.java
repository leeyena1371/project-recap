package co.project.api.common.util;

import co.dearu.live.api.common.config.AWSConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.mediaconvert.MediaConvertClient;
import software.amazon.awssdk.services.mediaconvert.model.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MediaCovertUtil {

    @Autowired
    AWSConfig awsConfig;

    public String createMediaJob(String liveID, MediaConvertClient mediaConvertClient, String mediaConvertRoleARN, List<String> inputFiles, String outputPath) {
        log.info("########## createMediaJob START");

        String mp4Output = outputPath;
        try {
            // MediaConvert Endpoint 조회
            DescribeEndpointsResponse describeEndpointsResponse = mediaConvertClient.describeEndpoints(DescribeEndpointsRequest.builder().maxResults(1).build());
            if (describeEndpointsResponse.endpoints().size() <= 0) {
                System.exit(1);
            }
            String endpointURL = describeEndpointsResponse.endpoints().get(0).url();
            Region region = Region.AP_NORTHEAST_1;
            MediaConvertClient endpointMediaConvertClient = MediaConvertClient.builder()
                    .region(region)
                    .credentialsProvider(awsConfig.getAWSCredential())
                    .endpointOverride(URI.create(endpointURL))
                    .build();
            OutputGroup fileMp4 = OutputGroup.builder().name("File Group").customName("mp4")
                    .outputGroupSettings(OutputGroupSettings.builder().type(OutputGroupType.FILE_GROUP_SETTINGS)
                            .fileGroupSettings(FileGroupSettings.builder().destination(mp4Output).build()).build())
                    .outputs(Output.builder().nameModifier("-" + liveID).extension("mp4")
                            .containerSettings(ContainerSettings.builder().container(ContainerType.MP4).build())
                            .videoDescription(VideoDescription.builder().width(1920).height(1080)
                                    .scalingBehavior(ScalingBehavior.DEFAULT).sharpness(50).antiAlias(AntiAlias.ENABLED)
                                    .timecodeInsertion(VideoTimecodeInsertion.DISABLED)
                                    .colorMetadata(ColorMetadata.INSERT).respondToAfd(RespondToAfd.NONE)
                                    .afdSignaling(AfdSignaling.NONE).dropFrameTimecode(DropFrameTimecode.ENABLED)
                                    .codecSettings(VideoCodecSettings.builder().codec(VideoCodec.H_264)
                                            .h264Settings(H264Settings.builder()
                                                    .rateControlMode(H264RateControlMode.QVBR)
                                                    .parControl(H264ParControl.INITIALIZE_FROM_SOURCE)
                                                    .qualityTuningLevel(H264QualityTuningLevel.SINGLE_PASS)
                                                    .qvbrSettings(H264QvbrSettings.builder().qvbrQualityLevel(8).build())
                                                    .codecLevel(H264CodecLevel.AUTO).codecProfile(H264CodecProfile.MAIN)
                                                    .maxBitrate(4000000)
                                                    .framerateControl(H264FramerateControl.INITIALIZE_FROM_SOURCE)
                                                    .gopSize(2.0).gopSizeUnits(H264GopSizeUnits.SECONDS)
                                                    .numberBFramesBetweenReferenceFrames(2).gopClosedCadence(1)
                                                    .gopBReference(H264GopBReference.DISABLED)
                                                    .slowPal(H264SlowPal.DISABLED).syntax(H264Syntax.DEFAULT)
                                                    .numberReferenceFrames(3).dynamicSubGop(H264DynamicSubGop.STATIC)
                                                    .fieldEncoding(H264FieldEncoding.PAFF)
                                                    .sceneChangeDetect(H264SceneChangeDetect.ENABLED).minIInterval(0)
                                                    .telecine(H264Telecine.NONE)
                                                    .framerateConversionAlgorithm(
                                                            H264FramerateConversionAlgorithm.DUPLICATE_DROP)
                                                    .entropyEncoding(H264EntropyEncoding.CABAC).slices(1)
                                                    .unregisteredSeiTimecode(H264UnregisteredSeiTimecode.DISABLED)
                                                    .repeatPps(H264RepeatPps.DISABLED)
                                                    .adaptiveQuantization(H264AdaptiveQuantization.HIGH)
                                                    .spatialAdaptiveQuantization(
                                                            H264SpatialAdaptiveQuantization.ENABLED)
                                                    .temporalAdaptiveQuantization(
                                                            H264TemporalAdaptiveQuantization.ENABLED)
                                                    .flickerAdaptiveQuantization(
                                                            H264FlickerAdaptiveQuantization.DISABLED)
                                                    .softness(0).interlaceMode(H264InterlaceMode.PROGRESSIVE).build())
                                            .build())
                                    .build())
                            .audioDescriptions(AudioDescription.builder()
                                    .audioTypeControl(AudioTypeControl.FOLLOW_INPUT)
                                    .languageCodeControl(AudioLanguageCodeControl.FOLLOW_INPUT)
                                    .codecSettings(AudioCodecSettings.builder().codec(AudioCodec.AAC)
                                            .aacSettings(AacSettings.builder().codecProfile(AacCodecProfile.LC)
                                                    .rateControlMode(AacRateControlMode.CBR)
                                                    .codingMode(AacCodingMode.CODING_MODE_2_0).sampleRate(44100)
                                                    .bitrate(128000).rawFormat(AacRawFormat.NONE)
                                                    .specification(AacSpecification.MPEG4)
                                                    .audioDescriptionBroadcasterMix(
                                                            AacAudioDescriptionBroadcasterMix.NORMAL)
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build();
            Map<String, AudioSelector> audioSelectors = new HashMap<String, AudioSelector>();
            audioSelectors.put("Audio Selector 1",
                    AudioSelector.builder().defaultSelection(AudioDefaultSelection.DEFAULT).offset(0).build());
            List<Input> inputs = new ArrayList<>();
            assert false;
            for (String inputFile : inputFiles) {
                inputs.add(Input.builder().audioSelectors(audioSelectors)
                        .videoSelector(
                                VideoSelector.builder().colorSpace(ColorSpace.FOLLOW).rotate(InputRotate.DEGREE_0).build())
                        .filterEnable(InputFilterEnable.AUTO).filterStrength(0).deblockFilter(InputDeblockFilter.DISABLED)
                        .denoiseFilter(InputDenoiseFilter.DISABLED).psiControl(InputPsiControl.USE_PSI)
                        .timecodeSource(InputTimecodeSource.EMBEDDED).fileInput(inputFile).build());
            }
            JobSettings jobSettings = JobSettings.builder().inputs(inputs)
                    .outputGroups(fileMp4).build();
            CreateJobRequest createJobRequest = CreateJobRequest.builder().role(mediaConvertRoleARN).settings(jobSettings)
                    .build();
            CreateJobResponse createJobResponse = endpointMediaConvertClient.createJob(createJobRequest);
            return createJobResponse.job().id();
        } catch (MediaConvertException e) {
            log.error(e.toString());
            System.exit(0);
        }
        return "";
    }
}
