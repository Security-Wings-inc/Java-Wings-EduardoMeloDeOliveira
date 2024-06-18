package processos;
import BancoDeDados.BancoLooca;
import Log.LogMonitoramento;
import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.processos.Processo;
import com.github.britooo.looca.api.group.processos.ProcessoGrupo;
import com.github.britooo.looca.api.group.sistema.Sistema;
import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Utilitarios {

    public static void EncerrarProcessos() throws IOException {
        DateTimeFormatter formatadorDeData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        Looca looca = new Looca();
        Sistema sistema = looca.getSistema();
        ProcessoGrupo processos1 = looca.getGrupoDeProcessos();
        List<Processo> processos = processos1.getProcessos();

        String sql = "SELECT processoName FROM processos WHERE fkCategoria = 2";

        try (PreparedStatement ps = BancoLooca.getbancoLooca2().prepareStatement(sql)) {
            try (ResultSet resultSet = ps.executeQuery()) {
                System.out.println("Os processos estão sendo encerrados...");
                while (resultSet.next()) {
                    String nomesProcessosStr = (String) resultSet.getObject("processoName");
                    List<String> nomeProcesso = Arrays.asList(nomesProcessosStr.split(","));

                    Map<String, List<Processo>> processosMap = processos.parallelStream().collect(Collectors.groupingBy(processo -> processo.getNome().toLowerCase()));

                    for (String nome : nomeProcesso) {
                        String trimmedNome = nome.trim().toLowerCase();

                        List<Processo> processosParaEncerrar = processosMap.getOrDefault(trimmedNome, Collections.emptyList());

                        List<Integer> pids = processosParaEncerrar.stream().map(Processo::getPid).collect(Collectors.toList());

                        try {
                            System.out.println("""
                                    ╔═════════════════════╗
                                    ║       Processo      ║
                                    ║  Nome: %s           
                                    ║                     ║
                                    ╚═════════════════════╝
                                    """.formatted(trimmedNome));

                            if (sistema.getSistemaOperacional().equalsIgnoreCase("Windows")) {
                                String pidsString = pids.stream().map(String::valueOf).collect(Collectors.joining(","));
                                PowerShellResponse response = PowerShell.executeSingleCommand(String.format("Stop-Process -Id %s -Force", pidsString));
                            }
                        } catch (Exception e) {
                            System.err.println("Erro ao encerrar os processos: " + e.getMessage());
                        }
                    }
                }
                System.out.println("""
                                         /* ╔═════════════════════════════════════════╗ */
                                         /* ║                                         ║ */
                                         /* ║  PROCESSOS DOS UTILITARIOS ENCERRADOS!  ║ */
                                         /* ║                                         ║ */
                                         /* ╚═════════════════════════════════════════╝ */
                                        """);
                LocalDateTime localDateTime = LocalDateTime.now();
                LogMonitoramento.generateLog("{" + formatadorDeData.format(localDateTime) + "} " +"[INFO] " + " Encerramento de Processos terminou. " + "\n");
            }
        } catch (SQLException e) {
            LocalDateTime localDateTime = LocalDateTime.now();
            LogMonitoramento.generateLog("{" + formatadorDeData.format(localDateTime) + "} " + "[WARNING] " + " Erro no encerramento dos processos. " + "\n");
        }
    }
}
