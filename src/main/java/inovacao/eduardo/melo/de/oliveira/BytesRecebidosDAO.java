package inovacao.eduardo.melo.de.oliveira;


import BancoDeDados.BancoLooca;
import org.example.Console;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BytesRecebidosDAO {
    public void cadastroDeDados(BytesRecebidos bytesRecebidos, Console console){
        String sql = "INSERT INTO inovacaoEduardoMeloDeOliveira (bytesRecebidos,monitor) values (?,?)";

        try(PreparedStatement ps = BancoLooca.getbancoLooca2().prepareStatement(sql)){
            ps.setLong(1, bytesRecebidos.getBytesRecebidos());
            ps.setInt(2, console.getIdMonitoramento());
            ps.execute();

        }catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}


