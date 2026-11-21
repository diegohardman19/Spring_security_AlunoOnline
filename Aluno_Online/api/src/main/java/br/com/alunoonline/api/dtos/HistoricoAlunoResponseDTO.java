package br.com.alunoonline.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HistoricoAlunoResponseDTO {
    private String nomeAluno;
    private String cpfAluno;
    private String emailAluno;
    private List<DisciplinasAlunoResponseDTO> disciplinas;
}
