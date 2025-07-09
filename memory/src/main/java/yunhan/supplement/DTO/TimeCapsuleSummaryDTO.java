package yunhan.supplement.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeCapsuleSummaryDTO {
    private int timecapsuleId;
    private String title;
    private LocalDateTime openDate;
    private Boolean isOpened;
}
