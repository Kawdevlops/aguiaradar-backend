package br.com.fiap.aguiaradar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightIAResponse {
    private String insight;
    private boolean geradoPorIA;
}
