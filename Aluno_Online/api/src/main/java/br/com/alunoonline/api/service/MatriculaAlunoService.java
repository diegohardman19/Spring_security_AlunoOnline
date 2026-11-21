package br.com.alunoonline.api.service;

import br.com.alunoonline.api.MatriculaAlunoStatusEnum;
import br.com.alunoonline.api.dtos.DisciplinasAlunoResponseDTO;
import br.com.alunoonline.api.dtos.HistoricoAlunoResponseDTO;
import br.com.alunoonline.api.model.Aluno;
import br.com.alunoonline.api.model.MatriculaAluno;
import br.com.alunoonline.api.repository.AlunoRepository;
import br.com.alunoonline.api.repository.MatriculaAlunoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import br.com.alunoonline.api.dtos.atualizarNotaRequestDTO;

import java.util.List;

@Service
public class MatriculaAlunoService {
    @Autowired
    MatriculaAlunoRepository matriculaAlunoRepository;

    @Autowired
    AlunoRepository alunoRepository;

    // ═══════════════ EMITIR HISTÓRICO ═══════════════
    public HistoricoAlunoResponseDTO emitirHistoricoAluno(long alunoId) {

        // 1) Buscar o aluno, se não existe - 404
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Aluno não encontrado"));

        // 2) Buscar todas as matrículas do aluno
        List<MatriculaAluno> matriculas =
                matriculaAlunoRepository.findByAlunoId(alunoId);

        // 3) Montar uma linha do histórico para cada disciplina
        List<DisciplinasAlunoResponseDTO> disciplinas = matriculas.stream()
                .map(matricula -> new DisciplinasAlunoResponseDTO(
                        matricula.getDisciplina().getNome(),
                        matricula.getDisciplina().getProfessor() != null
                                ? matricula.getDisciplina().getProfessor().getNomeCompleto()
                                : null,
                        matricula.getNota1(),
                        matricula.getNota2(),
                        calcularMedia(matricula),
                        matricula.getStatus()))
                .toList();

        // 4) Juntar os dados do aluno com a lista de disciplinas
        return new HistoricoAlunoResponseDTO(
                aluno.getNomeCompleto(),
                aluno.getCpf(),
                aluno.getEmail(),
                disciplinas);
    }

    private Double calcularMedia(MatriculaAluno matricula) {
        if (matricula.getNota1() != null && matricula.getNota2() != null) {
            return (matricula.getNota1() + matricula.getNota2()) / 2;
        }
        return null;
    }

    private static final Double MEDIA_PARA_APROVAR = 7.0;

    // ═══════════════ ATUALIZAR NOTAS ═══════════════
    public void atualizarNotas(Long id,
                               atualizarNotaRequestDTO dto) {

        // 1) Buscar matrícula (mesmo padrão do trancar)
        MatriculaAluno matricula =
                matriculaAlunoRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Matricula não encontrada"));

        // 2) Só atualiza o que veio preenchido (PATCH parcial!)
        if (dto.getNota1() != null)
            matricula.setNota1(dto.getNota1());
        if (dto.getNota2() != null)
            matricula.setNota2(dto.getNota2());

        // 3) Se as 2 notas existem, calcula média e define status
        if (matricula.getNota1() != null
                && matricula.getNota2() != null) {
            Double media = (matricula.getNota1()
                    + matricula.getNota2()) / 2;
            matricula.setStatus(
                    media >= MEDIA_PARA_APROVAR
                            ? MatriculaAlunoStatusEnum.APROVADO
                            : MatriculaAlunoStatusEnum.REPROVADO);
        }

        // 4) Salvar a matrícula atualizada
        matriculaAlunoRepository.save(matricula);
    }
    public void criarMatricula(MatriculaAluno matriculaAluno){
        matriculaAluno.setStatus(MatriculaAlunoStatusEnum.MATRICULADO);
        matriculaAlunoRepository.save(matriculaAluno);
    }

    public void trancarMatricula(Long id){

        // buscar matricula antes de atualizar, se nao existe - 404
        MatriculaAluno matriculaAluno = matriculaAlunoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Mtricula nao encontrada"
        ));

        if (matriculaAluno.getStatus()
                .equals(MatriculaAlunoStatusEnum.MATRICULADO)){
                matriculaAluno.setStatus(MatriculaAlunoStatusEnum.TRANCADO);
            matriculaAlunoRepository.save(matriculaAluno);
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"So e possivel tranca com o status mariculado");
        }
    }
}
