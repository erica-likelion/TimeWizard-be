package timeWizard.bilnut.dto;

public record AiRequestFormData(String depart,  Integer grade, Integer semester,
                                Integer goalCredit, Integer maxCredit, String requirement,
                                String curriculumCsvUrl, String curriculumImageUrl) {
}
