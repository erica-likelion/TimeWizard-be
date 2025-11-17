package timeWizard.bilnut.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferencesResponse {

    @JsonProperty("preferred_days")
    private List<String> preferredDays;

    @JsonProperty("preferred_start_time")
    private String preferredStartTime;

    @JsonProperty("preferred_end_time")
    private String preferredEndTime;

    @JsonProperty("target_credits")
    private Integer targetCredits;

    @JsonProperty("required_courses")
    private List<Long> requiredCourses;

    @JsonProperty("excluded_courses")
    private List<Long> excludedCourses;

    public static PreferencesResponse from(String preferencesJson) {
        if (preferencesJson == null || preferencesJson.trim().isEmpty()) {
            return PreferencesResponse.builder()
                    .preferredDays(new ArrayList<>())
                    .preferredStartTime(null)
                    .preferredEndTime(null)
                    .targetCredits(null)
                    .requiredCourses(new ArrayList<>())
                    .excludedCourses(new ArrayList<>())
                    .build();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = objectMapper.readValue(preferencesJson, new TypeReference<Map<String, Object>>() {});
            
            // List 변환 헬퍼 메서드
            List<String> preferredDays = convertToStringList(map.get("preferred_days"));
            List<Long> requiredCourses = convertToLongList(map.get("required_courses"));
            List<Long> excludedCourses = convertToLongList(map.get("excluded_courses"));
            
            return PreferencesResponse.builder()
                    .preferredDays(preferredDays)
                    .preferredStartTime((String) map.get("preferred_start_time"))
                    .preferredEndTime((String) map.get("preferred_end_time"))
                    .targetCredits(map.get("target_credits") != null ? 
                            ((Number) map.get("target_credits")).intValue() : null)
                    .requiredCourses(requiredCourses)
                    .excludedCourses(excludedCourses)
                    .build();
        } catch (Exception e) {
            // JSON 파싱 실패 시 빈 객체 반환
            return PreferencesResponse.builder()
                    .preferredDays(new ArrayList<>())
                    .preferredStartTime(null)
                    .preferredEndTime(null)
                    .targetCredits(null)
                    .requiredCourses(new ArrayList<>())
                    .excludedCourses(new ArrayList<>())
                    .build();
        }
    }

    public String toJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static List<String> convertToStringList(Object obj) {
        if (obj == null) {
            return new ArrayList<>();
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        }
        return new ArrayList<>();
    }

    private static List<Long> convertToLongList(Object obj) {
        if (obj == null) {
            return new ArrayList<>();
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            List<Long> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Number) {
                    result.add(((Number) item).longValue());
                } else if (item != null) {
                    try {
                        result.add(Long.parseLong(item.toString()));
                    } catch (NumberFormatException e) {
                        // 무시
                    }
                }
            }
            return result;
        }
        return new ArrayList<>();
    }
}

