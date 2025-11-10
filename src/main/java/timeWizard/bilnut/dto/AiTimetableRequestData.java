package timeWizard.bilnut.dto;

public record AiTimetableRequestData(String requestText,
                                     Integer maxCredit,
                                     Integer targetCredit) {
}
